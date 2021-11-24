package cas.thomas.RestartHandling;

import cas.thomas.Formulas.Formula;
import cas.thomas.utils.IntegerStack;

public class NoRestartsSchedulingStrategy extends RestartSchedulingStrategy {

    public NoRestartsSchedulingStrategy(int numberOfInitialConflicts) {
        super(numberOfInitialConflicts);
    }

    @Override
    public boolean handleRestart(IntegerStack trail, Formula formula) {
        return false;
    }

}
