package cas.thomas.Formulas;

import cas.thomas.utils.IntegerArrayQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Formula {

    private int[] variables;
    private int[] unitLiteralState;
    private double[] variableOccurences;
    private int[] phaseSavingLastAssignment;
    private Constraint[] constraints;
    private List<Constraint>[] positivelyWatchedDisjunctiveConstraints;
    private List<Constraint>[] negativelyWatchedDisjunctiveConstraints;
    private List<Constraint>[] positivelWatchedAMOConstraints;
    private List<Constraint>[] negativelyWatchedAMOConstraints;
    private List<Constraint>[] positivelyWatchedDNFConstraints;
    private List<Constraint>[] negativelyWatchedDNFConstraints;

    private int currentDecisionLevel;
    private int[] decisionLevelOfVariables;

    private Constraint[] reasonClauses;
    private Constraint conflictClause;
    private boolean hasConflict;
    private IntegerArrayQueue unitLiterals;
    int assignedCounter;
    private int conflictLiteral;

    public Formula(int variableCount, Constraint[] constraints, double[] variableOccurences,
                   IntegerArrayQueue unitLiterals, int[] unitLiteralState,
                   List<Constraint>[] positivelyWatchedDisjunctiveConstraints
            , List<Constraint>[] negativelyWatchedDisjunctiveConstraints,
                   List<Constraint>[] positivelWatchedAMOConstraints,
                   List<Constraint>[] negativelyWatchedAMOConstraints,
                   List<Constraint>[] positivelyWatchedDNFConstraints,
                   List<Constraint>[] negativelyWatchedDNFConstraints) {

        this.variables = new int[variableCount];
        this.unitLiteralState = unitLiteralState;
        this.phaseSavingLastAssignment = new int[variableCount];
        this.variableOccurences = variableOccurences;
        this.reasonClauses = new Constraint[variableCount];
        this.decisionLevelOfVariables = new int[variableCount];
        this.unitLiterals = unitLiterals;
        this.constraints = constraints;
        this.conflictClause = null;
        this.hasConflict = false;
        this.positivelyWatchedDisjunctiveConstraints = positivelyWatchedDisjunctiveConstraints;
        this.negativelyWatchedDisjunctiveConstraints = negativelyWatchedDisjunctiveConstraints;
        this.positivelWatchedAMOConstraints = positivelWatchedAMOConstraints;
        this.negativelyWatchedAMOConstraints = negativelyWatchedAMOConstraints;
        this.positivelyWatchedDNFConstraints = positivelyWatchedDNFConstraints;
        this.negativelyWatchedDNFConstraints = negativelyWatchedDNFConstraints;
        this.assignedCounter = 0;
        this.currentDecisionLevel = 0;
        this.conflictLiteral = 0;
    }

    public void propagate(int literal) {
        propagate(Math.abs(literal), literal < 0 ? false : true);
    }

    public void propagateAfterSwappingVariableAssigment(int variable, boolean truthValue) {
        assignedCounter--;
        variables[variable] *= -1;
        propagate(variable, truthValue);
    }

    public void propagate(int variable, boolean truthValue) {

        assignedCounter++;

        if (truthValue) {
            variables[variable] = 1;
            propagateInDisjunctiveConstraints(negativelyWatchedDisjunctiveConstraints[variable], variable);
            propagateInAMOConstraints(positivelWatchedAMOConstraints[variable], variable);
            propagateInDNFConstraints(negativelyWatchedDNFConstraints[variable], variable);
        } else {
            variables[variable] = -1;
            propagateInDisjunctiveConstraints(positivelyWatchedDisjunctiveConstraints[variable], -variable);
            propagateInAMOConstraints(negativelyWatchedAMOConstraints[variable], -variable);
            propagateInDNFConstraints(positivelyWatchedDNFConstraints[variable], -variable);
        }

    }

    private void propagateInDisjunctiveConstraints(List<Constraint> watchedList, int literal) {
        for (Iterator<Constraint> constraintIterator = watchedList.iterator(); constraintIterator.hasNext();) {
            Constraint currentConstraint = constraintIterator.next();

            if (currentConstraint.isObsolete()) {
                constraintIterator.remove();
                continue;
            }

            if (!currentConstraint.propagate(literal, variables, unitLiteralState, unitLiterals,
                    positivelyWatchedDisjunctiveConstraints
                    ,negativelyWatchedDisjunctiveConstraints, reasonClauses)) {
                constraintIterator.remove();
            }

            int conflictLiteralClause = currentConstraint.resetConflictState();

            if (conflictLiteral == 0) {
                conflictLiteral = conflictLiteralClause;
            }

            if (!hasConflict && conflictLiteral != 0) {
                conflictClause = currentConstraint;
                hasConflict = true;
                return;
            }
        }
    }

    private void propagateInAMOConstraints(List<Constraint> watchedList, int literal) {
        for (Iterator<Constraint> constraintIterator = watchedList.iterator(); constraintIterator.hasNext();) {
            Constraint currentConstraint = constraintIterator.next();


            currentConstraint.propagate(literal, variables, unitLiteralState, unitLiterals,
                    positivelWatchedAMOConstraints,
                    negativelyWatchedAMOConstraints, reasonClauses);

            int conflictLiteralAMO = currentConstraint.resetConflictState();

            if (conflictLiteral == 0) {
                conflictLiteral = conflictLiteralAMO;
            }

            if (!hasConflict && conflictLiteral != 0) {
                conflictClause = currentConstraint;
                hasConflict = true;
                return;
            }
        }
    }

    private void propagateInDNFConstraints(List<Constraint> watchedList, int literal) {
        for (Iterator<Constraint> constraintIterator = watchedList.iterator(); constraintIterator.hasNext();) {
            Constraint currentConstraint = constraintIterator.next();

            if (currentConstraint.isObsolete() || !currentConstraint.isStillWatched(literal)) {
                constraintIterator.remove();
                continue;
            }

            if (!currentConstraint.propagate(literal, variables, unitLiteralState, unitLiterals,
                    positivelyWatchedDNFConstraints
                    ,negativelyWatchedDNFConstraints, reasonClauses)) {
                constraintIterator.remove();
            }

            int conflictLiteralClause = currentConstraint.resetConflictState();

            if (conflictLiteral == 0) {
                conflictLiteral = conflictLiteralClause;
            }

            if (!hasConflict && conflictLiteral != 0) {
                conflictClause = currentConstraint;
                hasConflict = true;
                return;
            }
        }
    }

    public int getNumberOfVariables() {
        return variables.length;
    }

    public void unassignVariable(int literal) {
        assignedCounter--;
        variables[Math.abs(literal)] = 0;
    }

    public int getAssignedCounter() {
        return assignedCounter;
    }

    public IntegerArrayQueue getUnitLiterals() {
        return unitLiterals;
    }

    public int[] getVariables() {
        return variables;
    }

    public void emptyUnitLiterals() {
        unitLiterals = new IntegerArrayQueue(variables.length);
        unitLiteralState = new int[unitLiteralState.length];
    }

    public List<Integer> getVariablesForSolutionChecker() {
        List<Integer> variables = new ArrayList<>(this.variables.length);

        for (int i = 0; i < this.variables.length; i++) {
            if (this.variables[i] < 0) {
                variables.add(-i);
            } else if (this.variables[i] > 0) {
                variables.add(i);
            }
        }

        return variables;
    }

    public boolean variableAlreadyHasTheSameValue(int literal) {
        if (variables[Math.abs(literal)] * literal > 0) {
            return true;
        }

        return false;
    }

    public boolean hasConflict() {
        return hasConflict;
    }

    public Constraint getConflictClause() {
        return conflictClause;
    }

    public int getConflictLiteral() {
        return conflictLiteral;
    }

    public void resetConflictState() {
        hasConflict = false;
        conflictLiteral = 0;
    }

    public Constraint getReasonClauses(int literal) {
        int literalsAbsoluteValue = Math.abs(literal);
        Constraint reasonConstraint = reasonClauses[literalsAbsoluteValue];

        if (reasonConstraint == null) {
            return null;
        } else if (reasonConstraint.isObsolete()) {
            reasonClauses[literalsAbsoluteValue] = null;
            return null;
        }

        return reasonClauses[literalsAbsoluteValue];
    }

    public Constraint[] getReasonClauses() {
        return reasonClauses;
    }

    public DisjunctiveConstraint addDisjunctiveConstraint(int[] literals) {
        return new DisjunctiveConstraint(literals, positivelyWatchedDisjunctiveConstraints,
                negativelyWatchedDisjunctiveConstraints);
    }

    public DNFConstraint addDNFConstraints(int[][] terms) {
        return new DNFConstraint(terms, positivelyWatchedDNFConstraints, negativelyWatchedDNFConstraints,
                unitLiterals, unitLiteralState);
    }

    public void removeReasonClauses() {
        reasonClauses = new Constraint[variables.length];
    }

    public void setReasonClauses(Constraint[] reasonClauses) {

        for (int i = 0; i < reasonClauses.length; i++) {
            this.reasonClauses[i] = reasonClauses[i];
        }
    }

    public void addUnitLiterals(List<Integer> unitLiterals) {
        for (Integer unitLiteral : unitLiterals) {
            this.unitLiterals.offer(unitLiteral);
            this.unitLiteralState[Math.abs(unitLiteral)] = unitLiteral < 0 ? -1 : 1;
        }
    }

    public void adjustVariableScores(int[] literals, long conflictIndex) {
        for (int i = 0; i < literals.length; i++) {
            if (literals[i] == 1){
                variableOccurences[i] += Math.pow(1.01, conflictIndex);
            }
        }
    }

    public double[] getVariableOccurences() {
        return variableOccurences;
    }

    public void setPhaseSavingLastAssignment(int literal) {
        phaseSavingLastAssignment[Math.abs(literal)] = literal < 0 ? -1 : 1;
    }

    public int getPhaseSavingLastAssignment(int literal) {
        return phaseSavingLastAssignment[Math.abs(literal)];
    }

    public void setCurrentDecisionLevel(int currentDecisionLevel) {
        this.currentDecisionLevel = currentDecisionLevel;
    }

    public void increaseCurrentDecisionLevel() {
        currentDecisionLevel++;
    }

    public void decreaseCurrentDecisionLevel() {
        currentDecisionLevel--;
    }

    public int getCurrentDecisionLevel() {
        return currentDecisionLevel;
    }

    public int[] getDecisionLevelOfVariables() {
        return decisionLevelOfVariables;
    }

    public int[] resetDecisionLevelOfVariables() {
        this.decisionLevelOfVariables = new int[variables.length];
        return decisionLevelOfVariables;
    }

    public int getLiteralFromVariableAssignment(int variable) {
        return variables[variable] * variable;
    }

    public int[] getUnitLiteralState() {
        return unitLiteralState;
    }


}
