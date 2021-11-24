package cas.thomas.ConflictHandling;

import cas.thomas.Formulas.Formula;
import cas.thomas.utils.IntegerStack;

public interface ConflictHandlingStrategy {

    boolean handleConflict(IntegerStack trail, Formula formula, boolean branchingDecision,
                           int[] variableDecisionLevel);


}
