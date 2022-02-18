package cas.thomas.ConflictHandling;

import cas.thomas.Formulas.Constraint;
import cas.thomas.Formulas.ConstraintType;
import cas.thomas.Formulas.DisjunctiveConstraint;
import cas.thomas.Formulas.Formula;
import cas.thomas.utils.IntegerArrayQueue;
import cas.thomas.utils.IntegerStack;

import javax.print.attribute.SetOfIntegerSyntax;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

            //System.out.println(constraint.toString());

            if (constraint.isEmpty()) {
                formula.resetConflictState();
                return false;
            }

            Set<Integer> unitLiterals = constraint.getUnitLiteralsNeededBeforePropagation();


            for (Integer unitLiteral : unitLiterals) {
                int unitLiteralAbsoluteValue = Math.abs(unitLiteral);

                if (learnedUnitClauses[unitLiteralAbsoluteValue] == -unitLiteral) {
                    formula.resetConflictState();
                    return false;
                } else if (constraint.getConstraintType() == ConstraintType.DISJUNCTIVE) {
                    learnedUnitClauses[unitLiteralAbsoluteValue] = unitLiteral;
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
        formula.setUnitLiteralsBeforePropagation();
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
            Set<Integer> unitLiterals = learnedConstraint.getUnitLiteralsNeededBeforePropagation();
            for (Integer unitLiteral : unitLiterals) {
                this.unitLiterals.add(unitLiteral);
                reasonClauses[Math.abs(unitLiteral)] = learnedConstraint;
                formula.addUnitLiteralBeforePropagation(unitLiteral);
            }
            learnedClauses.add(learnedConstraint);
        }

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
