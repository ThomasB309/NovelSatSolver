package cas.thomas.ConflictHandling;

import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.Formulas.Formula;
import cas.thomas.VariableSelection.VariableSelectionStrategy;
import cas.thomas.utils.IntegerStack;

public class DPLLConflictHandler implements ConflictHandlingStrategy {


    @Override
    public boolean handleConflict(IntegerStack trail, Formula formula, boolean branchingDecision,
                                  int[] variableDecisionLevel, VariableSelectionStrategy variableSelectionStrategy) throws UnitLiteralConflictException {

        int nextLiteral;
        if ((nextLiteral = findLastLiteralNotTriedBothValues(trail, formula, variableDecisionLevel, variableSelectionStrategy)) == -1) {
            return false;
        }

        formula.emptyUnitLiterals();
        formula.resetConflictState();

        int nextLiteralAbsoluteValue = Math.abs(nextLiteral);

        formula.setCurrentDecisionLevel(variableDecisionLevel[nextLiteralAbsoluteValue]);
        trail.push(-trail.pop());
        formula.propagateAfterSwappingVariableAssigment(nextLiteralAbsoluteValue, !branchingDecision);

        return true;
    }

    private int findLastLiteralNotTriedBothValues(IntegerStack trail, Formula formula, int[] variableDecisionLevel,
                                                  VariableSelectionStrategy variableSelectionStrategy) {
        while (trail.hasNext()) {
            int nextLiteral = trail.peekFirst();


            if (nextLiteral > 0) {
                return nextLiteral;
            }

            variableDecisionLevel[Math.abs(nextLiteral)] = 0;
            trail.pop();
            formula.unassignVariable(nextLiteral);
            variableSelectionStrategy.addUnassignedVariable(Math.abs(nextLiteral));
        }

        return -1;
    }
}
