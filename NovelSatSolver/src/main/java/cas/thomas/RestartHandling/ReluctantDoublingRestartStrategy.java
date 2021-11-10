package cas.thomas.RestartHandling;

import cas.thomas.Formulas.Formula;
import cas.thomas.utils.Pair;

import java.util.Deque;
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
    public void handleRestart(Deque<Integer> trail, Formula formula) {

        conflictCounter++;

        if (conflictCounter < currenctConflictLimit) {
            return;
        }

        setNextReluctantDoublingPair();

        currenctConflictLimit = numberOfInitialConflicts * vn;

        restart(trail, formula);


    }

    private void setNextReluctantDoublingPair() {
        Integer u = ((un & -un) == vn) ? un + 1 : un;
        Integer v = ((un & -un) == vn) ? 1 : 2 * vn;

        un = u;
        vn = v;
    }

    private void restart(Deque<Integer> trail, Formula formula) {
        for (Iterator<Integer> iterator = trail.iterator(); iterator.hasNext();) {
            int currentLiteral = iterator.next();

            formula.unassignVariable(currentLiteral);
            iterator.remove();

        }

        formula.removeReasonClauses();
        conflictCounter = 0;
    }
}
