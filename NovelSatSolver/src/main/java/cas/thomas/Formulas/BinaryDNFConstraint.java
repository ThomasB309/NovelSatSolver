package cas.thomas.Formulas;

import cas.thomas.utils.IntegerArrayQueue;

import java.util.HashSet;
import java.util.List;

public class BinaryDNFConstraint extends DNFConstraint {

    public BinaryDNFConstraint(int[][] terms, List<Constraint>[] positivelyWatchedList,
                               List<Constraint>[] negativelyWatchedList, int[] variableAssignment) {

        int[] sharedLiterals = new int[variableAssignment.length];
        unitLiteralsPropagatedDuringInitialization = new HashSet<>();
        this.terms = terms;

        findLiteralsSharedByBothTerms(terms, sharedLiterals);

        assignWatchedLiterals(positivelyWatchedList, negativelyWatchedList);


    }

    private void findLiteralsSharedByBothTerms(int[][] terms, int[] sharedLiterals) {
        for (int i = 0; i < terms[0].length; i++) {
            int currentLiteral = terms[0][i];
            sharedLiterals[Math.abs(currentLiteral)] = currentLiteral;
        }

        for (int i = 0; i < terms[1].length; i++) {
            int currentLiteral = terms[1][i];
            if (sharedLiterals[Math.abs(currentLiteral)] == currentLiteral) {
                unitLiteralsPropagatedDuringInitialization.add(currentLiteral);
            }
        }
    }

    private void assignWatchedLiterals(List<Constraint>[] positivelyWatchedList,
                                       List<Constraint>[] negativelyWatchedList) {

        for (int i = 0; i < terms.length; i++) {
            for (int j = 0; j < terms[i].length; j++) {
                int watchedLiteral = terms[i][j];
                if (watchedLiteral < 0) {
                    negativelyWatchedList[Math.abs(watchedLiteral)].add(this);
                } else {
                    positivelyWatchedList[watchedLiteral].add(this);
                }
            }
        }
    }

    @Override
    public boolean propagate(int propagatedLiteral, int[] variableAssignments, int[] unitLiteralState, IntegerArrayQueue unitLiterals, List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched, Constraint[] reasonClauses) {

        if (isLiteralInTerm(true, propagatedLiteral)) {
            propagateBinaryTerm(false, variableAssignments, unitLiteralState, unitLiterals, reasonClauses);
        } else {
            propagateBinaryTerm(true, variableAssignments, unitLiteralState, unitLiterals, reasonClauses);
        }

        return true;

    }

    private boolean isLiteralInTerm(boolean firstTerm, int literal) {

        int[] term = firstTerm ? terms[0] : terms[1];

        for (int i = 0; i < term.length; i++) {
            if (term[i] == -literal) {
                return true;
            }
        }
        return false;
    }

    private void propagateBinaryTerm(boolean firstTerm, int[] variableAssignments, int[] unitLiteralState,
                                     IntegerArrayQueue unitLiterals, Constraint[] reasonClauses) {

        int[] term = firstTerm ? terms[0] : terms[1];

        for (int i = 0; i < term.length; i++) {
            int currentLiteral = term[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);
            if (variableAssignments[currentLiteralAbsoluteValue] * currentLiteral < 0 || unitLiteralState[currentLiteralAbsoluteValue] * currentLiteral < 0) {
                hasConflict = true;
                conflictLiteral = currentLiteral;
                return;
            } else if (isNeededForUnitPropagation(currentLiteral, variableAssignments, unitLiteralState)) {
                unitLiterals.offer(currentLiteral);
                unitLiteralState[currentLiteralAbsoluteValue] = currentLiteral < 0 ? -1 : 1;
                if (reasonClauses[currentLiteralAbsoluteValue] == null) {
                    reasonClauses[currentLiteralAbsoluteValue] = this;
                }
            }
        }
    }

    @Override
    public int getNeededDecisionLevel(int[] decisionLevelOfVariables, int[] variables) {
        int decisionLevel = Integer.MAX_VALUE;

        for (int i = 0; i < terms[0].length; i++) {
            decisionLevel = Math.min(decisionLevel, decisionLevelOfVariables[Math.abs(terms[0][i])]);
        }

        for (int i = 0; i < terms[1].length; i++) {
            decisionLevel = Math.min(decisionLevel, decisionLevelOfVariables[Math.abs(terms[1][i])]);
        }

        return decisionLevel;

    }

    @Override
    public boolean isStillWatched(int literal, int[] variables) {
        return true;
    }

    @Override
    public void backtrack(int variable, int[] variableAssignments) {
        return;
    }

    private boolean isNeededForUnitPropagation(final int literal, final int[] variables, final int[] unitLiteralState) {
        final int literalAbsoluteValue = Math.abs(literal);
        return variables[literalAbsoluteValue] * literal == 0 && unitLiteralState[literalAbsoluteValue] == 0;
    }

}
