package cas.thomas.ConflictHandling;

import cas.thomas.Formulas.Formula;
import cas.thomas.VariableSelection.VariableSelectionStrategy;
import cas.thomas.utils.IntegerStack;

import java.util.Iterator;

public class DPLLConflictHandler implements ConflictHandlingStrategy {


    @Override
    public boolean handleConflict(IntegerStack trail, Formula formula, boolean branchingDecision,
                                  int[] variableDecisionLevel, VariableSelectionStrategy variableSelectionStrategy) {

        int nextLiteral;
        if ((nextLiteral = findLastLiteralNotTriedBothValues(trail, formula, variableDecisionLevel)) == -1) {
            return false;
        }

        int nextLiteralAbsoluteValue = Math.abs(nextLiteral);

        formula.setCurrentDecisionLevel(variableDecisionLevel[nextLiteralAbsoluteValue]);
        trail.push(-trail.pop());
        formula.propagateAfterSwappingVariableAssigment(nextLiteralAbsoluteValue, !branchingDecision);

        return true;
    }

    private int findLastLiteralNotTriedBothValues(IntegerStack trail, Formula formula, int[] variableDecisionLevel) {
        while (trail.hasNext()) {
            int nextLiteral = trail.peekFirst();


            if (nextLiteral > 0) {
                return nextLiteral;
            }

            variableDecisionLevel[Math.abs(nextLiteral)] = 0;
            trail.pop();
            formula.unassignVariable(nextLiteral);
        }

        return -1;
    }
}
