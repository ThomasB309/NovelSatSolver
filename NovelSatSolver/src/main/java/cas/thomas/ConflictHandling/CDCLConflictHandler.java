package cas.thomas.ConflictHandling;

import cas.thomas.Formulas.Constraint;
import cas.thomas.Formulas.DisjunctiveConstraint;
import cas.thomas.Formulas.Formula;
import cas.thomas.utils.IntegerArrayQueue;
import cas.thomas.utils.IntegerStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class CDCLConflictHandler implements ConflictHandlingStrategy {

    private List<Integer> unitLiterals = new LinkedList<>();
    private Constraint[] reasonClauses;
    private List<Constraint> learnedClauses;
    private int[] decisionLevelOfVariables;
    private long reductionCounter = 0;
    private long conflictCounter = 0;
    private long vsidsConflictCounter = 0;
    private int[] learnedUnitClauses;


    @Override
    public boolean handleConflict(IntegerStack trail, Formula formula, boolean branchingDecision,
                                  int[] variableDecisionLevel) {

        conflictCounter++;
        vsidsConflictCounter++;
        decisionLevelOfVariables = variableDecisionLevel;

        if (reasonClauses == null) {
            reasonClauses = new Constraint[formula.getNumberOfVariables()];
        }

        if (learnedClauses == null) {
            learnedClauses = new ArrayList<>();
        }

        if (learnedUnitClauses == null) {
            learnedUnitClauses = new int[formula.getNumberOfVariables()];
        }

        Constraint conflictClause = formula.getConflictClause();

        int[] conflictLiterals = conflictClause.getLiterals();

        int[] stateOfResolvedVariables = new int[formula.getNumberOfVariables()];

        int[] variablesInvolvedInConflict = new int[formula.getNumberOfVariables()];

        for (int i = 0; i < conflictLiterals.length; i++) {
            int currentLiteralAbsoluteValue = Math.abs(conflictLiterals[i]);
            stateOfResolvedVariables[currentLiteralAbsoluteValue] = conflictLiterals[i];
            variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;
        }

        trail.prepareIterationWithoutPop();
        while (trail.hasNextWithoutPop()) {
            int literal = trail.peekNextWithoutPop();
            Constraint reasonClause = formula.getReasonClauses(literal);

            int counter = 0;
            boolean resolve = false;
            for (int i = 0; i < conflictLiterals.length; i++) {
                int currentLiteral = conflictLiterals[i];
                int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

                if (variableDecisionLevel[Math.abs(currentLiteralAbsoluteValue)] == formula.getCurrentDecisionLevel()) {
                    counter++;
                }

                if (currentLiteralAbsoluteValue == Math.abs(literal)) {
                    resolve = true;
                }
            }

            if (counter == 1) {
                break;
            }

            if (reasonClause == null || !resolve) {
                continue;
            }

            conflictLiterals = resolveClauses(formula.getNumberOfVariables(), conflictLiterals,
                    reasonClause, Math.abs(literal));

        }



        if (conflictLiterals.length == 1) {
            int unitLiteral = conflictLiterals[0];
            int unitLiteralAbsoluteValue = Math.abs(unitLiteral);

            if (learnedUnitClauses[unitLiteralAbsoluteValue] == -unitLiteral) {
                formula.resetConflictState();
                return false;
            } else {
                learnedUnitClauses[unitLiteralAbsoluteValue] = unitLiteral;
            }
        }

        if (conflictLiterals.length == 0) {
            formula.resetConflictState();
            return false;
        }

        backtrackTrailToHighestDecisionLevelOfConflictClause(formula, trail,
                conflictLiterals,
                stateOfResolvedVariables);

        formula.adjustVariableScores(variablesInvolvedInConflict, vsidsConflictCounter);


        if (conflictCounter == 20000 + 500 * reductionCounter) {
            Constraint[] learnedClausesArray = learnedClauses.toArray(Constraint[]::new);
            Arrays.sort(learnedClausesArray, Comparator.comparing(a -> a.getLBDScore(variableDecisionLevel)));
            reduceClauseDatabaseSize(learnedClausesArray);
            reductionCounter++;
            conflictCounter = 0;
        }

        return true;
    }

    private int[] resolveClauses(int numberOfVariables, int[] conflictLiterals,
                                      Constraint reasonClause, int conflictLiteral) {

        int[] stateOfResolvedVariables = new int[numberOfVariables];
        int[] reasonClauseLiterals = reasonClause.getLiterals();
        IntegerArrayQueue newConflictLiteralsQueue =
                new IntegerArrayQueue(conflictLiterals.length + reasonClauseLiterals.length - 2);

        int maxLength = Math.max(conflictLiterals.length, reasonClauseLiterals.length);

        for (int i = 0; i < maxLength; i++) {

            if (i < conflictLiterals.length) {
                int currentLiteral = conflictLiterals[i];
                int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

                if (stateOfResolvedVariables[currentLiteralAbsoluteValue] != 1) {
                    if (currentLiteralAbsoluteValue != conflictLiteral) {
                        stateOfResolvedVariables[currentLiteralAbsoluteValue] = 1;
                        newConflictLiteralsQueue.offer(currentLiteral);
                    }
                }

            }

            if (i < reasonClauseLiterals.length) {
                int currentLiteral = reasonClauseLiterals[i];
                int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

                if (stateOfResolvedVariables[currentLiteralAbsoluteValue] != 1) {
                    if (currentLiteralAbsoluteValue != conflictLiteral) {
                        stateOfResolvedVariables[currentLiteralAbsoluteValue] = 1;
                        newConflictLiteralsQueue.offer(currentLiteral);
                    }
                }
            }

        }

        return newConflictLiteralsQueue.getInternalArray();
    }

    private void backtrackTrailToHighestDecisionLevelOfConflictClause(Formula formula, IntegerStack trail,
                                                                      int learnedConstraintLiterals[],
                                                                      int[] newConstraintLiterals) {
        learnedConstraintLiterals =
                Arrays.stream(learnedConstraintLiterals).boxed().sorted(Comparator.comparingInt(a -> decisionLevelOfVariables[Math.abs(a)] * -1)).mapToInt(i -> i).toArray();

        int neededDecisionlevel = learnedConstraintLiterals.length == 1 ?
                decisionLevelOfVariables[Math.abs(learnedConstraintLiterals[0])] :
                decisionLevelOfVariables[Math.abs(learnedConstraintLiterals[1])];



        int currentDecisionLevel = formula.getCurrentDecisionLevel();

        while (trail.hasNext()) {
            int currentLiteral = trail.pop();
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (currentLiteral > 0) {
                currentDecisionLevel--;
            }


            formula.unassignVariable(currentLiteral);
            decisionLevelOfVariables[currentLiteralAbsoluteValue] = 0;

            if (currentDecisionLevel < neededDecisionlevel) {
                break;
            }


        }

        formula.setCurrentDecisionLevel(currentDecisionLevel);

        DisjunctiveConstraint learnedConstraint =
                formula.addDisjunctiveConstraint(learnedConstraintLiterals);

        if (learnedConstraintLiterals.length == 1) {
            this.unitLiterals.add(learnedConstraintLiterals[0]);
            reasonClauses[Math.abs(learnedConstraintLiterals[0])] = learnedConstraint;
        }

        formula.addUnitLiterals(this.unitLiterals);
        formula.setReasonClauses(reasonClauses);
        learnedClauses.add(learnedConstraint);
    }

    private void reduceClauseDatabaseSize(Constraint[] sortedLearnedClauses) {
        int counter = 0;
        int halfSize = learnedClauses.size() / 2;
        List<Constraint> reducedLearnedClauses = new ArrayList<>(halfSize);
        for (int i = 0; i < sortedLearnedClauses.length; i++) {
            if (counter < halfSize || sortedLearnedClauses[i].getLiterals().length == 1) {
                reducedLearnedClauses.add(sortedLearnedClauses[i]);
            } else {
                sortedLearnedClauses[i].setObsolete();
            }
            counter++;
        }

        learnedClauses = reducedLearnedClauses;
    }


}
