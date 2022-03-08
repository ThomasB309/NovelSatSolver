package cas.thomas.SolverAlgorithms;

import cas.thomas.ConflictHandling.CDCLConflictHandler;
import cas.thomas.ConflictHandling.ConflictHandlingStrategy;
import cas.thomas.Exceptions.SolverTimeoutException;
import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.Formulas.Formula;
import cas.thomas.RestartHandling.ReluctantDoublingRestartStrategy;
import cas.thomas.RestartHandling.RestartSchedulingStrategy;
import cas.thomas.VariableSelection.VSIDS;
import cas.thomas.VariableSelection.VariableSelectionStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class IncrementalCdclSolver extends mDPLL implements IncrementalSatSolver {

	private List<int[]> clauses;
	private List<int[]> amoConstraints;
	private List<int[][]> dnfConstraints;
	private int maxVariable;
	private Formula formula;
	private long timeLimit = 0;

	public IncrementalCdclSolver(VariableSelectionStrategy variableSelectionStrategy, ConflictHandlingStrategy conflictHandlingStrategy, RestartSchedulingStrategy restartSchedulingStrategy, boolean phaseSaving, boolean firstBranchingDecision) {
		super(variableSelectionStrategy, conflictHandlingStrategy, restartSchedulingStrategy, phaseSaving, firstBranchingDecision);
		clauses = new ArrayList<>();
		amoConstraints = new ArrayList<>();
		dnfConstraints = new ArrayList<>();
		maxVariable = Integer.MIN_VALUE;
	}

	public IncrementalCdclSolver() {
		super(new VSIDS(), new CDCLConflictHandler(), new ReluctantDoublingRestartStrategy(512), true, false);
		clauses = new ArrayList<>();
		amoConstraints = new ArrayList<>();
		dnfConstraints = new ArrayList<>();
		maxVariable = Integer.MIN_VALUE;
	}

	@Override
	public void addClause(int[] clause) {
		for (int i = 0; i < clause.length; i++) {
			maxVariable = Math.max(maxVariable, Math.abs(clause[i]));
		}
		clauses.add(clause);
		
	}

	@Override
	public void addAtMostOne(int[] literals) {
		for (int i = 0; i < literals.length; i++) {
			maxVariable = Math.max(maxVariable, Math.abs(literals[i]));
		}
		amoConstraints.add(literals);
	}

	@Override
	public void addDnf(int[][] dnf) {
		for (int i = 0; i < dnf.length; i++) {
			for (int j = 0; j < dnf[i].length; j++) {
				maxVariable = Math.max(maxVariable, Math.abs(dnf[i][j]));
			}
		}
		dnfConstraints.add(dnf);
	}

	@Override
	public void setTimeLimit(long milliseconds) {
		this.timeLimit = milliseconds;
	}

	@Override
	public boolean solve(int[] assumptions) throws TimeoutException {

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future future = executor.submit(() -> {
			resetStrategies();
			formula = new Formula(clauses, amoConstraints, dnfConstraints, maxVariable);
			formula.addAssumptions(assumptions);
			boolean satisfied = mDPPLAlgorithm(formula, firstBranchingDecision);
			return satisfied;
		});

		Boolean satisfied = false;
		try {
			satisfied = timeLimit <= 0 ? (Boolean) future.get() : (Boolean) future.get(timeLimit,
					TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return satisfied;

	}

	@Override
	public boolean getValue(int variable) throws IllegalStateException {
		return formula.getVariables()[Math.abs(variable)] > 0;
	}

	private void resetStrategies() {
		variableSelectionStrategy = new VSIDS();
		restartSchedulingStrategy = new ReluctantDoublingRestartStrategy(512);
		conflictHandlingStrategy = new CDCLConflictHandler();
	}

}
