package cas.thomas.ConflictHandling;

import cas.thomas.Formulas.Constraint;
import cas.thomas.Formulas.DisjunctiveConstraint;
import cas.thomas.Formulas.Formula;

import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CDCLConflictHandler implements ConflictHandlingStrategy {

    private List<Integer> unitLiterals = new LinkedList<>();
    private Constraint[] reasonClauses;


    @Override
    public boolean handleConflict(Deque<Integer> trail, Formula formula, boolean branchingDecision) {

        if (reasonClauses == null) {
            reasonClauses = new Constraint[formula.getNumberOfVariables()];
        }

        Constraint conflictClause = formula.getConflictClause();

        int[] conflictLiterals = conflictClause.getLiterals();
        int[] stateOfResolvedVariables = new int[formula.getNumberOfVariables()];

        for (int i = 0; i < conflictLiterals.length; i++) {
            stateOfResolvedVariables[Math.abs(conflictLiterals[i])] = conflictLiterals[i];
        }

        for (Iterator<Integer> iterator = trail.iterator(); iterator.hasNext();) {
            int literal = iterator.next();
            Constraint reasonClause = formula.getReasonClauses(literal);

            if (reasonClause == null) {
                continue;
            }

            conflictLiterals = resolveClauses(formula.getNumberOfVariables(), conflictLiterals, stateOfResolvedVariables,
                    reasonClause);
        }

        if (conflictLiterals.length == 0) {
            formula.resetConflictState();
            return false;
        }

        DisjunctiveConstraint learnedConstraint =
                formula.addDisjunctiveConstraint(conflictLiterals);

        backtrackTrailToHighestDecisionLevelOfConflictClause(formula, trail, learnedConstraint, stateOfResolvedVariables);
        return true;
    }

    private int[] resolveClauses(int numberOfVariables, int[] conflictLiterals, int[] stateOfResolvedVariables,
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
                                                                      Constraint learnedConstraint, int[] newConstraintLiterals) {

        int[] learnedConstraintLiterals = learnedConstraint.getLiterals();
        int counter = learnedConstraintLiterals.length;

        for (Iterator<Integer> iterator = trail.iterator(); iterator.hasNext();) {
            int currentLiteral = iterator.next();
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (newConstraintLiterals[currentLiteralAbsoluteValue] != 0 && newConstraintLiterals[currentLiteralAbsoluteValue] != formula.getNumberOfVariables()) {
                counter--;
            }

            formula.unassignVariable(currentLiteral);
            iterator.remove();

            if (counter == 0) {
                break;
            }
        }

        if (learnedConstraintLiterals.length == 1) {
            this.unitLiterals.add(learnedConstraintLiterals[0]);
            this.reasonClauses[Math.abs(learnedConstraintLiterals[0])] = learnedConstraint;
        }

        formula.addUnitLiterals(this.unitLiterals);
        formula.addVariableOccurenceCount(learnedConstraintLiterals);
        formula.setReasonClauses(reasonClauses);
    }

}
