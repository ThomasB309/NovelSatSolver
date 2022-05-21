package cas.thomas.SolverAlgorithms;

import cas.thomas.ConflictHandling.CDCLConflictHandler;
import cas.thomas.ConflictHandling.ConflictHandlingStrategy;
import cas.thomas.ConflictHandling.DPLLConflictHandler;
import cas.thomas.Evaluation.Statistics;
import cas.thomas.Exceptions.SolverTimeoutException;
import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.Formulas.Formula;
import cas.thomas.RestartHandling.NoRestartsSchedulingStrategy;
import cas.thomas.RestartHandling.ReluctantDoublingRestartStrategy;
import cas.thomas.RestartHandling.RestartSchedulingStrategy;
import cas.thomas.SolutionChecker.SolutionCheckerAMOConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerConjunctiveFormula;
import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDNFConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDisjunctiveConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerFormula;
import cas.thomas.VariableSelection.VSIDS;
import cas.thomas.VariableSelection.VariableSelectionStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class IncrementalCdclSolver extends mDPLL implements IncrementalSatSolver {

    private List<int[]> clauses;
    private List<int[]> amoConstraints;
    private List<int[][]> dnfConstraints;
    private int maxVariable;
    private Formula formula;
    private SolutionCheckerFormula solutionCheckerFormula;

    public IncrementalCdclSolver(VariableSelectionStrategy variableSelectionStrategy,
                                 ConflictHandlingStrategy conflictHandlingStrategy,
                                 RestartSchedulingStrategy restartSchedulingStrategy, boolean phaseSaving,
                                 boolean firstBranchingDecision) {
        super(variableSelectionStrategy, conflictHandlingStrategy, restartSchedulingStrategy, phaseSaving,
                firstBranchingDecision, 0);
        clauses = new ArrayList<>();
        amoConstraints = new ArrayList<>();
        dnfConstraints = new ArrayList<>();
        maxVariable = Integer.MIN_VALUE;
    }

    public IncrementalCdclSolver(VariableSelectionStrategy variableSelectionStrategy,
                                 ConflictHandlingStrategy conflictHandlingStrategy,
                                 RestartSchedulingStrategy restartSchedulingStrategy, boolean phaseSaving,
                                 boolean firstBranchingDecision, long timeout) {
        super(variableSelectionStrategy, conflictHandlingStrategy, restartSchedulingStrategy, phaseSaving,
                firstBranchingDecision, timeout);
        clauses = new ArrayList<>();
        amoConstraints = new ArrayList<>();
        dnfConstraints = new ArrayList<>();
        maxVariable = Integer.MIN_VALUE;
    }

    public IncrementalCdclSolver() {
        super(new VSIDS(), new DPLLConflictHandler(), new NoRestartsSchedulingStrategy(512), true, true,0);
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
        this.timeout = milliseconds;
    }

    @Override
    public boolean solve(int[] assumptions) throws SolverTimeoutException {

        boolean satisfied = false;
        try {
            formula = new Formula(clauses, amoConstraints, dnfConstraints, maxVariable);
            formula.addAssumptions(assumptions);
            satisfied = mDPPLAlgorithm(formula, firstBranchingDecision);

            if (unkown) {
                throw new SolverTimeoutException();
            }

        } catch (UnitLiteralConflictException e) {
            return false;
        }

        assert (satisfied == isCorrect(formula.getVariablesForSolutionChecker(), assumptions,
                formula.getNumberOfVariables()));

        resetStrategies();
        return satisfied;


    }

    @Override
    public boolean getValue(int variable) throws IllegalStateException {
        return formula.getVariables()[Math.abs(variable)] > 0;
    }

    private void resetStrategies() {
        variableSelectionStrategy = new VSIDS();
        restartSchedulingStrategy = new NoRestartsSchedulingStrategy(512);
        conflictHandlingStrategy = new DPLLConflictHandler();
    }

    public boolean isCorrect(int[]solution, int[] assumptions, int numberOfVariables) {
        List<SolutionCheckerConstraint> constraints = new ArrayList<>();
        for (int[] clause : clauses) {
            constraints.add(new SolutionCheckerDisjunctiveConstraint(clause));
        }

        for (int assumption : assumptions) {
            constraints.add(new SolutionCheckerDisjunctiveConstraint(new int[]{assumption}));
        }

        for (int[] amo : amoConstraints) {
            constraints.add(new SolutionCheckerAMOConstraint(amo));
        }

        for (int[][] dnf : dnfConstraints) {
            constraints.add(new SolutionCheckerDNFConstraint(dnf));
        }

        return new SolutionCheckerConjunctiveFormula(constraints.toArray(SolutionCheckerConstraint[]::new), numberOfVariables).isTrue(solution);
    }

    public boolean isUnkown() {
        return unkown;
    }

}
