package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class Formula {

    private int[] variables;
    private int[] variableOccurences;
    private int[] phaseSavingLastAssignment;
    private Constraint[] constraints;
    private List<Constraint>[] positivelyWatchedDisjunctiveConstraints;
    private List<Constraint>[] negativelyWatchedDisjunctiveConstraints;
    private List<Constraint>[] positivelWatchedAMOConstraints;
    private List<Constraint>[] negativelyWatchedAMOConstraints;

    private Constraint[] reasonClauses;
    private Constraint conflictClause;
    private boolean hasConflict;
    private List<Integer> unitLiterals;
    int assignedCounter;

    public Formula(int variableCount, Constraint[] constraints, int[] variableOccurences, List<Integer> unitLiterals,
                   List<Constraint>[] positivelyWatchedDisjunctiveConstraints
            , List<Constraint>[] negativelyWatchedDisjunctiveConstraints,
                   List<Constraint>[] positivelWatchedAMOConstraints,
                   List<Constraint>[] negativelyWatchedAMOConstraints) {

        this.variables = new int[variableCount];
        this.phaseSavingLastAssignment = new int[variableCount];
        this.variableOccurences = variableOccurences;
        this.reasonClauses = new Constraint[variableCount];
        this.unitLiterals = unitLiterals;
        this.constraints = constraints;
        this.conflictClause = null;
        this.hasConflict = false;
        this.positivelyWatchedDisjunctiveConstraints = positivelyWatchedDisjunctiveConstraints;
        this.negativelyWatchedDisjunctiveConstraints = negativelyWatchedDisjunctiveConstraints;
        this.positivelWatchedAMOConstraints = positivelWatchedAMOConstraints;
        this.negativelyWatchedAMOConstraints = negativelyWatchedAMOConstraints;
        this.assignedCounter = 0;
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
        } else {
            variables[variable] = -1;
            propagateInDisjunctiveConstraints(positivelyWatchedDisjunctiveConstraints[variable], -variable);
            propagateInAMOConstraints(negativelyWatchedAMOConstraints[variable], variable);
        }

    }

    private void propagateInDisjunctiveConstraints(List<Constraint> watchedList, int literal) {
        for (Iterator<Constraint> constraintIterator = watchedList.listIterator(); constraintIterator.hasNext();) {
            Constraint currentConstraint = constraintIterator.next();

            if (currentConstraint == null) {
                constraintIterator.remove();
                continue;
            }

            if (!currentConstraint.propagate(literal, variables, unitLiterals, positivelyWatchedDisjunctiveConstraints
                    ,negativelyWatchedDisjunctiveConstraints, reasonClauses)) {
                constraintIterator.remove();
            }

            if (currentConstraint.resetConflictState()) {
                conflictClause = currentConstraint;
                hasConflict = true;
                return;
            }
        }
    }

    private void propagateInAMOConstraints(List<Constraint> watchedList, int literal) {
        for (int i = 0; i < watchedList.size(); i++) {
            Constraint currentConstraint = watchedList.get(i);
            currentConstraint.propagate(literal, variables, unitLiterals, positivelWatchedAMOConstraints,
                    negativelyWatchedAMOConstraints, reasonClauses);

            if (currentConstraint.resetConflictState()) {
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

    public List<Integer> getUnitLiterals() {
        return unitLiterals;
    }

    public int[] getVariables() {
        return variables;
    }

    public void emptyUnitLiterals() {
        unitLiterals = new LinkedList<>();
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

    public void resetConflictState() {
        hasConflict = false;
    }

    public Constraint getReasonClauses(int literal) {
        return reasonClauses[Math.abs(literal)];
    }

    public Constraint[] getReasonClauses() {
        return reasonClauses;
    }

    public DisjunctiveConstraint addDisjunctiveConstraint(int[] literals) {
        return new DisjunctiveConstraint(literals, positivelyWatchedDisjunctiveConstraints,
                negativelyWatchedDisjunctiveConstraints);
    }

    public void removeReasonClauses() {
        reasonClauses = new Constraint[variables.length];
    }

    public void setReasonClauses(Constraint[] reasonClauses) {
        this.reasonClauses = Arrays.copyOf(reasonClauses, reasonClauses.length);
    }

    public void addUnitLiterals(List<Integer> unitLiterals) {
        this.unitLiterals.addAll(unitLiterals);
    }

    public void addVariableOccurenceCount(int[] literals) {
        for (int i = 0; i < literals.length; i++) {
            variableOccurences[Math.abs(literals[i])] += 1;
        }
    }

    public void halfVariableOccurenceCounter() {
        for (int i = 0; i < variableOccurences.length; i++) {
            variableOccurences[i] /= 2;
        }
    }

    public int[] getVariableOccurences() {
        return variableOccurences;
    }

    public void setPhaseSavingLastAssignment(int literal) {
        phaseSavingLastAssignment[Math.abs(literal)] = literal < 0 ? -1 : 1;
    }

    public int getPhaseSavingLastAssignment(int literal) {
        return phaseSavingLastAssignment[Math.abs(literal)];
    }


}
