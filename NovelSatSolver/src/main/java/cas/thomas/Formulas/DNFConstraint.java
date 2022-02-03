package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDNFConstraint;
import cas.thomas.utils.IntegerArrayQueue;
import cas.thomas.utils.IntegerHashMap;
import cas.thomas.utils.IntegerStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DNFConstraint extends Constraint {

    private int[][] terms;
    private int[] firstWatchedLiterals;
    private int[] secondWatchedLiterals;

    public DNFConstraint(int[][] terms, List<Constraint>[] positivelyWatchedList,
                         List<Constraint>[] negativelyWatchedList, IntegerArrayQueue unitLiterals, int[] unitLiteralState) {
        super();

        assert(terms.length > 0);
        for (int i = 0; i < terms.length; i++) {
            assert(terms[i].length > 0);
        }

        firstWatchedLiterals = new int[0];
        secondWatchedLiterals = new int[0];
        this.terms = terms;

        assignWatchedLiteralsToWatchList(positivelyWatchedList, negativelyWatchedList);
    }

    @Override
    public boolean propagate(int propagatedLiteral, int[] variableAssignments, int[] unitLiteralState, IntegerArrayQueue unitLiterals, List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched, Constraint[] reasonClauses) {

        if (terms.length == 1) {
           propagateIfConstraintHasOnlyOneTerm(variableAssignments, unitLiteralState, unitLiterals, reasonClauses);
           return true;
        }

        boolean unitPropagation = true;

        if (isLiteralInFirstWatchedTerm(propagatedLiteral)) {
            unitPropagation = propagateWatchedLiteral(variableAssignments, unitLiteralState, positivelyWatched,
                    negativelyWatched,
                    true, unitLiterals,
                    reasonClauses);

        }

        if (isLiteralInSecondWatchedTerm(propagatedLiteral)) {
            unitPropagation = propagateWatchedLiteral(variableAssignments, unitLiteralState, positivelyWatched,
                    negativelyWatched,
                    false, unitLiterals,
                    reasonClauses);
        }

        return unitPropagation;
    }

    @Override
    public SolutionCheckerConstraint getSolutionCheckerConstraint() {
        return new SolutionCheckerDNFConstraint(this.terms);
    }

    @Override
    public List<Constraint> handleConflict(int numberOfVariables, IntegerStack trail, int[] variableDecisionLevel, int[] variablesInvolvedInConflict, Formula formula) {
        int[] literals = this.literals;

        int[] stateOfResolvedVariables = new int[numberOfVariables];

        for (int i = 0; i < terms.length; i++) {
            for (int j = 0; j < terms[i].length; j++) {
                int currentLiteralAbsoluteValue = Math.abs(terms[i][j]);
                stateOfResolvedVariables[currentLiteralAbsoluteValue] = terms[i][j];
                variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;
            }
        }

        trail.prepareIterationWithoutPop();
        while (trail.hasNextWithoutPop()) {
            int literal = trail.peekNextWithoutPop();
            Constraint reasonConstraint = formula.getReasonClauses(literal);

            if (reasonConstraint == null) {
                continue;
            }

            return reasonConstraint.resolveConflict(this, trail,stateOfResolvedVariables, formula, variablesInvolvedInConflict);
        }

        return Arrays.asList();
    }

    @Override
    public List<Constraint> resolveConflict(Constraint conflictConstraint, IntegerStack trail,
                                            int[] stateOfResolvedVariables, Formula formula,
                                            int[] variablesInvolvedInConflict) {
        int[] conflictLiterals = conflictConstraint.getLiterals();
        int[][] reasonTerms = this.terms;
        int conflictLiteral = formula.getConflictLiteral();

        ArrayList<int[]> resolutionTerms = new ArrayList<>();

        for (int i = 0; i < conflictLiterals.length; i++) {
            int currentLiteral = conflictLiterals[i];
            if (currentLiteral != conflictLiteral) {
                resolutionTerms.add(new int[]{currentLiteral});
            }
        }

        for (int i = 0; i < reasonTerms.length; i++) {
            boolean containsConflictLiteral = false;
            for (int j = 0; j < reasonTerms[i].length; j++) {
                if (reasonTerms[i][j] == -conflictLiteral) {
                    containsConflictLiteral = true;
                    break;
                }
            }

            if (!containsConflictLiteral) {
                resolutionTerms.add(reasonTerms[i]);
            }
        }


        return Arrays.asList(formula.addDNFConstraints(resolutionTerms.toArray(int[][]::new)));

    }

    @Override
    public List<Constraint> resolveConflict(AMOConstraint conflictConstraint, IntegerStack trail,
                                            int[] stateOfResolvedvariables, Formula formula,
                                            int[] variablesInvolvedInConflict) {
        return null;
    }

    @Override
    public List<Constraint> resolveConflict(DNFConstraint conflictConstraint, IntegerStack trail,
                                            int[] stateOfResolvedVariables, Formula formula,
                                            int[] variablesInvolvedInConflict) {

        int[][] conflictTerms = conflictConstraint.terms;
        int[][] reasonTerms = this.terms;
        int conflictLiteral = formula.getConflictLiteral();
        ArrayList<int[]> resolutionTerms = new ArrayList<>();


        for (int i = 0; i < conflictTerms.length; i++) {
            boolean containsConflictLiteral = false;
            for (int j = 0; j < conflictTerms[i].length; j++) {
                if (conflictTerms[i][j] == conflictLiteral) {
                    containsConflictLiteral = true;
                    break;
                }
            }

            if (!containsConflictLiteral) {
                resolutionTerms.add(conflictTerms[i]);
            }
        }

        for (int i = 0; i < reasonTerms.length; i++) {
            boolean containsConflictLiteral = false;
            for (int j = 0; j < reasonTerms[i].length; j++) {
                if (reasonTerms[i][j] == -conflictLiteral) {
                    containsConflictLiteral = true;
                    break;
                }
            }

            if (!containsConflictLiteral) {
                resolutionTerms.add(reasonTerms[i]);
            }
        }

        return Arrays.asList(formula.addDNFConstraints(resolutionTerms.toArray(int[][]::new)));
    }

    @Override
    public ConstraintType getConstraintType() {
        return ConstraintType.DNF;
    }

    @Override
    public boolean isUnitConstraint() {
        return terms.length == 1;
    }

    @Override
    public int[] getUnitLiterals() {
        if (isUnitConstraint()) {
            return terms[0];
        } else {
            return new int[0];
        }
    }

    @Override
    public boolean isEmpty() {
        return terms.length == 0 || terms[0].length == 0;
    }

    @Override
    public int getNeededDecisionLevel(int[] decisionLevelOfVariables) {
        return 0;
    }

    @Override
    public void addVariableOccurenceCount(double[] variableOccurences) {
        for (int i = 0; i < terms.length; i++) {
            for (int j = 0; j < terms[i].length; j++) {
                variableOccurences[Math.abs(terms[i][j])] += 1;
            }
        }
    }

    @Override
    public boolean isStillWatched(int literal) {
        for (int i = 0; i < firstWatchedLiterals.length; i++) {
            if (firstWatchedLiterals[i] == -literal) {
                return true;
            }
        }

        for (int i = 0; i < secondWatchedLiterals.length; i++) {
            if (secondWatchedLiterals[i] == -literal) {
                return true;
            }
        }

        return false;
    }

    private boolean isLiteralInFirstWatchedTerm(int literal) {
        for (int i = 0; i < firstWatchedLiterals.length; i++) {
            if (firstWatchedLiterals[i] == -literal) {
                return true;
            }
        }
        return false;
    }

    private boolean isLiteralInSecondWatchedTerm(int literal) {
        for (int i = 0; i < secondWatchedLiterals.length; i++) {
            if (secondWatchedLiterals[i] == -literal) {
                return true;
            }
        }

        return false;
    }

    private void assignWatchedLiteralsToWatchList(List<Constraint>[] positivelyWatchedList,
                                                  List<Constraint>[] negativelyWatchedList) {


        if (terms.length > 0) {
            assignWatchedLiteralToWatchList(true, positivelyWatchedList, negativelyWatchedList);
        }

        if (terms.length > 1) {
            assignWatchedLiteralToWatchList(false, positivelyWatchedList, negativelyWatchedList);
        }
    }

    private void propagateIfConstraintHasOnlyOneTerm(int[] variableAssignments, int[] unitLiteralState, IntegerArrayQueue unitLiterals,
                                                     Constraint[] reasonClauses) {

        int[] currentTerm = terms[0];

        for (int i = 0; i < currentTerm.length; i++) {
            int currentLiteral = currentTerm[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (variableAssignments[currentLiteralAbsoluteValue] * currentLiteral < 0 || unitLiteralState[currentLiteralAbsoluteValue] * currentLiteral < 0) {
                hasConflict = true;
                conflictLiteral = currentLiteral;
            } else if (variableAssignments[currentLiteralAbsoluteValue] == 0) {
                unitLiterals.offer(currentLiteral);
                unitLiteralState[currentLiteralAbsoluteValue] = currentLiteral < 0 ? -1 : 1;
                if (reasonClauses[currentLiteralAbsoluteValue] == null) {
                    reasonClauses[currentLiteralAbsoluteValue] = this;
                }
            }

        }

    }

    private boolean propagateWatchedLiteral(int[] variableAssignments, int[] unitLiteralState,
                                            List<Constraint>[] positivelyWatched,
                                            List<Constraint>[] negativelyWatched,
                                            boolean firstTerm,
                                            IntegerArrayQueue unitLiterals,
                                            Constraint[] reasonClauses) {

        int[] currentTerm;

        if (firstTerm) {
            currentTerm = terms[0];
        } else {
            currentTerm = terms[1];
        }

        for (int i = 2; i < terms.length; i++) {
            if (!checkIfTermIsFalse(terms[i], variableAssignments)) {
                if (firstTerm) {
                    terms[0] = terms[i];
                    terms[i] = currentTerm;
                } else {
                    terms[1] = terms[i];
                    terms[i] = currentTerm;
                }

                assignWatchedLiteralToWatchList(firstTerm, positivelyWatched, negativelyWatched);

                return false;
            }
        }


        currentTerm = firstTerm ? terms[1] : terms[0];

        for (int i = 0; i < currentTerm.length; i++) {
            int currentLiteral = currentTerm[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);
            if (variableAssignments[currentLiteralAbsoluteValue] * currentLiteral < 0 || unitLiteralState[currentLiteralAbsoluteValue] * currentLiteral < 0) {
                hasConflict = true;
                conflictLiteral = currentLiteral;
                return true;
            }

            if (isNeededForUnitPropagation(currentLiteralAbsoluteValue, variableAssignments, unitLiteralState)) {
                unitLiterals.offer(currentLiteral);
                unitLiteralState[currentLiteralAbsoluteValue] = currentLiteral < 0 ? -1 : 1;
                if (reasonClauses[currentLiteralAbsoluteValue] == null) {
                    reasonClauses[currentLiteralAbsoluteValue] = this;
                }
            }
        }

        return true;
    }

    private boolean isNeededForUnitPropagation(int literal, int[] variables, int[] unitLiteralState) {
        int literalAbsoluteValue = Math.abs(literal);
        if (variables[literalAbsoluteValue] * literal == 0 && unitLiteralState[literalAbsoluteValue] == 0) {
            return true;
        }

        return false;
    }

    private void assignWatchedLiteralToWatchList(boolean firstTerm, List<Constraint>[] positivelyWatchedList,
                                                 List<Constraint>[] negativelyWatchedList) {

        Set<Integer> currentWatchedLiterals = getCurrentWatchedLiterals();

        int[] watchedLiterals;

        if (firstTerm) {
            firstWatchedLiterals = terms[0];
            watchedLiterals = firstWatchedLiterals;
        } else {
            secondWatchedLiterals = terms[1];
            watchedLiterals = secondWatchedLiterals;
        }

        for (int i = 0; i < watchedLiterals.length; i++) {
            int watchedLiteral = watchedLiterals[i];
            if (!currentWatchedLiterals.contains(watchedLiteral)) {
                if (watchedLiteral < 0) {
                    negativelyWatchedList[Math.abs(watchedLiteral)].add(this);
                } else {
                    positivelyWatchedList[watchedLiteral].add(this);
                }
            }
        }
    }

    private boolean checkIfTermIsFalse(int[] term, int[] variables) {
        for (int i = 0; i < term.length; i++) {
            int literal = term[i];
            if (variables[Math.abs(literal)] * literal < 0) {
                return true;
            }

        }

        return false;
    }

    public boolean isSatisfied(int[] variableAssignments) {
        for (int i = 0; i < terms.length; i++){
            if (checkIfTermIsFalse(terms[i], variableAssignments)) {
                return false;
            }
        }

        return true;
    }

    private Set<Integer> getCurrentWatchedLiterals() {
        Set<Integer> currentWatchedLiterals = new HashSet<>();

        for (int i = 0; i < firstWatchedLiterals.length; i++) {
            currentWatchedLiterals.add(firstWatchedLiterals[i]);
        }

        for (int i = 0; i < secondWatchedLiterals.length; i++) {
            currentWatchedLiterals.add(secondWatchedLiterals[i]);
        }

        return currentWatchedLiterals;
    }

}
