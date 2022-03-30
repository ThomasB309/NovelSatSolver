package cas.thomas.Formulas;

import cas.thomas.utils.IntegerArrayQueue;

import java.util.List;

public class BinaryDisjunctiveConstraint extends DisjunctiveConstraint {

    public BinaryDisjunctiveConstraint(int[] literals, List<Constraint>[] positivelyWatchedList, List<Constraint>[] negativelyWatchedList) {
        super(literals, positivelyWatchedList, negativelyWatchedList);
    }

    @Override
    public boolean propagate(int propagatedLiteral, int[] variableAssignments, int[] unitLiteralState,
                             IntegerArrayQueue unitLiterals,
                             List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched,
                             Constraint[] reasonClauses) {

        if (propagatedLiteral == -literals[0]) {
            propagateBinaryLiteral(literals[1], variableAssignments, unitLiteralState, unitLiterals,
                    reasonClauses);
        } else {
            propagateBinaryLiteral(literals[0], variableAssignments, unitLiteralState, unitLiterals,
                    reasonClauses);
        }

        return true;
    }

    private void propagateBinaryLiteral(int literal, int[] variableAssignments, int[] unitLiteralState,
                                        IntegerArrayQueue unitLiterals, Constraint[] reasonClauses) {
        int literalAbsoluteValue = Math.abs(literal);
        if (variableAssignments[literalAbsoluteValue] * literal < 0 || unitLiteralState[literalAbsoluteValue] * literal < 0) {
            hasConflict = true;
            conflictLiteral = literal;
            return;
        } else if (isNeededForUnitPropagation(literal, variableAssignments, unitLiteralState)) {
            unitLiterals.offer(literal);
            unitLiteralState[literalAbsoluteValue] = literal < 0 ? -1 : 1;
            if (reasonClauses[literalAbsoluteValue] == null) {
                reasonClauses[literalAbsoluteValue] = this;
            }
        }
    }
}
