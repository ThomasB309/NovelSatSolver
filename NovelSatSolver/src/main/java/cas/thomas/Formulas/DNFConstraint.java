package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDNFConstraint;
import cas.thomas.utils.IntegerArrayQueue;
import cas.thomas.utils.IntegerStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DNFConstraint extends Constraint {

    protected int[][] terms;
    protected Set<Integer> unitLiteralsPropagatedDuringInitialization;
    private int[] falseTermCache;
    private Set<Integer> visitedLiterals;
    private HashMap<Integer, Integer> literalsMapping;
    private Integer[][] positiveLiteralsIntersectionList;
    private Set<Integer> literalSet;
    private int falseCounter;
    private int firstWatchedTerm;
    private int secondWatchedTerm;

    public DNFConstraint(final int[][] terms, final List<Constraint>[] positivelyWatchedList,
                         final List<Constraint>[] negativelyWatchedList) {

        assert (terms.length > 0);

        this.terms = terms;
        this.falseTermCache = new int[terms.length];
        this.visitedLiterals = new HashSet<>();
        this.unitLiteralsPropagatedDuringInitialization = new HashSet<>();
        this.literalSet = new HashSet<>();
        this.falseCounter = 0;

        Arrays.sort(terms, Comparator.comparing(term -> term.length));

        if (terms.length == 1) {
            for (int i = 0; i < terms[0].length; i++) {
                this.unitLiteralsPropagatedDuringInitialization.add(terms[0][i]);
            }
            return;
        }

        this.unitLiteralsPropagatedDuringInitialization = new HashSet<>();

        final Set<Integer> literalsInDNF = new HashSet<>();

        int nullCounter = termSubsumption(terms);

        this.terms = new int[terms.length - nullCounter][];

        int counter = 0;
        for (int i = 0; i < terms.length; i++){
            if (terms[i] != null) {
                this.terms[counter] = terms[i];
                counter++;
            }
        }



        this.calculateTermIntersectionsAndUnitLiterals(this.terms, literalsInDNF);

        firstWatchedTerm = 0;
        secondWatchedTerm = 1;

        this.assignWatchedLiteralsToWatchList(literalsInDNF, positivelyWatchedList, negativelyWatchedList);
    }

    private int termSubsumption(int[][] terms) {

        int nullCounter = 0;

        for (int i = 0; i < terms.length; i++) {
            int[] term = terms[i];

            if (term == null) {
                continue;
            }

            Arrays.sort(term);

            for (int a = i + 1; a < terms.length; a++) {
                int[] secondTerm = terms[a];

                if (secondTerm == null || secondTerm.length < term.length) {
                    continue;
                }

                boolean subset = true;
                for (int k = 0; k < term.length; k++) {
                    if (Arrays.binarySearch(secondTerm, term[k]) < 0) {
                        subset = false;
                        break;
                    }
                }

                if (subset) {
                    terms[a] = null;
                    nullCounter++;
                }
            }
        }

        return nullCounter;
    }

    private void calculateTermIntersectionsAndUnitLiterals(final int[][] terms, final Set<Integer> literalsInDNF) {
        int literalCounter = 0;
        Map<Integer, HashSet<Integer>> literalIntersections = new HashMap<>();

        for (int i = 0; i < terms.length; i++) {
            assert (terms[i].length > 0);

            this.terms[i] = terms[i];
            final List<Integer> termSet = new LinkedList<>();

            for (int j = 0; j < terms[i].length; j++) {
                final int currentLiteral = terms[i][j];
                this.literalSet.add(currentLiteral);

                literalCounter++;

                literalsInDNF.add(currentLiteral);
                termSet.add(currentLiteral);
                if (literalIntersections.containsKey(currentLiteral)) {
                    final HashSet<Integer> intersectionList = literalIntersections.get(currentLiteral);
                    intersectionList.add(i);

                    if (intersectionList.size() == terms.length) {
                        this.unitLiteralsPropagatedDuringInitialization.add(currentLiteral);
                    }

                } else {
                    final HashSet<Integer> newList = new HashSet<>();
                    newList.add(i);
                    literalIntersections.put(currentLiteral, newList);
                }
            }

        }

        positiveLiteralsIntersectionList = new Integer[literalCounter][];
        literalsMapping = new HashMap<>();

        int counter = 0;
        for (Integer key : literalSet) {
            literalsMapping.put(key, counter);
            positiveLiteralsIntersectionList[counter] = literalIntersections.get(key).toArray(Integer[] :: new);
            counter++;
        }
    }

    private void assignWatchedLiteralsToWatchList(final Set<Integer> literalsInDNF,
                                                  final List<Constraint>[] positivelyWatchedList,
                                                  final List<Constraint>[] negativelyWatchedList) {


        for (final Integer literal : literalsInDNF) {
            final int literalAbsoluteValue = Math.abs(literal);
            if (literal < 0) {
                negativelyWatchedList[literalAbsoluteValue].add(this);
            } else {
                positivelyWatchedList[literalAbsoluteValue].add(this);
            }
        }


    }

    protected DNFConstraint() {

    }

    @Override
    public boolean propagate(final int propagatedLiteral, final int[] variableAssignments, final int[] unitLiteralState, final IntegerArrayQueue unitLiterals, final List<Constraint>[] positivelyWatched, final List<Constraint>[] negativelyWatched, final Constraint[] reasonClauses) {

        //this.visitedLiterals.add(-propagatedLiteral);
        if (this.terms.length == 1) {
            this.propagateIfConstraintHasOnlyOneTerm(0, variableAssignments, unitLiteralState, unitLiterals, reasonClauses);
            return true;
        }

        final IntegerStack literalsToPropagate = new IntegerStack();


        falseTermCache = new int[terms.length];
        falseCounter = 0;

        Integer[] falseTerms = positiveLiteralsIntersectionList[literalsMapping.get(-propagatedLiteral)];
        boolean changeFirstWatchedTerm = false;
        boolean changeSecondWatchedTerm = false;
        for (int i = 0; i  < falseTerms.length; i++) {
            int termIndex = falseTerms[i];
            falseTermCache[termIndex] = -1;

            if (termIndex == firstWatchedTerm) {
                changeFirstWatchedTerm = true;
            } else if (termIndex == secondWatchedTerm) {
                changeSecondWatchedTerm = true;
            }
        }

        if (!changeFirstWatchedTerm && checkIfTermIsFalse(variableAssignments, firstWatchedTerm, falseTermCache)) {
            changeFirstWatchedTerm = true;
        }

        if (!changeSecondWatchedTerm && checkIfTermIsFalse(variableAssignments, secondWatchedTerm, falseTermCache)) {
            changeSecondWatchedTerm = true;
        }

        int firstNonFalseTermIndex = -1;
        for (int i = 0; i < this.falseTermCache.length; i++) {

            if (!checkIfTermIsFalse(variableAssignments, i, falseTermCache)) {
                if (firstNonFalseTermIndex == -1) {
                    firstNonFalseTermIndex = i;
                }

                if (changeFirstWatchedTerm && i != secondWatchedTerm) {
                    firstWatchedTerm = i;
                    changeFirstWatchedTerm = false;
                } else if (changeSecondWatchedTerm && i != firstWatchedTerm) {
                    secondWatchedTerm = i;
                    changeSecondWatchedTerm = false;
                }
            }

            if (!changeFirstWatchedTerm && !changeSecondWatchedTerm && firstNonFalseTermIndex != -1) {
                break;
            }
        }

        if (firstNonFalseTermIndex == -1) {
            return false;
        }

        if (changeFirstWatchedTerm ^ changeSecondWatchedTerm) {
            this.propagateIfConstraintHasOnlyOneTerm(firstNonFalseTermIndex, variableAssignments, unitLiteralState,
                    unitLiterals,reasonClauses);
            return true;
        }

        final int[] shortestNonFalseTerm = this.terms[firstNonFalseTermIndex];

        this.findLiteralsInIntersectionOfAllNonFalseTerms(literalsToPropagate, firstNonFalseTermIndex, falseCounter,
                shortestNonFalseTerm, variableAssignments);

        final boolean propagate = literalsToPropagate.size() > 0;

        if (this.addUnitLiterals(variableAssignments, unitLiteralState, unitLiterals, reasonClauses, literalsToPropagate))
            return true;

        return propagate;
    }

    @Override
    public SolutionCheckerConstraint getSolutionCheckerConstraint() {
        return new SolutionCheckerDNFConstraint(terms);
    }

    public int calculateLBDScore(final int[] variableDecisionLevels) {
        final Set<Integer> distinctDecisionLevels = new HashSet<>();

        for (int i = 0; i < this.terms.length; i++) {
            for (int j = 0; j < this.terms[i].length; j++) {
                distinctDecisionLevels.add(variableDecisionLevels[Math.abs(this.terms[i][j])]);
            }
        }

        return distinctDecisionLevels.size();
    }

    @Override
    public List<Constraint> handleConflict(final int numberOfVariables, final IntegerStack trail, final int[] variableDecisionLevel, final int[] variablesInvolvedInConflict, final Formula formula) {
        final int[] stateOfResolvedVariables = new int[numberOfVariables];

        for (int i = 0; i < this.terms.length; i++) {
            for (int j = 0; j < this.terms[i].length; j++) {
                final int currentLiteralAbsoluteValue = Math.abs(this.terms[i][j]);
                stateOfResolvedVariables[currentLiteralAbsoluteValue] = this.terms[i][j];
                variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;
            }
        }

        return this.findReasonConstraintAndResolveConflict(trail, variablesInvolvedInConflict, formula, stateOfResolvedVariables);
    }

    private List<Constraint> findReasonConstraintAndResolveConflict(final IntegerStack trail, final int[] variablesInvolvedInConflict, final Formula formula, final int[] stateOfResolvedVariables) {
        trail.prepareIterationWithoutPop();
        while (trail.hasNextWithoutPop()) {
            final int literal = trail.peekNextWithoutPop();
            final Constraint reasonConstraint = formula.getReasonClauses(literal);

            if (reasonConstraint == null) {
                continue;
            }

            return reasonConstraint.resolveConflict(this, trail, stateOfResolvedVariables, formula, variablesInvolvedInConflict);
        }

        return Arrays.asList();
    }

    @Override
    public List<Constraint> resolveConflict(final Constraint conflictConstraint, final IntegerStack trail,
                                            final int[] stateOfResolvedVariables, final Formula formula,
                                            final int[] variablesInvolvedInConflict) {
        final int[] conflictLiterals = conflictConstraint.getLiterals();
        final int[][] reasonTerms = terms;
        final int conflictLiteral = formula.getConflictLiteral();

        final ArrayList<int[]> resolutionTerms = new ArrayList<>();

        this.getResolutionTermsFromConflictingDisjunctiveConstraint(conflictLiterals, conflictLiteral, resolutionTerms);

        this.getResolutionTermsFromReasonDNFConstraint(variablesInvolvedInConflict, reasonTerms, conflictLiteral, resolutionTerms);


        return Arrays.asList(formula.addDNFConstraints(resolutionTerms.toArray(int[][]::new)));

    }

    private void getResolutionTermsFromConflictingDisjunctiveConstraint(final int[] conflictLiterals, final int conflictLiteral, final ArrayList<int[]> resolutionTerms) {
        for (int i = 0; i < conflictLiterals.length; i++) {
            final int currentLiteral = conflictLiterals[i];
            if (currentLiteral != conflictLiteral) {
                resolutionTerms.add(new int[]{currentLiteral});
            }
        }
    }

    private void getResolutionTermsFromReasonDNFConstraint(final int[] variablesInvolvedInConflict, final int[][] reasonTerms, final int conflictLiteral, final ArrayList<int[]> resolutionTerms) {
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
    }

    @Override
    public List<Constraint> resolveConflict(final AMOConstraint conflictConstraint, final IntegerStack trail,
                                            final int[] stateOfResolvedvariables, final Formula formula,
                                            final int[] variablesInvolvedInConflict) {
        final int[] conflictLiterals = conflictConstraint.getLiterals();
        final int[][] reasonTerms = terms;
        final int conflictLiteral = formula.getConflictLiteral();

        final ArrayList<int[]> resolutionTerms = new ArrayList<>();
        final int[] termFromAmoLiterals = this.getResolutionTermsFromConflictingAMOConstraint(conflictLiterals, conflictLiteral);

        resolutionTerms.add(termFromAmoLiterals);

        this.getResolutionTermsFromReasonDNFConstraint(variablesInvolvedInConflict, reasonTerms, conflictLiteral, resolutionTerms);

        return Arrays.asList(formula.addDNFConstraints(resolutionTerms.toArray(int[][]::new)));

    }

    private int[] getResolutionTermsFromConflictingAMOConstraint(final int[] conflictLiterals, final int conflictLiteral) {
        final int[] termFromAmoLiterals = new int[conflictLiterals.length - 1];
        int counter = 0;
        for (int i = 0; i < conflictLiterals.length; i++) {
            if (conflictLiterals[i] != -conflictLiteral) {
                termFromAmoLiterals[counter] = -conflictLiterals[i];
                counter++;
            }
        }
        return termFromAmoLiterals;
    }

    @Override
    public List<Constraint> resolveConflict(final DNFConstraint conflictConstraint, final IntegerStack trail,
                                            final int[] stateOfResolvedVariables, final Formula formula,
                                            final int[] variablesInvolvedInConflict) {

        final int[][] conflictTerms = conflictConstraint.terms;
        final int[][] reasonTerms = terms;
        final int conflictLiteral = formula.getConflictLiteral();
        final ArrayList<int[]> resolutionTerms = new ArrayList<>();


        this.getResolutionTermsFromConflictingDNFConstraint(conflictTerms, conflictLiteral, resolutionTerms);

        this.getResolutionTermsFromReasonDNFConstraint(variablesInvolvedInConflict, reasonTerms, conflictLiteral, resolutionTerms);

        return Arrays.asList(formula.addDNFConstraints(resolutionTerms.toArray(int[][]::new)));
    }

    private void getResolutionTermsFromConflictingDNFConstraint(final int[][] conflictTerms, final int conflictLiteral, final ArrayList<int[]> resolutionTerms) {
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
    }

    @Override
    public ConstraintType getConstraintType() {
        return ConstraintType.DNF;
    }

    @Override
    public boolean isUnitConstraint() {
        return this.unitLiteralsPropagatedDuringInitialization.size() > 0;
    }

    @Override
    public int[] getUnitLiterals() {
        if (this.isUnitConstraint()) {
            return this.terms[0];
        } else {
            return new int[0];
        }
    }

    @Override
    public boolean isEmpty() {
        return this.terms.length == 0 || this.terms[0].length == 0;
    }

    @Override
    public int getNeededDecisionLevel(final int[] decisionLevelOfVariables, int[] variables) {

        if (terms.length == 1) {
            return calculateNeededDecisionLevelForDNFConstraintWithSingleTerm(decisionLevelOfVariables);
        }

        Integer[] literals = literalSet.toArray(new Integer[0]);

        Arrays.sort(literals, Comparator.comparingInt(a -> decisionLevelOfVariables[Math.abs(a)] * -1));

        int[] variablesCopy = Arrays.copyOf(variables, variables.length);

        int[] trueTermCache = new int[terms.length];

        this.falseTermCache = new int[terms.length];
        this.falseCounter = this.terms.length;

        for (int i = 0; i < falseTermCache.length; i++) {
            falseTermCache[i] = -1;
        }

        List<Integer> unassignedTermsIndeces = new LinkedList<>();
        boolean containsFirstWatchedTerm = false;
        boolean containsSecondWatchedTerm = false;
        int smallestTermIndex = Integer.MAX_VALUE;
        int numberOfUnassignedTerms = 0;
        for (int i = 0; i < literals.length; i++) {
            variablesCopy[Math.abs(literals[i])] = 0;
            Integer[] termIndexList = positiveLiteralsIntersectionList[literalsMapping.get(literals[i])];
            for (Integer termIndex : termIndexList) {
                if (trueTermCache[termIndex] != 1 && !checkIfTermIsFalseAfterUnassignment(variablesCopy,
                        terms[termIndex])) {

                    if (!containsFirstWatchedTerm) {
                        firstWatchedTerm = termIndex;
                        containsFirstWatchedTerm = true;
                    } else if (!containsSecondWatchedTerm) {
                        secondWatchedTerm = termIndex;
                        containsSecondWatchedTerm = true;
                    }


                    unassignedTermsIndeces.add(termIndex);
                    smallestTermIndex = Math.min(termIndex, smallestTermIndex);
                    numberOfUnassignedTerms++;
                    trueTermCache[termIndex] = 1;
                    falseTermCache[termIndex] = 0;
                    falseCounter--;
                }

            }

            if (unassignedTermsIndeces.size() > 1) {
                if (checkIfUnassignedTermsIntersect(smallestTermIndex, terms[smallestTermIndex], trueTermCache,
                        numberOfUnassignedTerms)) {

                    assert (unassignedTermsIndeces.contains(firstWatchedTerm) && unassignedTermsIndeces.contains(secondWatchedTerm));

                    return decisionLevelOfVariables[Math.abs(literals[i])];
                }
            }
        }

        return 0;

    }

    private int calculateNeededDecisionLevelForDNFConstraintWithSingleTerm(int[] decisionLevelOfVariables) {
        int decisionLevel = Integer.MAX_VALUE;

        for (int i = 0; i < terms[0].length; i++) {
            decisionLevel = Math.min(decisionLevel, decisionLevelOfVariables[Math.abs(terms[0][i])]);
        }

        return decisionLevel;
    }

    private boolean checkIfUnassignedTermsIntersect(int smallestUnassignedTermIndex, int[] smallestUnassignedTerm,
                                                    int[] trueTermCache, int numberOfUnassignedTerms) {

        int trueCounterIntersection = 0;
        int[] intersectionAlreadyCounted = new int[terms.length];
        for (int i = 0; i < smallestUnassignedTerm.length; i++) {
            final int currentLiteral = smallestUnassignedTerm[i];
            Integer[] termIndexList = positiveLiteralsIntersectionList[literalsMapping.get(currentLiteral)];
            for (int a = 0; a < termIndexList.length; a++) {
                int termIndex = termIndexList[a];
                if (intersectionAlreadyCounted[termIndex] != 1 && termIndex != smallestUnassignedTermIndex && trueTermCache[termIndex] == 1) {
                    trueCounterIntersection++;
                    intersectionAlreadyCounted[termIndex] = 1;
                }
            }

        }

        if (trueCounterIntersection == numberOfUnassignedTerms - 1) {
            return false;
        }

        return true;
    }

    private boolean checkIfTermIsFalseAfterUnassignment(int[] variables, int[] term) {
        for (int i = 0; i < term.length; i++) {
            final int literal = term[i];
            if (variables[Math.abs(literal)] * literal < 0) {
                return true;
            }

        }

        return false;
    }

    private boolean checkIfTermIsFalse(int[] variables, int termIndex, int[] falseTermCache) {
        if (falseTermCache[termIndex] == -1) {
            return true;
        }

        if (falseTermCache[termIndex] == 1) {
            return false;
        }

        int[] term = terms[termIndex];

        for (int i = 0; i < term.length; i++) {
            final int literal = term[i];
            if (variables[Math.abs(literal)] * literal < 0) {
                falseTermCache[termIndex] = -1;
                return true;
            }
        }

        falseTermCache[termIndex] = 1;

        return false;
    }

    @Override
    public void addVariableOccurenceCount(final double[] variableOccurences) {
        for (int i = 0; i < this.terms.length; i++) {
            for (int j = 0; j < this.terms[i].length; j++) {
                if (this.terms[i].length > 1) {
                    variableOccurences[Math.abs(this.terms[i][j])] += 1;
                } else {
                    variableOccurences[Math.abs(this.terms[i][j])] += 1;
                }
            }
        }
    }

    @Override
    public boolean isStillWatched(final int literal, int[] variables) {

        return true;
    }

    public void backtrack(final int variable, final int[] variableAssingments) {

        Integer[] falseTerms = positiveLiteralsIntersectionList[literalsMapping.get(-variable)];
        this.visitedLiterals.remove(-variable);

        for (int i = 0; i < falseTerms.length; i++) {
            int termIndex = falseTerms[i];

            if (this.falseTermCache[termIndex] == 0) {
                return;
            }

            if (!this.checkIfTermIsFalseExludingLiteral(this.terms[termIndex], variableAssingments, -variable)) {
                this.falseTermCache[termIndex] = 0;
                falseCounter--;
            }
        }
    }

    private boolean checkIfTermIsFalseExludingLiteral(final int[] term, final int[] variables, final int excludedLiteral) {
        for (int i = 0; i < term.length; i++) {
            final int literal = term[i];
            if (literal != excludedLiteral && variables[Math.abs(literal)] * literal < 0) {
                return true;
            }

        }

        return false;
    }

    public String toString() {
        String constraint = "DNF";

        for (int i = 0; i < this.terms.length; i++) {
            constraint += " " + Arrays.toString(this.terms[i]);
        }

        return constraint;
    }

    @Override
    public Set<Integer> getUnitLiteralsNeededBeforePropagation() {
        return this.unitLiteralsPropagatedDuringInitialization;
    }

    private void propagateIfConstraintHasOnlyOneTerm(final int termIndex, final int[] variableAssignments, final int[] unitLiteralState,
                                                     final IntegerArrayQueue unitLiterals,
                                                     final Constraint[] reasonClauses) {

        final int[] currentTerm = this.terms[termIndex];

        for (int i = 0; i < currentTerm.length; i++) {
            final int currentLiteral = currentTerm[i];
            final int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (variableAssignments[currentLiteralAbsoluteValue] * currentLiteral < 0 || unitLiteralState[currentLiteralAbsoluteValue] * currentLiteral < 0) {
                this.hasConflict = true;
                this.conflictLiteral = currentLiteral;
                return;
            } else if (this.isNeededForUnitPropagation(currentLiteralAbsoluteValue, variableAssignments, unitLiteralState)) {
                unitLiterals.offer(currentLiteral);
                unitLiteralState[currentLiteralAbsoluteValue] = currentLiteral < 0 ? -1 : 1;
                if (reasonClauses[currentLiteralAbsoluteValue] == null) {
                    reasonClauses[currentLiteralAbsoluteValue] = this;
                }
            }

        }

    }

    private void findLiteralsInIntersectionOfAllNonFalseTerms(final IntegerStack literalsToPropagate,
                                                              final int firstNonFalseTermIndex,
                                                              final int nonFalseCounter,
                                                              final int[] shortestNonFalseTerm, int[] variables) {
        int counterPartTermIndex = firstNonFalseTermIndex == firstWatchedTerm ? secondWatchedTerm : firstWatchedTerm;

        Integer[] intersectionIndeces;
        for (int i = 0; i < shortestNonFalseTerm.length; i++) {
            final int currentLiteral = shortestNonFalseTerm[i];

            intersectionIndeces = positiveLiteralsIntersectionList[literalsMapping.get(currentLiteral)];

            if (intersectionIndeces.length == 1) {
                continue;
            }

            if (!contains(intersectionIndeces, counterPartTermIndex)) {
                continue;
            }

            boolean foundNonFalseNonIntersectingTerm = false;
            for (int a = firstNonFalseTermIndex + 1; a < falseTermCache.length; a++) {
                if (a != counterPartTermIndex && !contains(intersectionIndeces,a) && !checkIfTermIsFalse(variables, a,
                        falseTermCache)) {
                    foundNonFalseNonIntersectingTerm = true;
                    break;
                }
            }

            if (foundNonFalseNonIntersectingTerm) {
                continue;
            }


            literalsToPropagate.push(currentLiteral);
        }
    }

    public boolean contains(Integer[] array, int number) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == number){
                return true;
            }
        }

        return false;
    }

    private boolean addUnitLiterals(final int[] variableAssignments, final int[] unitLiteralState, final IntegerArrayQueue unitLiterals, final Constraint[] reasonClauses, final IntegerStack literalsToPropagate) {
        while (literalsToPropagate.hasNext()) {
            final int currentLiteral = literalsToPropagate.pop();
            final int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (variableAssignments[currentLiteralAbsoluteValue] * currentLiteral < 0 || unitLiteralState[currentLiteralAbsoluteValue] * currentLiteral < 0) {
                this.hasConflict = true;
                this.conflictLiteral = currentLiteral;
                return true;
            } else if (this.isNeededForUnitPropagation(currentLiteralAbsoluteValue, variableAssignments, unitLiteralState)) {
                unitLiterals.offer(currentLiteral);
                unitLiteralState[currentLiteralAbsoluteValue] = currentLiteral < 0 ? -1 : 1;
                if (reasonClauses[currentLiteralAbsoluteValue] == null) {
                    reasonClauses[currentLiteralAbsoluteValue] = this;
                }
            }

        }
        return false;
    }

    private boolean isNeededForUnitPropagation(final int literal, final int[] variables, final int[] unitLiteralState) {
        final int literalAbsoluteValue = Math.abs(literal);
        return variables[literalAbsoluteValue] * literal == 0 && unitLiteralState[literalAbsoluteValue] == 0;
    }


    public int[][] getTerms() {
        return terms;
    }

}
