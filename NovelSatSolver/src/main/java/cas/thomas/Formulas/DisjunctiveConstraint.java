package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDisjunctiveConstraint;

import java.util.List;

public class DisjunctiveConstraint extends Constraint {

    private int firstWatchedIndex;
    private int secondWatchedIndex;

    public DisjunctiveConstraint(int[] literals, List<Constraint>[] positivelyWatchedList,
                                 List<Constraint>[] negativelyWatchedList) {
        super(literals);

        assert (literals.length >= 1);

        assignWatchedIndicesAndLiterals(literals, positivelyWatchedList, negativelyWatchedList);
        assignWatchedLiteralsToWatchList(positivelyWatchedList, negativelyWatchedList);
    }


    @Override
    public boolean propagate(int propagatedLiteral, int[] variableAssignments, List<Integer> unitLiterals,
                             List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched,
                             Constraint[] reasonClauses) {



        int firstWatchedLiteral = literals[firstWatchedIndex];
        int firstWatchedLiteralAbsoluteValue = Math.abs(firstWatchedLiteral);

        if (literals.length == 1) {
            propagateIfConstraintHasOnlyOneLiteral(variableAssignments, firstWatchedLiteralAbsoluteValue,
                    firstWatchedLiteral, unitLiterals,reasonClauses);
            return true;
        }


        int secondWatchedLiteral = literals[secondWatchedIndex];
        int secondWatchedLiteralAbsoluteValue = Math.abs(secondWatchedLiteral);

        boolean unitPropagation = true;

        if (firstWatchedLiteral == -propagatedLiteral) {
            unitPropagation = propagateWatchedLiteral(variableAssignments, positivelyWatched, negativelyWatched,
                     firstWatchedIndex, secondWatchedLiteral, secondWatchedLiteralAbsoluteValue, unitLiterals,
                    reasonClauses);

        } else if (secondWatchedLiteral == -propagatedLiteral) {
            unitPropagation = propagateWatchedLiteral(variableAssignments, positivelyWatched, negativelyWatched,
                     secondWatchedIndex, firstWatchedLiteral, firstWatchedLiteralAbsoluteValue, unitLiterals,
                    reasonClauses);
        }

        return unitPropagation;
    }

    @Override
    public SolutionCheckerConstraint getSolutionCheckerConstraint() {
        return new SolutionCheckerDisjunctiveConstraint(literals);
    }



    private void assignWatchedIndicesAndLiterals(int[] literals, List<Constraint>[] positivelyWatchedList,
                                                 List<Constraint>[] negativelyWatchedList) {
        if (literals.length > 1) {
            firstWatchedIndex = 0;
            secondWatchedIndex = 1;

        } else if (literals.length == 1) {
            firstWatchedIndex = 0;
            secondWatchedIndex = -1;

        } else {
            firstWatchedIndex = -1;
            secondWatchedIndex = -1;
        }
    }

    private void assignWatchedLiteralsToWatchList(List<Constraint>[] positivelyWatchedList,
                                                  List<Constraint>[] negativelyWatchedList) {

        if (firstWatchedIndex >= 0) {
            assignWatchedLiteralToWatchList(firstWatchedIndex, positivelyWatchedList, negativelyWatchedList);
        }

        if (secondWatchedIndex >= 0) {
            assignWatchedLiteralToWatchList(secondWatchedIndex, positivelyWatchedList, negativelyWatchedList);
        }
    }

    private void assignWatchedLiteralToWatchList(int index, List<Constraint>[] positivelyWatchedList,
                                                 List<Constraint>[] negativelyWatchedList) {

        int watchedLiteral = literals[index];

        if (watchedLiteral < 0) {
            negativelyWatchedList[Math.abs(watchedLiteral)].add(this);
        } else {
            positivelyWatchedList[watchedLiteral].add(this);
        }
    }

    private boolean isNeededForUnitPropagation(int literal, int[] variables) {
        if (variables[Math.abs(literal)] * literal == 0) {
            return true;
        }

        return false;
    }


    private boolean checkIfLiteralIsFalse(int literal, int[] variables) {
        if (variables[Math.abs(literal)] * literal < 0) {
            return true;
        }

        return false;
    }

    private void propagateIfConstraintHasOnlyOneLiteral(int[] variableAssignments,
                                                           int firstWatchedLiteralAbsoluteValue,
                                                           int firstWatchedLiteral, List<Integer> unitLiterals,
                                                           Constraint[] reasonClauses) {
        if (variableAssignments[firstWatchedLiteralAbsoluteValue] * firstWatchedLiteral < 0) {
            hasConflict = true;
            conflictLiteral = firstWatchedLiteral;
        }

        unitLiterals.add(firstWatchedLiteral);
        reasonClauses[firstWatchedLiteralAbsoluteValue] = this;

    }

    private boolean propagateWatchedLiteral(int[] variableAssignments, List<Constraint>[] positivelyWatched,
                                            List<Constraint>[] negativelyWatched,
                                            int indexOfVariableThatTurnedFalse,
                                            int unitLiteralCandidate, int unitLiteralCandidateAbsoluteValue,
                                            List<Integer> unitLiterals,
                                            Constraint[] reasonClauses) {

        for (int i = 0; i < literals.length; i++) {
            if (i != firstWatchedIndex && i != secondWatchedIndex && !checkIfLiteralIsFalse(literals[i], variableAssignments)) {

                if (firstWatchedIndex == indexOfVariableThatTurnedFalse) {
                    firstWatchedIndex = i;
                    assignWatchedLiteralToWatchList(firstWatchedIndex, positivelyWatched, negativelyWatched);
                } else {
                    secondWatchedIndex = i;
                    assignWatchedLiteralToWatchList(secondWatchedIndex, positivelyWatched, negativelyWatched);
                }

                return false;
            }
        }

        if (variableAssignments[unitLiteralCandidateAbsoluteValue] * unitLiteralCandidate < 0) {
            hasConflict = true;
            conflictLiteral = unitLiteralCandidate;
            return true;
        }

        if (isNeededForUnitPropagation(unitLiteralCandidateAbsoluteValue, variableAssignments)) {
            unitLiterals.add(unitLiteralCandidate);
            if (reasonClauses[unitLiteralCandidateAbsoluteValue] == null) {
                reasonClauses[unitLiteralCandidateAbsoluteValue] = this;
            }
        }

        return true;
    }


}
