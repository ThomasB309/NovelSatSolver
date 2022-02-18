package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDNFConstraint;
import cas.thomas.utils.IntegerArrayQueue;
import cas.thomas.utils.IntegerStack;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DNFConstraint extends Constraint {

    private int[][] terms;
    private int[] firstWatchedLiterals;
    private int[] secondWatchedLiterals;
    private Map<Integer, Set<Integer>> literalIntersections;
    private Set<Integer>[] termSets;
    private Set<Integer> unitLiteralsPropagatedDuringInitialization;
    private Set<Integer> currentWatchedLiterals;
    private LinkedList<int[][]> lastWatchedLiterals;

    public DNFConstraint(int[][] terms, List<Constraint>[] positivelyWatchedList,
                         List<Constraint>[] negativelyWatchedList, IntegerArrayQueue unitLiterals,
                         int[] variableAssignment,
                         int[] unitLiteralState, int[] decisionLevelOfVariables) {
        super();

        assert(terms.length > 0);

        this.terms = terms;
        unitLiteralsPropagatedDuringInitialization = new HashSet<>();

        if (terms.length == 1) {
            for (int i = 0; i < terms[0].length; i++) {
                unitLiteralsPropagatedDuringInitialization.add(terms[0][i]);
            }
            return;
        }

        literalIntersections = new HashMap<Integer, Set<Integer>>();
        termSets = new HashSet[terms.length];
        unitLiteralsPropagatedDuringInitialization = new HashSet<>();
        currentWatchedLiterals = new HashSet<>();
        lastWatchedLiterals = new LinkedList<>();



        for (int i = 0; i < terms.length; i++) {
            assert(terms[i].length > 0);

            this.terms[i] = terms[i];
            Set<Integer> termSet = new HashSet<>();
            this.termSets[i] = termSet;


            for (int j = 0; j < terms[i].length; j++) {
                int currentLiteral = terms[i][j];
                termSet.add(currentLiteral);
                if (literalIntersections.containsKey(currentLiteral)) {
                    Set<Integer> intersectionList = literalIntersections.get(currentLiteral);
                    intersectionList.add(i);

                    if (intersectionList.size() == terms.length) {
                        unitLiteralsPropagatedDuringInitialization.add(currentLiteral);
                    }

                } else {
                    Set<Integer> newList = new HashSet<>();
                    newList.add(i);
                    literalIntersections.put(currentLiteral, newList);
                }
            }

        }

        this.terms = terms;

        assignWatchedLiteralsToWatchList(positivelyWatchedList, negativelyWatchedList, variableAssignment,
                unitLiteralState, decisionLevelOfVariables);
    }

    @Override
    public boolean propagate(int propagatedLiteral, int[] variableAssignments, int[] unitLiteralState, IntegerArrayQueue unitLiterals, List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched, Constraint[] reasonClauses) {

        if (terms.length == 1) {
           propagateIfConstraintHasOnlyOneTerm(variableAssignments, unitLiteralState, unitLiterals, reasonClauses);
           return true;
        }


        lastWatchedLiterals.add(new int[][]{firstWatchedLiterals, secondWatchedLiterals});

        boolean unitPropagation = true;

        if (isLiteralInFirstWatchedTerm(propagatedLiteral)) {
            unitPropagation = propagateWatchedLiteral(variableAssignments, unitLiteralState, positivelyWatched,
                    negativelyWatched,
                    true, unitLiterals,
                    reasonClauses);

        } else if (isLiteralInSecondWatchedTerm(propagatedLiteral)) {
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
                variablesInvolvedInConflict[Math.abs(reasonTerms[i][j])] = 1;
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
        int[] conflictLiterals = conflictConstraint.getLiterals();
        int[][] reasonTerms = this.terms;
        int conflictLiteral = formula.getConflictLiteral();

        ArrayList<int[]> resolutionTerms = new ArrayList<>();
        int[] termFromAmoLiterals = new int[conflictLiterals.length - 1];
        int counter = 0;
        for (int i = 0; i < conflictLiterals.length; i++) {
            if (conflictLiterals[i] != -conflictLiteral) {
                termFromAmoLiterals[counter] = -conflictLiterals[i];
                counter++;
            }
        }

        resolutionTerms.add(termFromAmoLiterals);

        for (int i = 0; i < reasonTerms.length; i++) {
            boolean containsConflictLiteral = false;
            for (int j = 0; j < reasonTerms[i].length; j++) {
                variablesInvolvedInConflict[Math.abs(reasonTerms[i][j])] = 1;
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
                variablesInvolvedInConflict[Math.abs(reasonTerms[i][j])] = 1;
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
        int decisionLevel = Integer.MAX_VALUE;

        if (terms.length == 1) {
            for (int i = 0; i < terms[0].length; i++) {
                decisionLevel = Math.min(decisionLevel, decisionLevelOfVariables[Math.abs(terms[0][i])]);
            }
            return decisionLevel;
        }

        for (int i = 0; i < firstWatchedLiterals.length; i++) {
            decisionLevel = Math.min(decisionLevel, decisionLevelOfVariables[Math.abs(firstWatchedLiterals[i])]);
        }

        for (int i = 0; i < secondWatchedLiterals.length; i++) {
            decisionLevel = Math.min(decisionLevel, decisionLevelOfVariables[Math.abs(secondWatchedLiterals[i])]);
        }

        return decisionLevel;

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
                                                  List<Constraint>[] negativelyWatchedList,
                                                  int[] variableAssignments, int[] unitLiteralState,
                                                  int[] decisionLevelOfVariables) {


        for (int i = 0; i < terms.length; i++) {
            Set<Integer> notIntersectingTerms = findTermNotIntersectingConsideringPropagatedUnitLiterals(i,
                    variableAssignments,
                    unitLiteralState, decisionLevelOfVariables);

            if(!notIntersectingTerms.isEmpty()) {
                firstWatchedLiterals = terms[i];
                secondWatchedLiterals = terms[notIntersectingTerms.iterator().next()];
                break;
            }

        }

        assert (firstWatchedLiterals != null);
        assert (secondWatchedLiterals != null);

        assignWatchedLiteralToWatchList(true, positivelyWatchedList, negativelyWatchedList);
        assignWatchedLiteralToWatchList(false, positivelyWatchedList, negativelyWatchedList);
        currentWatchedLiterals = getCurrentWatchedLiterals();
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
            currentTerm = firstWatchedLiterals;
        } else {
            currentTerm = secondWatchedLiterals;
        }

        int[] falseTermsCache = new int[terms.length];

        if (!findNewWatchedTermsPair(firstTerm, variableAssignments, unitLiteralState,currentTerm, positivelyWatched,
                negativelyWatched,
                falseTermsCache)) {
            return false;
        }

        boolean onlyOneTermLeft = true;
        int falseCounter = 0;
        for (int i = 0; i < falseTermsCache.length; i++) {
            if (falseTermsCache[i] == -1) {
                falseCounter++;
                continue;
            } else if (checkIfTermIsFalse(terms[i], variableAssignments)) {
                falseTermsCache[i] = -1;
                falseCounter++;
            }
        }

        if (falseCounter == terms.length) {
            return false;
        }

        if (falseCounter == terms.length - 1) {
            currentTerm = firstTerm ? secondWatchedLiterals : firstWatchedLiterals;

            for (int a = 0; a < currentTerm.length; a++) {
                int currentLiteral = currentTerm[a];
                int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

                /*if (Math.abs(currentLiteral) == 2695) {
                    System.out.println("hello");
                }*/

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

        Set<Integer> unitLiteralCandidates = findSharedLiterals(falseTermsCache, falseCounter, variableAssignments);

        for (Integer currentLiteral : unitLiteralCandidates) {
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            /*if (Math.abs(currentLiteral) == 2695) {
                System.out.println("hello");
            }*/

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

        if (!findNewWatchedTermsPair(firstTerm, variableAssignments, unitLiteralState,currentTerm, positivelyWatched,
                negativelyWatched,
                falseTermsCache)) {
            return false;
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

    private boolean findNewWatchedTermsPair(boolean firstTerm, int[] variableAssignments, int[] unitLiteralState,
                                            int[] currentTerm,
                                            List<Constraint>[] positivelyWatched,
                                            List<Constraint>[] negativelyWatched, int[] falseTermsCache) {

        for (int i = 0; i < terms.length; i++) {

            if ((firstTerm && terms[i] == firstWatchedLiterals) || (!firstTerm && terms[i] == secondWatchedLiterals)) {
                continue;
            }

            if (checkIfTermIsFalse(terms[i], variableAssignments)) {
                falseTermsCache[i] = -1;
                continue;
            }

            Set<Integer> notIntersectingTerms = findTermNotIntersectingConsideringPropagatedUnitLiterals(i,
                    variableAssignments, unitLiteralState);

            if (!notIntersectingTerms.isEmpty()) {
                for (Integer termIndex : notIntersectingTerms) {
                    if (falseTermsCache[termIndex] != -1 && !checkIfTermIsFalse(terms[termIndex], variableAssignments)) {
                        firstWatchedLiterals = terms[i];
                        secondWatchedLiterals = terms[termIndex];

                        assignWatchedLiteralToWatchList(firstTerm, positivelyWatched, negativelyWatched);
                        assignWatchedLiteralToWatchList(!firstTerm, positivelyWatched, negativelyWatched);
                        currentWatchedLiterals = getCurrentWatchedLiterals();

                        return false;
                    } else {
                        falseTermsCache[termIndex] = -1;
                    }
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

        int[] watchedLiterals;

        if (firstTerm) {
            watchedLiterals = firstWatchedLiterals;
        } else {
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

    private void assignWatchedLiteralToWatchListWithIterator(boolean firstTerm,
                                                             List<Constraint>[] positivelyWatchedList,
                                                             List<Constraint>[] negativelyWatchedList,
                                                             ListIterator<Constraint> listIterator, int literal) {

        int[] watchedLiterals;

        if (firstTerm) {
            watchedLiterals = firstWatchedLiterals;
        } else {
            watchedLiterals = secondWatchedLiterals;
        }

        for (int i = 0; i < watchedLiterals.length; i++) {
            int watchedLiteral = watchedLiterals[i];
            if (!currentWatchedLiterals.contains(watchedLiteral)) {
                if (watchedLiteral == literal) {
                    listIterator.add(this);
                } else if (watchedLiteral < 0) {
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

    private boolean checkIfTermIsFalseConsideringUnitLiterals(int[] term, int[] variables,
                                                              int[] decisionLevelOfVariables) {

        for (int i = 0; i < term.length; i++) {
            int literal = term[i];
            if (variables[Math.abs(literal)] * literal < 0 && decisionLevelOfVariables[Math.abs(literal)] == 0) {
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


    private Set<Integer> findTermNotIntersectingConsideringPropagatedUnitLiterals(int termIndex,
                                                                                  int[] variableAssignments,
                                                                                  int[] unitLiteralState) {
        int[] currentTerm = terms[termIndex];
        Set<Integer> notIntersectingterms = new HashSet<Integer>(IntStream.range(0, terms.length).boxed().collect(Collectors.toList()));
        for (int i = 0; i < currentTerm.length; i++) {
            int currentLiteral = currentTerm[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (variableAssignments[currentLiteralAbsoluteValue] * currentLiteral <= 0 && unitLiteralState[currentLiteralAbsoluteValue] * currentLiteral <= 0 && !unitLiteralsPropagatedDuringInitialization.contains(currentLiteral)) {
                notIntersectingterms.removeAll(literalIntersections.get(currentLiteral));
            }

        }

        notIntersectingterms.remove(termIndex);

        return notIntersectingterms;

    }

    private Set<Integer> findTermNotIntersectingConsideringPropagatedUnitLiterals(int termIndex,
                                                                                  int[] variableAssignments,
                                                                                  int[] unitLiteralState,
                                                                                  int[] decisionLevelOfVariables) {
        int[] currentTerm = terms[termIndex];
        Set<Integer> notIntersectingterms = new HashSet<Integer>(IntStream.range(0, terms.length).boxed().collect(Collectors.toList()));
        for (int i = 0; i < currentTerm.length; i++) {
            int currentLiteral = currentTerm[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (unitLiteralState[currentLiteralAbsoluteValue] * currentLiteral <= 0 && !unitLiteralsPropagatedDuringInitialization.contains(currentLiteral)) {
                notIntersectingterms.removeAll(literalIntersections.get(currentLiteral));
            }

        }

        notIntersectingterms.remove(termIndex);

        return notIntersectingterms;

    }

    private Set<Integer> findSharedLiterals(int[] falseTermCache, int falseCounter, int[] variableAssignments) {

        for (int i = 0; i < falseTermCache.length; i++) {
            if (falseTermCache[i] != -1) {
                int[] currentTerm = terms[i];
                Set<Integer> unitLiterals = new HashSet<>();

                for (int j = 0; j < currentTerm.length; j++) {
                    int currentLiteral = currentTerm[j];

                    Set<Integer> intersectedTerms = literalIntersections.get(currentLiteral);

                    int trueCounter = 0;
                    for (Integer termIndex : intersectedTerms) {
                        if (falseTermCache[termIndex] != -1) {
                            if (!checkIfTermIsFalse(terms[termIndex], variableAssignments)) {
                                trueCounter++;
                            } else {
                                falseTermCache[termIndex] = -1;
                            }
                        }
                    }

                    if (trueCounter == (terms.length - falseCounter)) {
                        unitLiterals.add(currentLiteral);
                    }
                }

                return unitLiterals;

            }
        }

        return new HashSet<>();

    }

    public void backtrack(int variable, int[] unitLiteralState, Set<Integer> unitLiteralsBeforePropagation,
                          List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched,
                          ListIterator<Constraint> listIterator) {
        int sharedLiteral = 0;

        boolean containsLiteral = false;
        for (int i = 0; i < firstWatchedLiterals.length; i++) {
            if (Math.abs(firstWatchedLiterals[i]) == Math.abs(variable)) {
                sharedLiteral = firstWatchedLiterals[i];
                containsLiteral = true;
            }
        }

        if (!containsLiteral) {
            return;
        }

        containsLiteral = false;

        for (int i = 0; i < secondWatchedLiterals.length; i++) {
            if (secondWatchedLiterals[i] == sharedLiteral) {
                containsLiteral = true;
                break;
            }
        }

        if (!containsLiteral) {
            return;
        }

        if (unitLiteralsPropagatedDuringInitialization.contains(sharedLiteral)) {
            return;
        }

        int[][] watchedLiterals = lastWatchedLiterals.removeLast();

        firstWatchedLiterals = watchedLiterals[0];
        secondWatchedLiterals = watchedLiterals[1];

        assignWatchedLiteralToWatchListWithIterator(true, positivelyWatched, negativelyWatched, listIterator, variable);
        assignWatchedLiteralToWatchListWithIterator(false, positivelyWatched, negativelyWatched, listIterator, variable);
        currentWatchedLiterals = getCurrentWatchedLiterals();
    }

    public int getLBDScore(int[] variableDecisionLevels) {
        Set<Integer> distinctDecisionLevels = new HashSet<>();

        for (int i = 0; i < terms.length; i++) {
            for (int j = 0; j < terms[i].length; j++) {
                distinctDecisionLevels.add(variableDecisionLevels[Math.abs(terms[i][j])]);
            }
        }

        return distinctDecisionLevels.size();
    }

    public String toString() {
        String constraint = "DNF";

        for (int i = 0; i < terms.length; i++) {
            constraint += " " + Arrays.toString(terms[i]);
        }

        return constraint;
    }

    @Override
    public Set<Integer> getUnitLiteralsNeededBeforePropagation() {
        return unitLiteralsPropagatedDuringInitialization;
    }

    public int[][] getTerms() {
        return this.terms;
    }

}
