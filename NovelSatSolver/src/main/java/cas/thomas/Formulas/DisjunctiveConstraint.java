package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDisjunctiveConstraint;

import java.util.List;

public class DisjunctiveConstraint extends Constraint {

    private int firstWatchedIndex;
    private int secondWatchedIndex;

    public DisjunctiveConstraint(int[] literals, List<Constraint>[] positivelyWatchedList,
                                 List<Constraint>[] negativelyWatchedList) {
        super(literals, positivelyWatchedList, negativelyWatchedList);

        assert (literals.length >= 1);

        assignWatchedIndecesAndLiterals(literals, positivelyWatchedList, negativelyWatchedList);
        assignWatchedLiteralsToWatchList(positivelyWatchedList, negativelyWatchedList);
    }


    @Override
    public boolean propagate(int propagatedLiteral, int[] variableAssignments, List<Integer> unitLiterals,
                             List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched) {

        int firstWatchedLiteral = literals[firstWatchedIndex];
        int secondWatchedLiteral = literals[secondWatchedIndex];

        if (firstWatchedLiteral == -propagatedLiteral) {
            for (int i = 0; i < literals.length; i++) {
                if (i != firstWatchedIndex && i != secondWatchedIndex && !checkIfLiteralIsFalse(literals[i], variableAssignments)) {
                    firstWatchedIndex = i;

                    assignWatchedLiteralToWatchList(firstWatchedIndex, positivelyWatched, negativelyWatched);

                    return false;
                }
            }

            if (isNeededForUnitPropagation(secondWatchedLiteral, variableAssignments)) {
                unitLiterals.add(secondWatchedLiteral);
            }

        } else if (secondWatchedLiteral == -propagatedLiteral) {
            for (int i = 0; i < literals.length; i++) {
                if (i != firstWatchedIndex && i != secondWatchedIndex && !checkIfLiteralIsFalse(literals[i], variableAssignments)) {
                    secondWatchedIndex = i;

                    assignWatchedLiteralToWatchList(secondWatchedIndex, positivelyWatched, negativelyWatched);

                    return false;
                }
            }

            if (isNeededForUnitPropagation(firstWatchedLiteral, variableAssignments)) {
                unitLiterals.add(firstWatchedLiteral);
            }
        }

        return true;
    }

    @Override
    protected int[] getWatchedLiterals() {
        return new int[]{literals[firstWatchedIndex], literals[secondWatchedIndex]};
    }

    @Override
    public SolutionCheckerConstraint getSolutionCheckerConstraint() {
        return new SolutionCheckerDisjunctiveConstraint(literals);
    }


    private void assignWatchedIndecesAndLiterals(int[] literals, List<Constraint>[] positivelyWatchedList,
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
        if (variables[Math.abs(literal)] * literal <= 0) {
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


}
