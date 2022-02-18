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

    public AMOConstraint(int[] literals, List<Constraint>[] positivelyWatchedList,
                         List<Constraint>[] negativelyWatchedList) {
        super();
        this.literals = literals;

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
                    }
                }

                return true;
            }
        }

        return false;
    }

    private boolean isNeededForUnitPropagation(int literal, int[] variables, int[] unitLiteralState) {
        int literalAbsoluteValue = Math.abs(literal);
        if (variables[literalAbsoluteValue] * literal == 0 && unitLiteralState[literalAbsoluteValue] == 0) {
            return true;
        }

        return false;
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

    public List<Constraint> resolveConflict(Constraint conflictConstraint, IntegerStack trail,
                                            int[] stateOfResolvedVariables ,
                                            Formula formula, int[] variablesInvolvedInConflict) {
        int[] reasonLiterals = literals;
        int[] conflictLiterals = conflictConstraint.getLiterals();
        int[] variableAssignment = formula.getVariables();
        IntegerArrayQueue clauseLiterals = new IntegerArrayQueue(conflictLiterals.length);
        IntegerArrayQueue amoLiterals = new IntegerArrayQueue(reasonLiterals.length);
        IntegerArrayQueue invertedLiterals = new IntegerArrayQueue(Math.max(reasonLiterals.length,
                conflictLiterals.length));
        List<Constraint> learnedConstraints = new ArrayList<>();

        stateOfResolvedVariables = new int[stateOfResolvedVariables.length];

        for (int i = 0; i < conflictLiterals.length; i++) {
            int currentLiteralAbsoluteValue = Math.abs(conflictLiterals[i]);
            stateOfResolvedVariables[currentLiteralAbsoluteValue] = conflictLiterals[i];
        }

        for (int i = 0; i < reasonLiterals.length; i++) {
            int currentLiteral = reasonLiterals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);
            variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;

            if (stateOfResolvedVariables[currentLiteralAbsoluteValue] == -currentLiteral) {
                invertedLiterals.offer(-currentLiteral);
                stateOfResolvedVariables[currentLiteralAbsoluteValue] = 0;
            } else if (stateOfResolvedVariables[currentLiteralAbsoluteValue] == currentLiteral) {
                stateOfResolvedVariables[currentLiteralAbsoluteValue] = 0;
            } else {
                amoLiterals.offer(-currentLiteral);
            }
        }

        for (int i = 0; i < conflictLiterals.length; i++) {
            int currentLiteral = conflictLiterals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (stateOfResolvedVariables[currentLiteralAbsoluteValue] != 0) {
                clauseLiterals.offer(currentLiteral);
            }
        }


        if (invertedLiterals.size() == 1) {
            int[] clauseLiteralsArray = Arrays.copyOf(clauseLiterals.getInternalArray(), clauseLiterals.size() + 1);
            clauseLiteralsArray[clauseLiteralsArray.length - 1] = invertedLiterals.poll();
            clauseLiteralsArray =
                    Arrays.stream(clauseLiteralsArray).boxed().sorted(Comparator.comparingInt(a -> formula.getDecisionLevelOfVariables()[Math.abs(a)] * -1)).mapToInt(a -> a).toArray();

            learnedConstraints.add(formula.addDisjunctiveConstraint(clauseLiteralsArray));

            return learnedConstraints;
        }


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
        int[] variableAssignment = formula.getVariables();

        int conflictLiteral = formula.getConflictLiteral();
        int[] literalSharingStatus = new int[formula.getNumberOfVariables()];
        int sharedTrueVariable = 0;
        int sameLiteralsCounter = 0;
        int negatedLiteralsCounter = 0;

        List<Constraint> learnedConstraints = new ArrayList<>();

        for (int i = 0; i < literals.length; i++) {
            int currentLiteral = literals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);
            variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;

            if (stateOfResolvedVariables[currentLiteralAbsoluteValue] == currentLiteral) {
                sameLiteralsCounter++;
                if (variableAssignment[currentLiteralAbsoluteValue] * currentLiteral > 0) {
                    sharedTrueVariable = currentLiteral;
                }
                literalSharingStatus[currentLiteralAbsoluteValue] = 1;
            } else if (stateOfResolvedVariables[currentLiteralAbsoluteValue] == -currentLiteral) {
                negatedLiteralsCounter++;
                literalSharingStatus[currentLiteralAbsoluteValue] = -1;
            }
        }

        if (negatedLiteralsCounter >= 3) {
            return Arrays.asList();
        }

        if (negatedLiteralsCounter == 2) {
            if (sameLiteralsCounter == 0 && negatedLiteralsCounter == 2) {
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
            }

            return learnedConstraints;
        }

        if (negatedLiteralsCounter >= 1) {
            for (int i = 0; i < conflictLiterals.length; i++) {
                int currentLiteralConflict = conflictLiterals[i];

                if (literalSharingStatus[Math.abs(currentLiteralConflict)] == -1) {
                    continue;
                } else if (literalSharingStatus[Math.abs(currentLiteralConflict)] == 1) {
                    learnedConstraints.add(formula.addDisjunctiveConstraint(new int[]{-currentLiteralConflict}));
                }

                for (int j = 0; j < literals.length; j++) {
                    int currentLiteralReason = literals[j];

                    if (literalSharingStatus[Math.abs(currentLiteralReason)] != 0) {
                        continue;
                    }

                    learnedConstraints.add(formula.addDisjunctiveConstraint(new int[]{-currentLiteralConflict,
                            -currentLiteralReason}));

                }
            }

            return learnedConstraints;
        }

        return learnedConstraints;
    }

    @Override
    public List<Constraint> resolveConflict(DNFConstraint conflictConstraint, IntegerStack trail, int[] stateOfResolvedVariables, Formula formula, int[] variablesInvolvedInConflict) {
        int[] reasonLiterals = this.literals;
        int[][] conflictTerms = conflictConstraint.getTerms();
        int conflictLiteral = formula.getConflictLiteral();

        ArrayList<int[]> resolutionTerms = new ArrayList<>();
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

        return Arrays.asList(formula.addDNFConstraints(resolutionTerms.toArray(int[][]::new)));
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
    public int getNeededDecisionLevel(int[] decisionLevelOfVariables) {
        int decisionLevel = Integer.MAX_VALUE;

        for (int i = 0; i < literals.length; i++) {
            decisionLevel = Math.min(decisionLevel, decisionLevelOfVariables[Math.abs(literals[i])]);
        }

        return decisionLevel;
    }

    @Override
    public void addVariableOccurenceCount(double[] variableOccurences) {
        for (int i = 0; i < literals.length; i++) {
            variableOccurences[Math.abs(literals[i])] += 1;
        }
    }

    @Override
    public boolean isStillWatched(int literal) {
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


}
