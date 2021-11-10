package cas.thomas.RestartHandling;

import cas.thomas.Formulas.Formula;

import java.util.Deque;

public class NoRestartsSchedulingStrategy extends RestartSchedulingStrategy {

    public NoRestartsSchedulingStrategy(int numberOfInitialConflicts) {
        super(numberOfInitialConflicts);
    }

    @Override
    public void handleRestart(Deque<Integer> trail, Formula formula) {
        return;
    }

}
