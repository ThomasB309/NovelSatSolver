package cas.thomas.RestartHandling;

import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.Formulas.Formula;
import cas.thomas.VariableSelection.VariableSelectionStrategy;
import cas.thomas.utils.IntegerStack;

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
    public boolean handleRestart(IntegerStack trail, Formula formula, VariableSelectionStrategy variableSelectionStrategy) {

        conflictCounter++;

        if (conflictCounter < currenctConflictLimit) {
            return false;
        }

        setNextReluctantDoublingPair();

        currenctConflictLimit = numberOfInitialConflicts * vn;

        restart(trail, formula, variableSelectionStrategy);

        return true;
    }

    private void setNextReluctantDoublingPair() {
        Integer u = ((un & -un) == vn) ? un + 1 : un;
        Integer v = ((un & -un) == vn) ? 1 : 2 * vn;

        un = u;
        vn = v;
    }

    private void restart(IntegerStack trail, Formula formula, VariableSelectionStrategy variableSelectionStrategy) {
        while (trail.hasNext()) {
            int currentLiteral = trail.pop();
            formula.unassignVariable(currentLiteral);
            variableSelectionStrategy.addUnassignedVariable(Math.abs(currentLiteral));
        }

        formula.setCurrentDecisionLevel(0);
        formula.emptyUnitLiterals();
        formula.resetConflictState();
        try {
            formula.setUnitLiteralsBeforePropagation();
        } catch (UnitLiteralConflictException e) {
            e.printStackTrace();
        }
        conflictCounter = 0;
    }
}
