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

        Constraint conflictClause = formula.getConflictClause();

        int[] variablesInvolvedInConflict = new int[formula.getNumberOfVariables()];


        List<Constraint> learnedConstraints = conflictClause.handleConflict(formula.getNumberOfVariables(), trail,
                variableDecisionLevel, variablesInvolvedInConflict,formula);

        if (learnedConstraints.size() == 0) {
            formula.resetConflictState();
            return false;
        }

        for (Constraint constraint : learnedConstraints) {

            //System.out.println(Arrays.toString(constraint.getLiterals()));

            if (constraint.isEmpty()) {
                formula.resetConflictState();
                return false;
            }


            if (constraint.isUnitConstraint()) {

                int[] unitLiterals = constraint.getUnitLiterals();

                for (int i = 0; i < unitLiterals.length; i++) {
                    int unitLiteral = unitLiterals[i];
                    int unitLiteralAbsoluteValue = Math.abs(unitLiteral);


                    if (learnedUnitClauses[unitLiteralAbsoluteValue] == -unitLiteral) {
                        formula.resetConflictState();
                        return false;
                    } else {
                        learnedUnitClauses[unitLiteralAbsoluteValue] = unitLiteral;
                    }
                }
            }
        }

        backtrackTrailToHighestDecisionLevelOfConflictClause(formula, trail,
                learnedConstraints);

        formula.adjustVariableScores(variablesInvolvedInConflict, vsidsConflictCounter);


        if (conflictCounter == 20000 + 500 * reductionCounter) {
            Constraint[] learnedClausesArray = learnedClauses.toArray(Constraint[]::new);
            Arrays.sort(learnedClausesArray, Comparator.comparing(a -> a.getLBDScore(variableDecisionLevel)));
            reduceClauseDatabaseSize(learnedClausesArray);
            reductionCounter++;
            conflictCounter = 0;
        }

        formula.resetConflictState();
        return true;
    }

    private void backtrackTrailToHighestDecisionLevelOfConflictClause(Formula formula, IntegerStack trail,
                                                                      List<Constraint> learnedConstraints) {
        int[] unitLiteralState = formula.getUnitLiteralState();

        int neededDecisionlevel = findNeededDecisionLevel(learnedConstraints, formula.getCurrentDecisionLevel());

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

        for (Constraint learnedConstraint : learnedConstraints) {
            int[] unitLiterals = learnedConstraint.getUnitLiterals();
            for (int i = 0; i < unitLiterals.length; i++) {
                int unitLiteral = unitLiterals[i];
                this.unitLiterals.add(unitLiteral);
                reasonClauses[Math.abs(unitLiteral)] = learnedConstraint;
            }
            learnedClauses.add(learnedConstraint);
        }

        formula.addUnitLiterals(this.unitLiterals);
        formula.setReasonClauses(reasonClauses);
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

    private int findNeededDecisionLevel(List<Constraint> learnedConstraints, int currentDecisionLevel) {
        int neededDecisionLevel = Integer.MAX_VALUE;

        for (Constraint learnedConstraint : learnedConstraints) {
            neededDecisionLevel = Math.min(neededDecisionLevel, learnedConstraint.getNeededDecisionLevel(decisionLevelOfVariables));
        }

        return neededDecisionLevel;
    }


}
