package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerAMOConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.utils.IntegerArrayQueue;
import cas.thomas.utils.IntegerStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AMOConstraint extends Constraint {

    private int[] binarySearchLiterals;

    public AMOConstraint(int[] literals, List<Constraint>[] positivelyWatchedList,
                         List<Constraint>[] negativelyWatchedList) {
        super();
        this.literals = literals;
        this.binarySearchLiterals = Arrays.copyOf(literals, literals.length);
        Arrays.sort(binarySearchLiterals);

        for (int i = 0; i < literals.length; i++) {
            int currentLiteral = literals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (currentLiteral < 0) {
                negativelyWatchedList[currentLiteralAbsoluteValue].add(this);
            } else {
                positivelyWatchedList[currentLiteralAbsoluteValue].add(this);
            }
        }
    }

    @Override
    public boolean propagate(int propagatedLiteral, int[] variableAssignments, int[] unitLiteralState,
                             IntegerArrayQueue unitLiterals,
                             List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched,
                             Constraint[] reasonClauses) {

        for (int i = 0; i < literals.length; i++) {
            int currentLiteral = literals[i];

            if (propagatedLiteral == currentLiteral) {
                for (int a = 0; a < literals.length; a++) {
                    int unitLiteralCandidate = -literals[a];
                    int unitLiteralCandidateAbsoluteValue = Math.abs(unitLiteralCandidate);

                    if (a != i) {

                        if (addUnitLiteral(variableAssignments, unitLiteralState, unitLiterals, reasonClauses, unitLiteralCandidate, unitLiteralCandidateAbsoluteValue))
                            return true;
                    }
                }

                return true;
            }
        }

        return false;
    }

    private boolean addUnitLiteral(int[] variableAssignments, int[] unitLiteralState, IntegerArrayQueue unitLiterals, Constraint[] reasonClauses, int unitLiteralCandidate, int unitLiteralCandidateAbsoluteValue) {
        if (variableAssignments[unitLiteralCandidateAbsoluteValue] * unitLiteralCandidate < 0 || unitLiteralState[unitLiteralCandidateAbsoluteValue] * unitLiteralCandidate < 0) {
            hasConflict = true;
            conflictLiteral = unitLiteralCandidate;
            return true;
        } else if (isNeededForUnitPropagation(unitLiteralCandidate, variableAssignments,
                unitLiteralState)) {
            unitLiterals.offer(unitLiteralCandidate);
            unitLiteralState[unitLiteralCandidateAbsoluteValue] = unitLiteralCandidate < 0 ? -1 : 1;
            if (reasonClauses[unitLiteralCandidateAbsoluteValue] == null) {
                reasonClauses[unitLiteralCandidateAbsoluteValue] = this;
            }
        }
        return false;
    }

    private boolean isNeededForUnitPropagation(int literal, int[] variables, int[] unitLiteralState) {
        int literalAbsoluteValue = Math.abs(literal);
        return variables[literalAbsoluteValue] * literal == 0 && unitLiteralState[literalAbsoluteValue] == 0;
    }


    @Override
    public SolutionCheckerConstraint getSolutionCheckerConstraint() {
        return new SolutionCheckerAMOConstraint(this.literals);
    }

    @Override
    public List<Constraint> handleConflict(int numberOfVariables, IntegerStack trail,
                                           int[] variableDecisionLevel, int[] variablesInvolvedInConflict, Formula formula) {

        int[] literals = this.literals;

        int[] stateOfResolvedVariables = new int[numberOfVariables];

        for (int i = 0; i < literals.length; i++) {
            int currentLiteralAbsoluteValue = Math.abs(literals[i]);
            stateOfResolvedVariables[currentLiteralAbsoluteValue] = literals[i];
            variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;
        }

        return findReasonConstraintAndResolveConflict(trail, variablesInvolvedInConflict, formula, stateOfResolvedVariables);
    }

    private List<Constraint> findReasonConstraintAndResolveConflict(IntegerStack trail, int[] variablesInvolvedInConflict, Formula formula, int[] stateOfResolvedVariables) {
        trail.prepareIterationWithoutPop();
        while (trail.hasNextWithoutPop()) {
            int literal = trail.peekNextWithoutPop();
            Constraint reasonConstraint = formula.getReasonClauses(literal);

            if (reasonConstraint == null) {
                continue;
            }

            return reasonConstraint.resolveConflict(this, trail, stateOfResolvedVariables, formula, variablesInvolvedInConflict);
        }

        return Arrays.asList();
    }

    public List<Constraint> resolveConflict(Constraint conflictConstraint, IntegerStack trail,
                                            int[] stateOfResolvedVariables,
                                            Formula formula, int[] variablesInvolvedInConflict) {
        int[] reasonLiterals = literals;
        int[] conflictLiterals = conflictConstraint.getLiterals();
        int conflictLiteral = formula.getConflictLiteral();
        IntegerArrayQueue clauseLiterals = new IntegerArrayQueue(conflictLiterals.length);
        IntegerArrayQueue amoLiterals = new IntegerArrayQueue(reasonLiterals.length);
        IntegerArrayQueue complementaryLiterals = new IntegerArrayQueue(Math.max(reasonLiterals.length,
                conflictLiterals.length));
        List<Constraint> learnedConstraints = new ArrayList<>();

        stateOfResolvedVariables = new int[stateOfResolvedVariables.length];

        for (int i = 0; i < conflictLiterals.length; i++) {
            int currentLiteralAbsoluteValue = Math.abs(conflictLiterals[i]);
            stateOfResolvedVariables[currentLiteralAbsoluteValue] = conflictLiterals[i];
        }

        findNeededAMOLiteralsAndComplementaryLiterals(stateOfResolvedVariables, variablesInvolvedInConflict,
                reasonLiterals, amoLiterals, complementaryLiterals, conflictLiteral, formula.getVariables());

        findNeededClauseLiterals(stateOfResolvedVariables, conflictLiterals, clauseLiterals);


        if (complementaryLiterals.size() >= 1) {
            return resolveConflictWithAtLeastOneComplementaryLiteral(formula, clauseLiterals, complementaryLiterals, learnedConstraints);
        }


        return resolveConflictWithNoComplementaryLiteral(formula, clauseLiterals, amoLiterals, learnedConstraints);
    }

    private void findNeededAMOLiteralsAndComplementaryLiterals(int[] stateOfResolvedVariables,
                                                               int[] variablesInvolvedInConflict,
                                                               int[] reasonLiterals, IntegerArrayQueue amoLiterals,
                                                               IntegerArrayQueue complementaryLiterals,
                                                               int conflictLiteral, int[] variableAssignments) {
        for (int i = 0; i < reasonLiterals.length; i++) {
            int currentLiteral = reasonLiterals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);
            variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;

            if (stateOfResolvedVariables[currentLiteralAbsoluteValue] == -currentLiteral) {
                complementaryLiterals.offer(-currentLiteral);
                stateOfResolvedVariables[currentLiteralAbsoluteValue] = 0;
            } else if (stateOfResolvedVariables[currentLiteralAbsoluteValue] == currentLiteral && currentLiteral == conflictLiteral) {
                stateOfResolvedVariables[currentLiteralAbsoluteValue] = 0;
            } else {
                if (variableAssignments[currentLiteralAbsoluteValue] * currentLiteral > 0) {
                    amoLiterals.offer(-currentLiteral);
                }
            }
        }
    }

    private void findNeededClauseLiterals(int[] stateOfResolvedVariables, int[] conflictLiterals, IntegerArrayQueue clauseLiterals) {
        for (int i = 0; i < conflictLiterals.length; i++) {
            int currentLiteral = conflictLiterals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (stateOfResolvedVariables[currentLiteralAbsoluteValue] != 0) {
                clauseLiterals.offer(currentLiteral);
            }
        }
    }

    private List<Constraint> resolveConflictWithAtLeastOneComplementaryLiteral(Formula formula, IntegerArrayQueue clauseLiterals, IntegerArrayQueue complementaryLiterals, List<Constraint> learnedConstraints) {
        int[] clauseLiteralsArray = Arrays.copyOf(clauseLiterals.getInternalArray(), clauseLiterals.size() + 1);
        clauseLiteralsArray[clauseLiteralsArray.length - 1] = complementaryLiterals.poll();
        clauseLiteralsArray =
                Arrays.stream(clauseLiteralsArray).boxed().sorted(Comparator.comparingInt(a -> formula.getDecisionLevelOfVariables()[Math.abs(a)] * -1)).mapToInt(a -> a).toArray();

        learnedConstraints.add(formula.addDisjunctiveConstraint(clauseLiteralsArray));

        return learnedConstraints;
    }

    private List<Constraint> resolveConflictWithNoComplementaryLiteral(Formula formula, IntegerArrayQueue clauseLiterals, IntegerArrayQueue amoLiterals, List<Constraint> learnedConstraints) {
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

    public List<Constraint> resolveConflict(AMOConstraint conflictConstraint, IntegerStack trail,
                                            int[] stateOfResolvedVariables,
                                            Formula formula, int[] variablesInvolvedInConflict) {
        int[] conflictLiterals = conflictConstraint.getLiterals();
        int[] literalSharingStatus = new int[formula.getNumberOfVariables()];
        List<Constraint> learnedConstraints = new ArrayList<>();
        int complementaryLiteralsCounter = getComplementaryLiteralsCounter(stateOfResolvedVariables, variablesInvolvedInConflict, literalSharingStatus);

        if (complementaryLiteralsCounter >= 3) {
            return Arrays.asList();
        }

        if (complementaryLiteralsCounter == 2) {
            return resolveAMOAMOConflictWithTwoComplementaryLiterals(formula, conflictLiterals, literalSharingStatus, learnedConstraints);
        }

        if (complementaryLiteralsCounter >= 1) {
            return resolveAMOMOConflictWithOneComplementaryLiteral(formula, conflictLiterals, literalSharingStatus,
                    learnedConstraints, formula.getVariables());
        }

        return learnedConstraints;
    }

    private int getComplementaryLiteralsCounter(int[] stateOfResolvedVariables, int[] variablesInvolvedInConflict, int[] literalSharingStatus) {
        int negatedLiteralsCounter = 0;

        for (int i = 0; i < literals.length; i++) {
            int currentLiteral = literals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);
            variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;

            if (stateOfResolvedVariables[currentLiteralAbsoluteValue] == currentLiteral) {
                literalSharingStatus[currentLiteralAbsoluteValue] = 1;
            } else if (stateOfResolvedVariables[currentLiteralAbsoluteValue] == -currentLiteral) {
                negatedLiteralsCounter++;
                literalSharingStatus[currentLiteralAbsoluteValue] = -1;
            }
        }
        return negatedLiteralsCounter;
    }

    private List<Constraint> resolveAMOAMOConflictWithTwoComplementaryLiterals(Formula formula, int[] conflictLiterals, int[] literalSharingStatus, List<Constraint> learnedConstraints) {
        for (int i = 0; i < conflictLiterals.length; i++) {
            int currentLiteral = conflictLiterals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (literalSharingStatus[currentLiteralAbsoluteValue] != -1) {
                learnedConstraints.add(formula.addDisjunctiveConstraint(new int[]{-currentLiteral}));
            }
        }

        for (int i = 0; i < literals.length; i++) {
            int currentLiteral = literals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (literalSharingStatus[currentLiteralAbsoluteValue] != -1) {
                learnedConstraints.add(formula.addDisjunctiveConstraint(new int[]{-currentLiteral}));
            }
        }

        return learnedConstraints;
    }

    private List<Constraint> resolveAMOMOConflictWithOneComplementaryLiteral(Formula formula, int[] conflictLiterals,
                                                                             int[] literalSharingStatus,
                                                                             List<Constraint> learnedConstraints,
                                                                             int[] variableAssignments) {
        for (int i = 0; i < conflictLiterals.length; i++) {
            int currentLiteralConflict = conflictLiterals[i];
            int currentLiteralConflictAbsoluteValue = Math.abs(currentLiteralConflict);

            if (literalSharingStatus[currentLiteralConflictAbsoluteValue] == -1) {
                continue;
            } else if (literalSharingStatus[currentLiteralConflictAbsoluteValue] == 1) {
                learnedConstraints.add(formula.addDisjunctiveConstraint(new int[]{-currentLiteralConflict}));
            }

            if (variableAssignments[currentLiteralConflictAbsoluteValue] * currentLiteralConflict <= 0) {
                continue;
            }

            for (int j = 0; j < literals.length; j++) {
                int currentLiteralReason = literals[j];
                int currentLiteralReasonAbsoluteValue = Math.abs(currentLiteralReason);

                if (literalSharingStatus[currentLiteralReasonAbsoluteValue] != 0) {
                    continue;
                }

                if (variableAssignments[currentLiteralReasonAbsoluteValue] * currentLiteralReason <= 0) {
                    continue;
                }

                learnedConstraints.add(formula.addDisjunctiveConstraint(new int[]{-currentLiteralConflict,
                        -currentLiteralReason}));

            }
        }

        return learnedConstraints;
    }

    @Override
    public List<Constraint> resolveConflict(DNFConstraint conflictConstraint, IntegerStack trail, int[] stateOfResolvedVariables, Formula formula, int[] variablesInvolvedInConflict) {
        /*int[] reasonLiterals = this.literals;
        int[][] conflictTerms = conflictConstraint.getTerms();
        int conflictLiteral = formula.getConflictLiteral();

        ArrayList<int[]> resolutionTerms = new ArrayList<>();
        getResolutionTermsFromReasonAMOConstraint(variablesInvolvedInConflict, reasonLiterals, conflictLiteral, resolutionTerms);

        getResolutionTermsFromConflictingDNFConstraint(conflictTerms, conflictLiteral, resolutionTerms);

        return Arrays.asList(formula.addDNFConstraints(resolutionTerms.toArray(int[][]::new)));*/

        int conflictLiteral = formula.getConflictLiteral();
        int[] conflictLiterals = conflictConstraint.getLiterals();
        int[] variableAssignments = formula.getVariables();

        Set<Integer> resolutionLiterals = new HashSet<>();

        conflictConstraint.getConflictResolutionLiterals(-conflictLiteral, variableAssignments, resolutionLiterals);

        for (int i = 0; i < literals.length; i++) {
            if (literals[i] != conflictLiteral) {
                resolutionLiterals.add(-literals[i]);
            }
        }

        Integer[] literals = new Integer[resolutionLiterals.size()];

        int pointer = 0;
        for (Integer literal : resolutionLiterals) {
            variablesInvolvedInConflict[Math.abs(literal)] = 1;
            literals[pointer] = literal;
            pointer++;
        }

        Arrays.sort(literals, Comparator.comparingInt(a -> formula.getDecisionLevelOfVariables()[Math.abs(a)] * -1));

        int[] literalsPrimitive = new int[literals.length];

        for (int i = 0 ; i < literals.length; i++) {
            literalsPrimitive[i] = literals[i];
        }

        return Arrays.asList(formula.addDisjunctiveConstraint(literalsPrimitive));
    }

    private void getResolutionTermsFromReasonAMOConstraint(int[] variablesInvolvedInConflict, int[] reasonLiterals, int conflictLiteral, ArrayList<int[]> resolutionTerms) {
        int[] termFromAmoLiterals = new int[reasonLiterals.length - 1];
        int counter = 0;
        for (int i = 0; i < reasonLiterals.length; i++) {
            variablesInvolvedInConflict[Math.abs(reasonLiterals[i])] = 1;
            if (reasonLiterals[i] != conflictLiteral) {
                termFromAmoLiterals[counter] = -reasonLiterals[i];
                counter++;
            }
        }

        resolutionTerms.add(termFromAmoLiterals);
    }

    private void getResolutionTermsFromConflictingDNFConstraint(int[][] conflictTerms, int conflictLiteral, ArrayList<int[]> resolutionTerms) {
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
        return ConstraintType.AMO;
    }

    @Override
    public boolean isUnitConstraint() {
        return false;
    }

    @Override
    public int[] getUnitLiterals() {
        return new int[0];
    }

    @Override
    public boolean isEmpty() {
        return literals.length == 0;
    }

    @Override
    public int getNeededDecisionLevel(int[] decisionLevelOfVariables, int[] variables, Formula formula) {
        int decisionLevel = Integer.MAX_VALUE;

        for (int i = 0; i < literals.length; i++) {
            decisionLevel = Math.min(decisionLevel, decisionLevelOfVariables[Math.abs(literals[i])]);
        }

        return decisionLevel;
    }

    @Override
    public void addVariableOccurenceCount(double[] variableOccurences) {
        for (int i = 0; i < literals.length; i++) {
            variableOccurences[Math.abs(literals[i])] += Math.pow(2, literals.length);
        }
    }

    @Override
    public boolean isStillWatched(int literal, int[] variables) {
        return false;
    }

    @Override
    public String toString() {
        String constraint = "AMO";

        for (int i = 0; i < literals.length; i++) {
            constraint += " " + literals[i];
        }

        return constraint;
    }

    @Override
    public Set<Integer> getUnitLiteralsNeededBeforePropagation() {
        return new HashSet<>();
    }

    @Override
    public boolean containsLiteral(int literal) {
        return Arrays.binarySearch(binarySearchLiterals, literal) >= 0;
    }


}
