package cas.thomas.RestartHandling;

import cas.thomas.Formulas.Formula;

import java.util.Deque;

public abstract class RestartSchedulingStrategy {

    protected int numberOfInitialConflicts;
    protected int conflictCounter;

    public RestartSchedulingStrategy(int numberOfInitialConflicts) {
        this.numberOfInitialConflicts = numberOfInitialConflicts;
        this.conflictCounter = 0;
    }

    public abstract void handleRestart(Deque<Integer> trail, Formula formula);
}
