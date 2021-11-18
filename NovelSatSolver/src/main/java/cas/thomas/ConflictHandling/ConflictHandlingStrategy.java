package cas.thomas.ConflictHandling;

import cas.thomas.Formulas.Formula;

import java.util.Deque;

public interface ConflictHandlingStrategy {

    boolean handleConflict(Deque<Integer> trail, Formula formula, boolean branchingDecision,
                                  int[] variableDecisionLevel);


}
