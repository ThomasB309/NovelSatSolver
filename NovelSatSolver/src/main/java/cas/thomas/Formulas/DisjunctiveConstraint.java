package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDisjunctiveConstraint;
import cas.thomas.utils.IntegerArrayQueue;

import java.util.List;

public class DisjunctiveConstraint extends Constraint {

    public DisjunctiveConstraint(int[] literals, List<Constraint>[] positivelyWatchedList,
                                 List<Constraint>[] negativelyWatchedList) {
        super(literals);

        assert (literals.length >= 1);

        assignWatchedLiteralsToWatchList(positivelyWatchedList, negativelyWatchedList);
    }


    @Override
    public boolean propagate(int propagatedLiteral, int[] variableAssignments, IntegerArrayQueue unitLiterals,
                             List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched,
                             Constraint[] reasonClauses) {



        int firstWatchedLiteral = literals[0];
        int firstWatchedLiteralAbsoluteValue = Math.abs(firstWatchedLiteral);

        if (literals.length == 1) {
            propagateIfConstraintHasOnlyOneLiteral(variableAssignments, firstWatchedLiteralAbsoluteValue,
                    firstWatchedLiteral, unitLiterals,reasonClauses);
            return true;
        }


        int secondWatchedLiteral = literals[1];
        int secondWatchedLiteralAbsoluteValue = Math.abs(secondWatchedLiteral);

        boolean unitPropagation = true;

        if (firstWatchedLiteral == -propagatedLiteral) {
            unitPropagation = propagateWatchedLiteral(variableAssignments, positivelyWatched, negativelyWatched,
                     true, secondWatchedLiteral, secondWatchedLiteralAbsoluteValue, unitLiterals,
                    reasonClauses);

        } else if (secondWatchedLiteral == -propagatedLiteral) {
            unitPropagation = propagateWatchedLiteral(variableAssignments, positivelyWatched, negativelyWatched,
                     false, firstWatchedLiteral, firstWatchedLiteralAbsoluteValue, unitLiterals,
                    reasonClauses);
        }

        return unitPropagation;
    }

    @Override
    public SolutionCheckerConstraint getSolutionCheckerConstraint() {
        return new SolutionCheckerDisjunctiveConstraint(literals);
    }


    private void assignWatchedLiteralsToWatchList(List<Constraint>[] positivelyWatchedList,
                                                  List<Constraint>[] negativelyWatchedList) {

        if (literals.length > 0) {
            assignWatchedLiteralToWatchList(true, positivelyWatchedList, negativelyWatchedList);
        }

        if (literals.length > 1) {
            assignWatchedLiteralToWatchList(false, positivelyWatchedList, negativelyWatchedList);
        }
    }

    private void assignWatchedLiteralToWatchList(boolean firstLiteral, List<Constraint>[] positivelyWatchedList,
                                                 List<Constraint>[] negativelyWatchedList) {

        int watchedLiteral = firstLiteral ? literals[0] : literals[1];

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
                                                        int firstWatchedLiteral, IntegerArrayQueue unitLiterals,
                                                        Constraint[] reasonClauses) {
        if (variableAssignments[firstWatchedLiteralAbsoluteValue] * firstWatchedLiteral < 0) {
            hasConflict = true;
            conflictLiteral = firstWatchedLiteral;
        } else if (variableAssignments[firstWatchedLiteralAbsoluteValue] == 0) {
            unitLiterals.offer(firstWatchedLiteral);
            if (reasonClauses[firstWatchedLiteralAbsoluteValue] == null) {
                reasonClauses[firstWatchedLiteralAbsoluteValue] = this;
            }
        }

    }

    private boolean propagateWatchedLiteral(int[] variableAssignments, List<Constraint>[] positivelyWatched,
                                            List<Constraint>[] negativelyWatched,
                                            boolean firstLiteral,
                                            int unitLiteralCandidate, int unitLiteralCandidateAbsoluteValue,
                                            IntegerArrayQueue unitLiterals,
                                            Constraint[] reasonClauses) {

        for (int i = 2; i < literals.length; i++) {
            if (!checkIfLiteralIsFalse(literals[i], variableAssignments)) {

                int nextWatchedLiteral = literals[i];
                if (firstLiteral) {
                    literals[i] = literals[0];
                    literals[0] = nextWatchedLiteral;
                    assignWatchedLiteralToWatchList(true, positivelyWatched, negativelyWatched);
                } else {
                    literals[i] = literals[1];
                    literals[1] = nextWatchedLiteral;
                    assignWatchedLiteralToWatchList(false, positivelyWatched, negativelyWatched);
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
            unitLiterals.offer(unitLiteralCandidate);
            if (reasonClauses[unitLiteralCandidateAbsoluteValue] == null) {
                reasonClauses[unitLiteralCandidateAbsoluteValue] = this;
            }
        }

        return true;
    }


}
