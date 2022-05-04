package cas.thomas.RestartHandling;

import cas.thomas.Formulas.Formula;
import cas.thomas.VariableSelection.VariableSelectionStrategy;
import cas.thomas.utils.IntegerStack;

public abstract class RestartSchedulingStrategy {

    protected int numberOfInitialConflicts;
    protected int conflictCounter;

    public RestartSchedulingStrategy(int numberOfInitialConflicts) {
        this.numberOfInitialConflicts = numberOfInitialConflicts;
        this.conflictCounter = 0;
    }

    public abstract boolean handleRestart(IntegerStack trail, Formula formula, VariableSelectionStrategy variableSelectionStrategy);
}
