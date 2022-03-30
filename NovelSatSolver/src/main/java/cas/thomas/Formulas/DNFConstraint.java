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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DNFConstraint extends Constraint {

    protected int[][] terms;
    protected Set<Integer> unitLiteralsPropagatedDuringInitialization;
    private Map<Integer, Set<Integer>> literalIntersections;
    private Set<Integer>[] termSets;
    private int[] falseTermCache;
    private Set<Integer> visitedLiterals;

    public DNFConstraint(final int[][] terms, final List<Constraint>[] positivelyWatchedList,
                         final List<Constraint>[] negativelyWatchedList) {

        assert (terms.length > 0);

        this.terms = terms;
        this.falseTermCache = new int[terms.length];
        this.visitedLiterals = new HashSet<>();
        this.unitLiteralsPropagatedDuringInitialization = new HashSet<>();

        Arrays.sort(terms, Comparator.comparing(term -> term.length));

        if (terms.length == 1) {
            for (int i = 0; i < terms[0].length; i++) {
                this.unitLiteralsPropagatedDuringInitialization.add(terms[0][i]);
            }
            return;
        }

        this.literalIntersections = new HashMap<>();
        this.termSets = new HashSet[terms.length];
        this.unitLiteralsPropagatedDuringInitialization = new HashSet<>();

        final Set<Integer> literalsInDNF = new HashSet<>();


        this.calculateTermIntersectionsAndUnitLiterals(terms, literalsInDNF);

        this.terms = terms;

        this.assignWatchedLiteralsToWatchList(literalsInDNF, positivelyWatchedList, negativelyWatchedList);
    }

    private void calculateTermIntersectionsAndUnitLiterals(final int[][] terms, final Set<Integer> literalsInDNF) {
        for (int i = 0; i < terms.length; i++) {
            assert (terms[i].length > 0);

            this.terms[i] = terms[i];
            final Set<Integer> termSet = new HashSet<>();
            termSets[i] = termSet;


            for (int j = 0; j < terms[i].length; j++) {
                final int currentLiteral = terms[i][j];
                literalsInDNF.add(currentLiteral);
                termSet.add(currentLiteral);
                if (this.literalIntersections.containsKey(currentLiteral)) {
                    final Set<Integer> intersectionList = this.literalIntersections.get(currentLiteral);
                    intersectionList.add(i);

                    if (intersectionList.size() == terms.length) {
                        this.unitLiteralsPropagatedDuringInitialization.add(currentLiteral);
                    }

                } else {
                    final Set<Integer> newList = new HashSet<>();
                    newList.add(i);
                    this.literalIntersections.put(currentLiteral, newList);
                }
            }

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

        this.visitedLiterals.add(-propagatedLiteral);
        if (this.terms.length == 1) {
            this.propagateIfConstraintHasOnlyOneTerm(0, variableAssignments, unitLiteralState, unitLiterals, reasonClauses);
            return true;
        }

        final IntegerStack literalsToPropagate = new IntegerStack();

        final Set<Integer> falseTerms = this.literalIntersections.get(-propagatedLiteral);

        for (final Integer termIndex : falseTerms) {
            this.falseTermCache[termIndex] = -1;

        }

        int firstNonFalseTermIndex = -1;
        int nonFalseCounter = 0;
        for (int i = 0; i < this.falseTermCache.length; i++) {
            if (firstNonFalseTermIndex == -1 && this.falseTermCache[i] != -1) {
                firstNonFalseTermIndex = i;
                nonFalseCounter++;
            } else if (this.falseTermCache[i] != -1) {
                nonFalseCounter++;
            }
        }

        if (firstNonFalseTermIndex == this.falseTermCache.length - 1) {
            this.propagateIfConstraintHasOnlyOneTerm(firstNonFalseTermIndex, variableAssignments, unitLiteralState,
                    unitLiterals,
                    reasonClauses);
            return true;
        }

        if (firstNonFalseTermIndex == -1) {
            return false;
        }

        final int[] shortestNonFalseTerm = this.terms[firstNonFalseTermIndex];

        this.findLiteralsInIntersectionOfAllNonFalseTerms(literalsToPropagate, firstNonFalseTermIndex, nonFalseCounter, shortestNonFalseTerm);

        final boolean propagate = literalsToPropagate.size() > 0;

        if (this.addUnitLiterals(variableAssignments, unitLiteralState, unitLiterals, reasonClauses, literalsToPropagate))
            return true;

        return propagate;
    }

    @Override
    public SolutionCheckerConstraint getSolutionCheckerConstraint() {
        return new SolutionCheckerDNFConstraint(terms);
    }

    public int getLBDScore(final int[] variableDecisionLevels) {
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
        return this.terms.length == 1;
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
    public int getNeededDecisionLevel(final int[] decisionLevelOfVariables) {

        int decisionLevel = Integer.MAX_VALUE;

        for (int i = 0; i < this.terms.length; i++) {
            for (int j = 0; j < this.terms[i].length; j++) {
                decisionLevel = Math.min(decisionLevel, decisionLevelOfVariables[Math.abs(this.terms[i][j])]);
            }
        }

        return decisionLevel;

    }

    @Override
    public void addVariableOccurenceCount(final double[] variableOccurences) {
        for (int i = 0; i < this.terms.length; i++) {
            for (int j = 0; j < this.terms[i].length; j++) {
                variableOccurences[Math.abs(this.terms[i][j])] += 1;
            }
        }
    }

    @Override
    public boolean isStillWatched(final int literal) {

        return !this.visitedLiterals.contains(-literal);
    }

    public void backtrack(final int variable, final int[] variableAssingments) {

        final Set<Integer> falseTerms = this.literalIntersections.get(-variable);
        this.visitedLiterals.remove(-variable);

        for (final Integer termIndex : falseTerms) {
            if (!this.checkIfTermIsFalseExludingLiteral(this.terms[termIndex], variableAssingments, -variable)) {
                this.falseTermCache[termIndex] = 0;
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

    private void findLiteralsInIntersectionOfAllNonFalseTerms(final IntegerStack literalsToPropagate, final int firstNonFalseTermIndex, final int nonFalseCounter, final int[] shortestNonFalseTerm) {
        for (int i = 0; i < shortestNonFalseTerm.length; i++) {
            final int currentLiteral = shortestNonFalseTerm[i];

            int trueCounterIntersection = 0;
            for (final Integer termIndex : this.literalIntersections.get(currentLiteral)) {
                if (termIndex != firstNonFalseTermIndex && this.falseTermCache[termIndex] == 0) {
                    trueCounterIntersection++;
                }
            }

            if (nonFalseCounter - 1 == trueCounterIntersection) {
                literalsToPropagate.push(currentLiteral);
            }
        }
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
