package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.utils.IntegerArrayQueue;
import cas.thomas.utils.IntegerStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Constraint {

    protected int[] literals;
    protected boolean hasConflict;
    protected int conflictLiteral;
    private boolean obsolete;
    private int lbdScore;

    public Constraint() {
        this.hasConflict = false;
        conflictLiteral = 0;
        obsolete = false;
    }


    public abstract boolean propagate(int propagatedLiteral, int[] variableAssignments, int[] unitLiteralState,
                                      IntegerArrayQueue unitLiterals,
                                      List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched,
                                      Constraint[] reasonClauses);

    public abstract SolutionCheckerConstraint getSolutionCheckerConstraint();

    public int resetConflictState() {
        hasConflict = false;
        int conflictLiteralCopy = conflictLiteral;
        conflictLiteral = 0;
        return conflictLiteralCopy;
    }

    public int[] getLiterals() {
        return literals;
    }

    public int getConflictLiteral() {
        return this.conflictLiteral;
    }

    public void setLBDScore(int[] variableDecisionLevels) {
        lbdScore = calculateLBDScore(variableDecisionLevels);
    }

    public int getLBDScore() {
        return lbdScore;
    }

    public int calculateLBDScore(int[] variableDecisionLevels) {
        Set<Integer> distinctDecisionLevels = new HashSet<>();

        for (int i = 0; i < literals.length; i++) {
            distinctDecisionLevels.add(variableDecisionLevels[Math.abs(literals[i])]);
        }

        return distinctDecisionLevels.size();
    }

    public void setObsolete() {
        obsolete = true;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public abstract List<Constraint> handleConflict(int numberOfVariables, IntegerStack trail,
                                                    int[] variableDecisionLevel, int[] variablesInvolvedInConflict, Formula formula);


    public abstract List<Constraint> resolveConflict(Constraint conflictConstraint, IntegerStack trail,
                                                     int[] stateOfResolvedVariables,
                                                     Formula formula, int[] variablesInvolvedInConflict);

    public abstract List<Constraint> resolveConflict(AMOConstraint conflictConstraint, IntegerStack trail,
                                                     int[] stateOfResolvedvariables, Formula formula,
                                                     int[] variablesInvolvedInConflict);

    public abstract List<Constraint> resolveConflict(DNFConstraint conflictConstraint, IntegerStack trail,
                                                     int[] stateOfResolvedVariables, Formula formula,
                                                     int[] variablesInvolvedInConflict);


    public abstract ConstraintType getConstraintType();

    protected boolean checkIfLiteralIsFalse(int literal, int[] variables) {
        if (variables[Math.abs(literal)] * literal < 0) {
            return true;
        }

        return false;
    }

    public abstract boolean isUnitConstraint();

    public abstract int[] getUnitLiterals();

    public abstract boolean isEmpty();

    public abstract int getNeededDecisionLevel(int[] decisionLevelOfVariables, int[] variables);

    public abstract void addVariableOccurenceCount(double[] variableOccurences);

    public abstract boolean isStillWatched(int literal, int[] variables);

    public void backtrack(int variable, int[] variableAssignments) {
        return;
    }

    public abstract String toString();

    public abstract Set<Integer> getUnitLiteralsNeededBeforePropagation();
}
