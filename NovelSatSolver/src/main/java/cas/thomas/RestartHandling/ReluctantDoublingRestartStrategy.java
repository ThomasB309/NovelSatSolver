package cas.thomas.RestartHandling;

import cas.thomas.Formulas.Formula;
import cas.thomas.utils.IntegerStack;

import java.util.Iterator;

public class ReluctantDoublingRestartStrategy extends RestartSchedulingStrategy {


    private int un;
    private int vn;
    private int currenctConflictLimit;

    public ReluctantDoublingRestartStrategy(int numberOfInitialConflicts) {
        super(numberOfInitialConflicts);
        currenctConflictLimit = numberOfInitialConflicts;
        un = 1;
        vn = 1;
    }

    @Override
    public boolean handleRestart(IntegerStack trail, Formula formula) {

        conflictCounter++;

        if (conflictCounter < currenctConflictLimit) {
            return false;
        }

        setNextReluctantDoublingPair();

        currenctConflictLimit = numberOfInitialConflicts * vn;

        restart(trail, formula);

        return true;
    }

    private void setNextReluctantDoublingPair() {
        Integer u = ((un & -un) == vn) ? un + 1 : un;
        Integer v = ((un & -un) == vn) ? 1 : 2 * vn;

        un = u;
        vn = v;
    }

    private void restart(IntegerStack trail, Formula formula) {
        while (trail.hasNext()) {
            int currentLiteral = trail.pop();
            formula.unassignVariable(currentLiteral);
        }

        formula.setCurrentDecisionLevel(0);
        formula.removeReasonClauses();
        conflictCounter = 0;
    }
}
