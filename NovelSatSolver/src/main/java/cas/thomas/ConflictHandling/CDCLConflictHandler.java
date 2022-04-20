package cas.thomas.ConflictHandling;

import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.Formulas.Constraint;
import cas.thomas.Formulas.Formula;
import cas.thomas.VariableSelection.VariableSelectionStrategy;
import cas.thomas.utils.IntegerStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class CDCLConflictHandler implements ConflictHandlingStrategy {

    private Constraint[] reasonClauses;
    private List<Constraint> learnedClauses;
    private int[] decisionLevelOfVariables;
    private long reductionCounter = 0;
    private long conflictCounter = 0;
    private long vsidsConflictCounter = 0;
    private int[] learnedUnitClauses;


    @Override
    public boolean handleConflict(IntegerStack trail, Formula formula, boolean branchingDecision,
                                  int[] variableDecisionLevel, VariableSelectionStrategy variableSelectionStrategy) throws UnitLiteralConflictException {

        initializeArraysAndIncreaseCounters(formula, variableDecisionLevel);

        Constraint conflictClause = formula.getConflictClause();

        int[] variablesInvolvedInConflict = new int[formula.getNumberOfVariables()];


        List<Constraint> learnedConstraints = conflictClause.handleConflict(formula.getNumberOfVariables(), trail,
                variableDecisionLevel, variablesInvolvedInConflict, formula);

        if (checkForEmptyLearnedConstraints(formula, learnedConstraints)) return false;


        backtrackTrailToHighestDecisionLevelOfConflictClause(formula, trail,
                learnedConstraints, variableSelectionStrategy);

        if (formula.adjustVariableScores(variablesInvolvedInConflict, vsidsConflictCounter, variableSelectionStrategy)) {
            vsidsConflictCounter = 0;
        }


        clauseDatabaseReduction(variableDecisionLevel);

        formula.resetConflictState();
        formula.setUnitLiteralsBeforePropagation();
        return true;
    }

    private void initializeArraysAndIncreaseCounters(Formula formula, int[] variableDecisionLevel) {
        formula.emptyUnitLiterals();
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
    }

    private boolean checkForEmptyLearnedConstraints(Formula formula, List<Constraint> learnedConstraints) {
        if (learnedConstraints.size() == 0) {
            formula.resetConflictState();
            return true;
        }

        for (Constraint constraint : learnedConstraints) {

            if (constraint.isEmpty()) {
                formula.resetConflictState();
                return true;
            }

        }
        return false;
    }

    private void backtrackTrailToHighestDecisionLevelOfConflictClause(Formula formula, IntegerStack trail,
                                                                      List<Constraint> learnedConstraints,
                                                                      VariableSelectionStrategy variableSelectionStrategy) {

        int neededDecisionlevel = findNeededDecisionLevelAndSetLBDScore(learnedConstraints, formula.getVariables());

        int currentDecisionLevel = formula.getCurrentDecisionLevel();


        currentDecisionLevel = backtrackTrail(formula, trail, variableSelectionStrategy, neededDecisionlevel, currentDecisionLevel);

        formula.setCurrentDecisionLevel(currentDecisionLevel);

        for (Constraint learnedConstraint : learnedConstraints) {
            Set<Integer> unitLiterals = learnedConstraint.getUnitLiteralsNeededBeforePropagation();
            for (Integer unitLiteral : unitLiterals) {
                formula.addUnitLiteralBeforePropagation(unitLiteral, learnedConstraint);
            }
            learnedClauses.add(learnedConstraint);
        }
    }

    private void clauseDatabaseReduction(int[] variableDecisionLevel) {
        if (conflictCounter == 20000 + 500 * reductionCounter) {
            Constraint[] learnedClausesArray = learnedClauses.toArray(Constraint[]::new);
            Arrays.sort(learnedClausesArray, Comparator.comparing(a -> a.getLBDScore()));
            reduceClauseDatabaseSize(learnedClausesArray);
            reductionCounter++;
            conflictCounter = 0;
        }
    }

    private int findNeededDecisionLevelAndSetLBDScore(List<Constraint> learnedConstraints, int[] variables) {
        int neededDecisionLevel = Integer.MAX_VALUE;

        for (Constraint learnedConstraint : learnedConstraints) {
            neededDecisionLevel = Math.min(neededDecisionLevel,
                    learnedConstraint.getNeededDecisionLevel(decisionLevelOfVariables, variables));
            learnedConstraint.setLBDScore(decisionLevelOfVariables);

        }

        return neededDecisionLevel;
    }

    private int backtrackTrail(Formula formula, IntegerStack trail, VariableSelectionStrategy variableSelectionStrategy, int neededDecisionlevel, int currentDecisionLevel) {
        while (trail.hasNext()) {
            int currentLiteral = trail.pop();
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (currentLiteral > 0) {
                currentDecisionLevel--;
            }


            formula.unassignVariable(currentLiteral);
            variableSelectionStrategy.addUnassignedVariable(currentLiteralAbsoluteValue);
            decisionLevelOfVariables[currentLiteralAbsoluteValue] = 0;

            if (currentDecisionLevel < neededDecisionlevel) {
                break;
            }


        }
        return currentDecisionLevel;
    }

    private void reduceClauseDatabaseSize(Constraint[] sortedLearnedClauses) {
        int counter = 0;
        int halfSize = learnedClauses.size() / 2;
        List<Constraint> reducedLearnedClauses = new ArrayList<>(halfSize);
        for (int i = 0; i < sortedLearnedClauses.length; i++) {
            if (counter < halfSize || sortedLearnedClauses[i].isUnitConstraint()) {
                reducedLearnedClauses.add(sortedLearnedClauses[i]);
            } else {
                sortedLearnedClauses[i].setObsolete();
            }
            counter++;
        }

        learnedClauses = reducedLearnedClauses;
    }


}
