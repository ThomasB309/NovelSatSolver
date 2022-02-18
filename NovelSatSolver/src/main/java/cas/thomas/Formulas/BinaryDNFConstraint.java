package cas.thomas.Formulas;

import cas.thomas.utils.IntegerArrayQueue;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class BinaryDNFConstraint extends DNFConstraint {

    public BinaryDNFConstraint(int[][] terms, List<Constraint>[] positivelyWatchedList, List<Constraint>[] negativelyWatchedList, IntegerArrayQueue unitLiterals, int[] variableAssignment, int[] unitLiteralState, int[] decisionLevelOfVariables) {
        super(terms, positivelyWatchedList, negativelyWatchedList, unitLiterals, variableAssignment, unitLiteralState, decisionLevelOfVariables);
    }

    public BinaryDNFConstraint(int[][] terms, List<Constraint>[] positivelyWatchedList,
                               List<Constraint>[] negativelyWatchedList, int[] variableAssignment) {

        int[] sharedLiterals = new int[variableAssignment.length];
        unitLiteralsPropagatedDuringInitialization = new HashSet<>();
        this.terms = terms;

        for (int i = 0; i < terms[0].length; i++) {
            int currentLiteral = terms[0][i];
            sharedLiterals[Math.abs(currentLiteral)] = currentLiteral;
        }

        for (int i = 0; i < terms[1].length; i++) {
            int currentLiteral = terms[1][i];
            if (sharedLiterals[Math.abs(currentLiteral)] != 0) {
                unitLiteralsPropagatedDuringInitialization.add(currentLiteral);
            }
        }

        assignWatchedLiterals(positivelyWatchedList, negativelyWatchedList);


    }

    @Override
    public boolean propagate(int propagatedLiteral, int[] variableAssignments, int[] unitLiteralState, IntegerArrayQueue unitLiterals, List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched, Constraint[] reasonClauses) {

        if (isLiteralInTerm(true, propagatedLiteral)) {
            propagateBinaryTerm(false, variableAssignments,unitLiteralState,unitLiterals,reasonClauses);
        } else {
            propagateBinaryTerm(true, variableAssignments,unitLiteralState,unitLiterals,reasonClauses);
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

    private void assignWatchedLiterals(List<Constraint>[] positivelyWatchedList,
                                       List<Constraint>[] negativelyWatchedList) {

        for (int i = 0; i < terms.length; i++){
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
    public boolean isStillWatched(int literal) {
        return true;
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
            } else if (variableAssignments[currentLiteralAbsoluteValue] == 0) {
                unitLiterals.offer(currentLiteral);
                unitLiteralState[currentLiteralAbsoluteValue] = currentLiteral < 0 ? -1 : 1;
                if (reasonClauses[currentLiteralAbsoluteValue] == null) {
                    reasonClauses[currentLiteralAbsoluteValue] = this;
                }
            }
        }
    }

    public void backtrack(int variable, int[] unitLiteralState, Set<Integer> unitLiteralsBeforePropagation,
                          List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched,
                          ListIterator<Constraint> listIterator) {
        return;
    }
}
