package cas.thomas.SolverAlgorithms;

import cas.thomas.ConflictHandling.ConflictHandlingStrategy;
import cas.thomas.Formulas.Constraint;
import cas.thomas.Formulas.Formula;
import cas.thomas.RestartHandling.RestartSchedulingStrategy;
import cas.thomas.VariableSelection.VariableSelectionStrategy;
import cas.thomas.utils.Pair;

public abstract class SolverAlgorithm {

    protected VariableSelectionStrategy variableSelectionStrategy;
    protected ConflictHandlingStrategy conflictHandlingStrategy;
    protected RestartSchedulingStrategy restartSchedulingStrategy;
    protected boolean phaseSaving;
    protected boolean firstBranchingDecision;
    protected int firstBranchingDecisionInteger;

    public SolverAlgorithm(VariableSelectionStrategy variableSelectionStrategy,
                           ConflictHandlingStrategy conflictHandlingStrategy,
                           RestartSchedulingStrategy restartSchedulingStrategy, boolean phaseSaving,
                           boolean firstBranchingDecision) {
        this.variableSelectionStrategy = variableSelectionStrategy;
        this.conflictHandlingStrategy = conflictHandlingStrategy;
        this.restartSchedulingStrategy = restartSchedulingStrategy;
        this.phaseSaving = phaseSaving;
        this.firstBranchingDecision = firstBranchingDecision;
        this.firstBranchingDecisionInteger = firstBranchingDecision ? 1 : -1;
    }

    public abstract String solve(Formula formula);
}
