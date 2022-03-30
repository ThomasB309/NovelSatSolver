package cas.thomas.ConflictHandling;

import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.Formulas.Formula;
import cas.thomas.VariableSelection.VariableSelectionStrategy;
import cas.thomas.utils.IntegerStack;

public interface ConflictHandlingStrategy {

    boolean handleConflict(IntegerStack trail, Formula formula, boolean branchingDecision,
                           int[] variableDecisionLevel, VariableSelectionStrategy variableSelectionStrategy) throws UnitLiteralConflictException;


}
