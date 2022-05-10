package cas.thomas.SolverAlgorithms;

import cas.thomas.ConflictHandling.ConflictHandlingStrategy;
import cas.thomas.Evaluation.Statistics;
import cas.thomas.Formulas.Formula;
import cas.thomas.RestartHandling.RestartSchedulingStrategy;
import cas.thomas.VariableSelection.VariableSelectionStrategy;

public abstract class SolverAlgorithm {

    protected VariableSelectionStrategy variableSelectionStrategy;
    protected ConflictHandlingStrategy conflictHandlingStrategy;
    protected RestartSchedulingStrategy restartSchedulingStrategy;
    protected boolean phaseSaving;
    protected boolean firstBranchingDecision;
    protected int firstBranchingDecisionInteger;
    protected long timeout;
    protected boolean unkown;
    protected long branchings;
    protected long conflicts;
    protected long unitPropagations;
    protected long restarts;

    public SolverAlgorithm(VariableSelectionStrategy variableSelectionStrategy,
                           ConflictHandlingStrategy conflictHandlingStrategy,
                           RestartSchedulingStrategy restartSchedulingStrategy, boolean phaseSaving,
                           boolean firstBranchingDecision, long timeout) {
        this.variableSelectionStrategy = variableSelectionStrategy;
        this.conflictHandlingStrategy = conflictHandlingStrategy;
        this.restartSchedulingStrategy = restartSchedulingStrategy;
        this.phaseSaving = phaseSaving;
        this.firstBranchingDecision = firstBranchingDecision;
        this.firstBranchingDecisionInteger = firstBranchingDecision ? 1 : -1;
        this.timeout = timeout;
        this.unkown = false;

    }

    public SolverAlgorithm(VariableSelectionStrategy variableSelectionStrategy,
                           ConflictHandlingStrategy conflictHandlingStrategy,
                           RestartSchedulingStrategy restartSchedulingStrategy, boolean phaseSaving,
                           boolean firstBranchingDecision) {
        this(variableSelectionStrategy,conflictHandlingStrategy,restartSchedulingStrategy,phaseSaving,
                firstBranchingDecision, 0);
        this.unkown = false;

    }

    public abstract String solve(Formula formula);



    public Statistics getStatistics() {
        Statistics statistics = new Statistics();

        statistics.setConflicts(conflicts);
        statistics.setDecisions(branchings);
        statistics.setPropagations(unitPropagations);

        return statistics;
    }
}
