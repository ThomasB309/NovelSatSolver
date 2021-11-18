package cas.thomas.ConflictHandling;

import cas.thomas.Formulas.Constraint;
import cas.thomas.Formulas.DisjunctiveConstraint;
import cas.thomas.Formulas.Formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
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


    @Override
    public boolean handleConflict(Deque<Integer> trail, Formula formula, boolean branchingDecision,
                                  int[] variableDecisionLevel) {

        conflictCounter++;
        vsidsConflictCounter++;

        if (decisionLevelOfVariables == null) {
            decisionLevelOfVariables = variableDecisionLevel;
        }

        if (reasonClauses == null) {
            reasonClauses = new Constraint[formula.getNumberOfVariables()];
        }

        if (learnedClauses == null) {
            learnedClauses = new ArrayList<>();
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

        for (Iterator<Integer> iterator = trail.iterator(); iterator.hasNext();) {
            int literal = iterator.next();
            Constraint reasonClause = formula.getReasonClauses(literal);

            int counter = 0;
            for (int i = 0; i < conflictLiterals.length; i++) {
                if (variableDecisionLevel[Math.abs(conflictLiterals[i])] == formula.getCurrentDecisionLevel()) {
                    counter++;
                }
            }

            if (counter == 1) {
                break;
            }

            if (reasonClause == null) {
                continue;
            }

            conflictLiterals = resolveClauses(formula.getNumberOfVariables(), conflictLiterals,
                    stateOfResolvedVariables, variablesInvolvedInConflict,
                    reasonClause);

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

    private int[] resolveClauses(int numberOfVariables, int[] conflictLiterals, int[] stateOfResolvedVariables,
                                 int[] variablesInvolvedInConflict,
                                      Constraint reasonClause) {

        int[] conflictLiteralsFastAccess = new int[numberOfVariables];

        for (int i = 0; i < conflictLiterals.length; i++) {
            conflictLiteralsFastAccess[Math.abs(conflictLiterals[i])] = conflictLiterals[i];
        }

        int[] literals = reasonClause.getLiterals();

        boolean resolve = false;
        for (int i = 0; i < literals.length; i++) {
            int currentLiteral = literals[i];

            if (conflictLiteralsFastAccess[Math.abs(currentLiteral)] == -currentLiteral) {
                resolve = true;
            }
        }

        if (resolve) {
            for (int i = 0; i < literals.length; i++) {
                int currentLiteral = literals[i];
                int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

                variablesInvolvedInConflict[currentLiteralAbsoluteValue] = 1;

                if (stateOfResolvedVariables[currentLiteralAbsoluteValue] != numberOfVariables) {

                    if (stateOfResolvedVariables[currentLiteralAbsoluteValue] == -currentLiteral) {
                        stateOfResolvedVariables[currentLiteralAbsoluteValue] = numberOfVariables;
                    } else {
                        stateOfResolvedVariables[currentLiteralAbsoluteValue] = currentLiteral;
                    }
                }
            }
        }


        return Arrays.stream(stateOfResolvedVariables).filter(a -> a != numberOfVariables && a != 0).toArray();
    }

    private void backtrackTrailToHighestDecisionLevelOfConflictClause(Formula formula, Deque<Integer> trail,
                                                                      int learnedConstraintLiterals[],
                                                                       int[] newConstraintLiterals) {
        learnedConstraintLiterals =
                Arrays.stream(learnedConstraintLiterals).boxed().sorted(Comparator.comparingInt(a -> decisionLevelOfVariables[Math.abs(a)] * -1)).mapToInt(i -> i).toArray();

        int neededDecisionlevel = learnedConstraintLiterals.length == 1 ?
                decisionLevelOfVariables[Math.abs(learnedConstraintLiterals[0])] :
                decisionLevelOfVariables[Math.abs(learnedConstraintLiterals[1])];



        int currentDecisionLevel = formula.getCurrentDecisionLevel();

        for (Iterator<Integer> iterator = trail.iterator(); iterator.hasNext();) {
            int currentLiteral = iterator.next();
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (currentLiteral > 0) {
                currentDecisionLevel--;
            }


            formula.unassignVariable(currentLiteral);
            decisionLevelOfVariables[currentLiteralAbsoluteValue] = 0;
            iterator.remove();

            if (currentDecisionLevel < neededDecisionlevel) {
                break;
            }


        }

        formula.setCurrentDecisionLevel(currentDecisionLevel);

        DisjunctiveConstraint learnedConstraint =
                formula.addDisjunctiveConstraint(learnedConstraintLiterals);

        if (learnedConstraintLiterals.length == 1) {
            this.unitLiterals.add(learnedConstraintLiterals[0]);
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
            if (counter < halfSize) {
                reducedLearnedClauses.add(sortedLearnedClauses[i]);
            } else {
                sortedLearnedClauses[i].setObsolete();
            }
            counter++;
        }

        learnedClauses = reducedLearnedClauses;
    }


}
