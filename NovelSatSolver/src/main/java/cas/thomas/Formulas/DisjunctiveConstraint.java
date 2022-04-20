package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDisjunctiveConstraint;
import cas.thomas.utils.IntegerArrayQueue;
import cas.thomas.utils.IntegerStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DisjunctiveConstraint extends Constraint {

    public DisjunctiveConstraint(int[] literals, List<Constraint>[] positivelyWatchedList,
                                 List<Constraint>[] negativelyWatchedList) {
        super();

        assert (literals.length >= 1);

        this.literals = literals;

        assignWatchedLiteralsToWatchList(positivelyWatchedList, negativelyWatchedList);
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

    public DisjunctiveConstraint(int[] literals) {
        super();
        assert (literals.length >= 1);
        this.literals = literals;
    }

    @Override
    public boolean propagate(int propagatedLiteral, int[] variableAssignments, int[] unitLiteralState,
                             IntegerArrayQueue unitLiterals,
                             List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched,
                             Constraint[] reasonClauses) {


        int firstWatchedLiteral = literals[0];
        int firstWatchedLiteralAbsoluteValue = Math.abs(firstWatchedLiteral);

        if (literals.length == 1) {
            propagateIfConstraintHasOnlyOneLiteral(variableAssignments, unitLiteralState,
                    firstWatchedLiteralAbsoluteValue,
                    firstWatchedLiteral, unitLiterals, reasonClauses);
            return true;
        }


        int secondWatchedLiteral = literals[1];
        int secondWatchedLiteralAbsoluteValue = Math.abs(secondWatchedLiteral);

        return propagateOrReplaceWatchedLiteral(propagatedLiteral, variableAssignments, unitLiteralState, unitLiterals, positivelyWatched, negativelyWatched, reasonClauses, firstWatchedLiteral, firstWatchedLiteralAbsoluteValue, secondWatchedLiteral, secondWatchedLiteralAbsoluteValue);
    }

    @Override
    public SolutionCheckerConstraint getSolutionCheckerConstraint() {
        return new SolutionCheckerDisjunctiveConstraint(literals);
    }

    @Override
    public List<Constraint> handleConflict(int numberOfVariables, IntegerStack trail, int[] variableDecisionLevel, int[] variablesInvolvedInConflict, Formula formula) {

        int[] literals = this.literals;

        int[] stateOfResolvedVariables = new int[numberOfVariables];

        for (int i = 0; i < literals.length; i++) {
            int currentLiteralAbsoluteValue = Math.abs(literals[i]);
            stateOfResolvedVariables[currentLiteralAbsoluteValue] = literals[i];
            variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;
        }

        trail.prepareIterationWithoutPop();
        while (trail.hasNextWithoutPop()) {
            int literal = trail.peekNextWithoutPop();
            Constraint reasonClause = formula.getReasonClauses(literal);

            int counter = 0;
            boolean resolve = false;
            for (int i = 0; i < literals.length; i++) {
                int currentLiteral = literals[i];
                int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

                if (variableDecisionLevel[Math.abs(currentLiteralAbsoluteValue)] == formula.getCurrentDecisionLevel()) {
                    counter++;
                }

                if (currentLiteralAbsoluteValue == Math.abs(literal)) {
                    formula.setConflictLiteral(currentLiteral);
                    resolve = true;
                }
            }


            if (counter == 1) {
                break;
            }

            if (!resolve) {
                continue;
            }

            if (reasonClause != null) {
                if (reasonClause.getConstraintType() != ConstraintType.DISJUNCTIVE) {
                    return reasonClause.resolveConflict(new DisjunctiveConstraint(literals), trail,
                            stateOfResolvedVariables
                            , formula, variablesInvolvedInConflict);

                }
            } else {
                continue;
            }


            literals = resolveClauses(formula.getNumberOfVariables(), literals,
                    reasonClause, Math.abs(literal), variablesInvolvedInConflict);

        }

        int[] learnedConstraintLiterals =
                Arrays.stream(literals).boxed().sorted(Comparator.comparingInt(a -> variableDecisionLevel[Math.abs(a)] * -1)).mapToInt(i -> i).toArray();

        return Arrays.asList(formula.addDisjunctiveConstraint(learnedConstraintLiterals));


    }

    @Override
    public List<Constraint> resolveConflict(Constraint conflictConstraint, IntegerStack trail,
                                            int[] stateOfResolvedVariables, Formula formula, int[] variablesInvolvedInConflict) {
        return null;
    }

    @Override
    public List<Constraint> resolveConflict(AMOConstraint conflictConstraint, IntegerStack trail,
                                            int[] stateOfResolvedVariables,
                                            Formula formula, int[] variablesInvolvedInConflict) {

        int[] reasonLiterals = literals;
        int[] conflictLiterals = conflictConstraint.getLiterals();
        int conflictLiteral = -formula.getConflictLiteral();
        IntegerArrayQueue clauseLiterals = new IntegerArrayQueue(reasonLiterals.length);
        IntegerArrayQueue amoLiterals = new IntegerArrayQueue(conflictLiterals.length);
        IntegerArrayQueue complementaryLiterals = new IntegerArrayQueue(Math.max(reasonLiterals.length,
                conflictLiterals.length));
        List<Constraint> learnedConstraints = new ArrayList<>();

        findComplementaryLiteralsAndNeededClauseLiterals(stateOfResolvedVariables, variablesInvolvedInConflict,
                reasonLiterals, clauseLiterals, complementaryLiterals, conflictLiteral);

        if (complementaryLiterals.size() >= 1) {
            return resolveConflictWithOneComplementaryLiteral(formula, clauseLiterals, complementaryLiterals, learnedConstraints);
        }

        findNeededAMOLiterals(stateOfResolvedVariables, conflictLiterals, amoLiterals, formula.getVariables());


        return resolveConflictWithMoreThanOneComplementaryLiteral(formula, clauseLiterals, amoLiterals, learnedConstraints);
    }

    private void findComplementaryLiteralsAndNeededClauseLiterals(int[] stateOfResolvedVariables,
                                                                  int[] variablesInvolvedInConflict,
                                                                  int[] reasonLiterals,
                                                                  IntegerArrayQueue clauseLiterals,
                                                                  IntegerArrayQueue complementaryLiterals,
                                                                  int conflictLiteral) {
        for (int i = 0; i < reasonLiterals.length; i++) {
            int currentLiteral = reasonLiterals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);
            variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;

            if (stateOfResolvedVariables[currentLiteralAbsoluteValue] == -currentLiteral) {
                complementaryLiterals.offer(currentLiteral);
                stateOfResolvedVariables[currentLiteralAbsoluteValue] = 0;
            } else if (stateOfResolvedVariables[currentLiteralAbsoluteValue] == currentLiteral && currentLiteral == conflictLiteral) {
                stateOfResolvedVariables[currentLiteralAbsoluteValue] = 0;
            } else {
                clauseLiterals.offer(currentLiteral);
            }
        }
    }

    private List<Constraint> resolveConflictWithOneComplementaryLiteral(Formula formula, IntegerArrayQueue clauseLiterals, IntegerArrayQueue complementaryLiterals, List<Constraint> learnedConstraints) {
        int[] clauseLiteralsArray = Arrays.copyOf(clauseLiterals.getInternalArray(), clauseLiterals.size() + 1);
        clauseLiteralsArray[clauseLiteralsArray.length - 1] = complementaryLiterals.poll();
        clauseLiteralsArray =
                Arrays.stream(clauseLiteralsArray).boxed().sorted(Comparator.comparingInt(a -> formula.getDecisionLevelOfVariables()[Math.abs(a)] * -1)).mapToInt(a -> a).toArray();

        learnedConstraints.add(formula.addDisjunctiveConstraint(clauseLiteralsArray));

        return learnedConstraints;
    }

    private void findNeededAMOLiterals(int[] stateOfResolvedVariables, int[] conflictLiterals,
                                       IntegerArrayQueue amoLiterals, int[] variableAssignments) {
        for (int i = 0; i < conflictLiterals.length; i++) {
            int currentLiteral = conflictLiterals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (stateOfResolvedVariables[currentLiteralAbsoluteValue] != 0) {
                if (variableAssignments[currentLiteralAbsoluteValue] * currentLiteral > 0) {
                    amoLiterals.offer(-currentLiteral);
                }
            }
        }
    }

    private List<Constraint> resolveConflictWithMoreThanOneComplementaryLiteral(Formula formula, IntegerArrayQueue clauseLiterals, IntegerArrayQueue amoLiterals, List<Constraint> learnedConstraints) {
        int[] clauseLiteralsArray = Arrays.copyOf(clauseLiterals.getInternalArray(), clauseLiterals.size() + 1);
        int[] amoLiteralsArray = amoLiterals.getInternalArray();

        for (int i = 0; i < amoLiteralsArray.length; i++) {
            clauseLiteralsArray[clauseLiterals.size()] = amoLiteralsArray[i];
            int[] sortedClauseLiteralsArray =
                    Arrays.stream(clauseLiteralsArray).boxed().sorted(Comparator.comparingInt(a -> formula.getDecisionLevelOfVariables()[Math.abs(a)] * -1)).mapToInt(a -> a).toArray();
            learnedConstraints.add(formula.addDisjunctiveConstraint(Arrays.copyOf(sortedClauseLiteralsArray,
                    clauseLiteralsArray.length)));
        }

        return learnedConstraints;
    }

    @Override
    public List<Constraint> resolveConflict(DNFConstraint conflictConstraint, IntegerStack trail, int[] stateOfResolvedVariables, Formula formula, int[] variablesInvolvedInConflict) {
        int[] reasonLiterals = this.literals;
        int[][] conflictTerms = conflictConstraint.getTerms();
        int conflictLiteral = formula.getConflictLiteral();

        ArrayList<int[]> resolutionTerms = new ArrayList<>();

        getResolutionTermsFomrReasonDisjunctiveConstraint(variablesInvolvedInConflict, reasonLiterals, conflictLiteral, resolutionTerms);

        getResolutionTermsFromConflictDNFConstraint(conflictTerms, conflictLiteral, resolutionTerms);


        return Arrays.asList(formula.addDNFConstraints(resolutionTerms.toArray(int[][]::new)));
    }

    private void getResolutionTermsFomrReasonDisjunctiveConstraint(int[] variablesInvolvedInConflict, int[] reasonLiterals, int conflictLiteral, ArrayList<int[]> resolutionTerms) {
        for (int i = 0; i < reasonLiterals.length; i++) {
            int currentLiteral = reasonLiterals[i];
            variablesInvolvedInConflict[Math.abs(currentLiteral)] = 1;
            if (currentLiteral != -conflictLiteral) {
                resolutionTerms.add(new int[]{currentLiteral});
            }
        }
    }

    private void getResolutionTermsFromConflictDNFConstraint(int[][] conflictTerms, int conflictLiteral, ArrayList<int[]> resolutionTerms) {
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
        return ConstraintType.DISJUNCTIVE;
    }

    @Override
    public boolean isUnitConstraint() {
        return literals.length == 1;
    }

    @Override
    public int[] getUnitLiterals() {
        if (isUnitConstraint()) {
            return literals;
        } else {
            return new int[0];
        }
    }

    @Override
    public boolean isEmpty() {
        return literals.length == 0;
    }

    @Override
    public int getNeededDecisionLevel(int[] decisionLevelOfVariables, int[] variables) {
        if (literals.length == 1) {
            return decisionLevelOfVariables[Math.abs(literals[0])];
        } else {
            return decisionLevelOfVariables[Math.abs(literals[1])];
        }


    }

    @Override
    public void addVariableOccurenceCount(double[] variableOccurences) {
        for (int i = 0; i < literals.length; i++) {
            variableOccurences[Math.abs(literals[i])] += 1;
        }
    }

    @Override
    public boolean isStillWatched(int literal, int[] variables) {
        return false;
    }

    @Override
    public String toString() {
        String constraint = "[";

        for (int i = 0; i < literals.length; i++) {
            constraint += " " + literals[i];
        }

        constraint += "]";

        return constraint;
    }

    @Override
    public Set<Integer> getUnitLiteralsNeededBeforePropagation() {
        if (literals.length == 1) {
            return new HashSet<>(Arrays.asList(literals[0]));
        } else {
            return new HashSet<>();
        }
    }

    public int[] resolveClauses(int numberOfVariables, int[] conflictLiterals,
                                Constraint reasonClause, int conflictLiteral, int[] variablesInvolvedInConflict) {

        int[] stateOfResolvedVariables = new int[numberOfVariables];
        int[] reasonClauseLiterals = reasonClause.getLiterals();
        IntegerArrayQueue newConflictLiteralsQueue =
                new IntegerArrayQueue(conflictLiterals.length + reasonClauseLiterals.length - 2);

        int maxLength = Math.max(conflictLiterals.length, reasonClauseLiterals.length);

        for (int i = 0; i < maxLength; i++) {

            findNeededLiteralsForClauseClauseConflictResolution(conflictLiterals, conflictLiteral, variablesInvolvedInConflict, stateOfResolvedVariables, newConflictLiteralsQueue, i);

            findNeededLiteralsForClauseClauseConflictResolution(reasonClauseLiterals, conflictLiteral, variablesInvolvedInConflict, stateOfResolvedVariables, newConflictLiteralsQueue, i);

        }

        return newConflictLiteralsQueue.getInternalArray();
    }

    private void findNeededLiteralsForClauseClauseConflictResolution(int[] conflictLiterals, int conflictLiteral, int[] variablesInvolvedInConflict, int[] stateOfResolvedVariables, IntegerArrayQueue newConflictLiteralsQueue, int i) {
        if (i < conflictLiterals.length) {
            int currentLiteral = conflictLiterals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);
            variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;

            if (stateOfResolvedVariables[currentLiteralAbsoluteValue] != 1) {
                if (currentLiteralAbsoluteValue != conflictLiteral) {
                    stateOfResolvedVariables[currentLiteralAbsoluteValue] = 1;
                    newConflictLiteralsQueue.offer(currentLiteral);
                }
            }

        }
    }

    private void propagateIfConstraintHasOnlyOneLiteral(int[] variableAssignments, int[] unitLiteralState,
                                                        int firstWatchedLiteralAbsoluteValue,
                                                        int firstWatchedLiteral, IntegerArrayQueue unitLiterals,
                                                        Constraint[] reasonClauses) {
        if (variableAssignments[firstWatchedLiteralAbsoluteValue] * firstWatchedLiteral < 0 || unitLiteralState[firstWatchedLiteralAbsoluteValue] * firstWatchedLiteral < 0) {
            hasConflict = true;
            conflictLiteral = firstWatchedLiteral;
            return;
        } else if (isNeededForUnitPropagation(firstWatchedLiteral, variableAssignments, unitLiteralState)) {
            unitLiterals.offer(firstWatchedLiteral);
            unitLiteralState[firstWatchedLiteralAbsoluteValue] = firstWatchedLiteral < 0 ? -1 : 1;
            if (reasonClauses[firstWatchedLiteralAbsoluteValue] == null) {
                reasonClauses[firstWatchedLiteralAbsoluteValue] = this;
            }
        }

    }

    private boolean propagateOrReplaceWatchedLiteral(int propagatedLiteral, int[] variableAssignments, int[] unitLiteralState, IntegerArrayQueue unitLiterals, List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched, Constraint[] reasonClauses, int firstWatchedLiteral, int firstWatchedLiteralAbsoluteValue, int secondWatchedLiteral, int secondWatchedLiteralAbsoluteValue) {
        boolean unitPropagation = true;

        if (firstWatchedLiteral == -propagatedLiteral) {
            unitPropagation = propagateWatchedLiteral(variableAssignments, unitLiteralState, positivelyWatched,
                    negativelyWatched,
                    true, secondWatchedLiteral, secondWatchedLiteralAbsoluteValue, unitLiterals,
                    reasonClauses);

        } else if (secondWatchedLiteral == -propagatedLiteral) {
            unitPropagation = propagateWatchedLiteral(variableAssignments, unitLiteralState, positivelyWatched,
                    negativelyWatched,
                    false, firstWatchedLiteral, firstWatchedLiteralAbsoluteValue, unitLiterals,
                    reasonClauses);
        }

        return unitPropagation;
    }

    protected boolean isNeededForUnitPropagation(int literal, int[] variables, int[] unitLiteralState) {
        int literalAbsoluteValue = Math.abs(literal);
        return variables[literalAbsoluteValue] * literal == 0 && unitLiteralState[literalAbsoluteValue] == 0;
    }

    private boolean propagateWatchedLiteral(int[] variableAssignments, int[] unitLiteralState,
                                            List<Constraint>[] positivelyWatched,
                                            List<Constraint>[] negativelyWatched,
                                            boolean firstLiteral,
                                            int unitLiteralCandidate, int unitLiteralCandidateAbsoluteValue,
                                            IntegerArrayQueue unitLiterals,
                                            Constraint[] reasonClauses) {

        for (int i = 2; i < literals.length; i++) {
            if (findWatchedLiteralReplacement(variableAssignments, positivelyWatched, negativelyWatched, firstLiteral, i)) {
                return false;
            }
        }

        return assignUnitLiteral(variableAssignments, unitLiteralState, unitLiteralCandidate, unitLiteralCandidateAbsoluteValue, unitLiterals, reasonClauses);
    }

    private boolean findWatchedLiteralReplacement(int[] variableAssignments, List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched, boolean firstLiteral, int i) {
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

            return true;
        }
        return false;
    }

    private boolean assignUnitLiteral(int[] variableAssignments, int[] unitLiteralState, int unitLiteralCandidate, int unitLiteralCandidateAbsoluteValue, IntegerArrayQueue unitLiterals, Constraint[] reasonClauses) {
        if (variableAssignments[unitLiteralCandidateAbsoluteValue] * unitLiteralCandidate < 0 || unitLiteralState[unitLiteralCandidateAbsoluteValue] * unitLiteralCandidate < 0) {
            hasConflict = true;
            conflictLiteral = unitLiteralCandidate;
            return true;
        }

        if (isNeededForUnitPropagation(unitLiteralCandidateAbsoluteValue, variableAssignments, unitLiteralState)) {
            unitLiterals.offer(unitLiteralCandidate);
            unitLiteralState[unitLiteralCandidateAbsoluteValue] = unitLiteralCandidate < 0 ? -1 : 1;
            if (reasonClauses[unitLiteralCandidateAbsoluteValue] == null) {
                reasonClauses[unitLiteralCandidateAbsoluteValue] = this;
            }
        }

        return true;
    }


}
