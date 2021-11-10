package cas.thomas.ConflictHandling;

import cas.thomas.Formulas.Formula;

import java.util.Deque;
import java.util.Iterator;

public class DPLLConflictHandler implements ConflictHandlingStrategy {


    @Override
    public boolean handleConflict(Deque<Integer> trail, Formula formula, boolean branchingDecision) {
        int nextLiteral;
        if ((nextLiteral = findLastLiteralNotTriedBothValues(trail, formula)) == -1) {
            return false;
        }

        trail.push(-trail.pop());
        formula.propagateAfterSwappingVariableAssigment(Math.abs(nextLiteral), !branchingDecision);

        return true;
    }

    private int findLastLiteralNotTriedBothValues(Deque<Integer> trail, Formula formula) {
        Iterator<Integer> trailIterator = trail.iterator();

        while (trailIterator.hasNext()) {
            int nextLiteral = trailIterator.next();

            if (nextLiteral > 0) {
                return nextLiteral;
            }

            formula.unassignVariable(nextLiteral);
            trailIterator.remove();

        }

        return -1;
    }
}
