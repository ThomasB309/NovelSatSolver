package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class Formula {

    private int[] variables;
    private Constraint[] constraints;
    private List<Constraint>[] positivelyWatchedDisjunctiveConstraints;
    private List<Constraint>[] negativelyWatchedDisjunctiveConstraints;
    private List<Constraint>[] positivelWatchedAMOConstraints;
    private List<Constraint>[] negativelyWatchedAMOConstraints;
    private List<Integer> unitLiterals;
    int assignedCounter;

    public Formula(int variableCount, Constraint[] constraints, List<Integer> unitLiterals,
                   List<Constraint>[] positivelyWatchedDisjunctiveConstraints
            , List<Constraint>[] negativelyWatchedDisjunctiveConstraints,
                   List<Constraint>[] positivelWatchedAMOConstraints,
                   List<Constraint>[] negativelyWatchedAMOConstraints) {
        this.variables = new int[variableCount];
        this.unitLiterals = unitLiterals;
        this.constraints = constraints;
        this.positivelyWatchedDisjunctiveConstraints = positivelyWatchedDisjunctiveConstraints;
        this.negativelyWatchedDisjunctiveConstraints = negativelyWatchedDisjunctiveConstraints;
        this.positivelWatchedAMOConstraints = positivelWatchedAMOConstraints;
        this.negativelyWatchedAMOConstraints = negativelyWatchedAMOConstraints;
        this.assignedCounter = 0;
    }

    public boolean propagate(int literal) {
        return propagate(Math.abs(literal), literal < 0 ? false : true);
    }

    public boolean propagateAfterSwappingVariableAssigment(int variable, boolean truthValue) {
        assignedCounter--;
        variables[variable] *= -1;
        return propagate(variable, truthValue);
    }

    public boolean propagate(int variable, boolean truthValue) {

        if (checkForConflictingVariableAssignment(variable, truthValue)) {
            return false;
        }

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

        return true;
    }

    private void propagateInDisjunctiveConstraints(List<Constraint> watchedList, int literal) {
        List<Constraint> stillWatched = new ArrayList<>(watchedList.size());
        for (int i = 0; i < watchedList.size(); i++) {
            Constraint currentConstraint = watchedList.get(i);
            if (currentConstraint.propagate(literal, variables, unitLiterals, positivelyWatchedDisjunctiveConstraints
                    ,negativelyWatchedDisjunctiveConstraints)) {
                stillWatched.add(currentConstraint);
            }
        }

        watchedList.clear();
        watchedList.addAll(stillWatched);
    }

    private void propagateInAMOConstraints(List<Constraint> watchedList, int literal) {
        for (int i = 0; i < watchedList.size(); i++) {
            Constraint currentConstraint = watchedList.get(i);
            currentConstraint.propagate(literal, variables, unitLiterals, positivelWatchedAMOConstraints,
                    negativelyWatchedAMOConstraints);
        }
    }

    public int getNumberOfVariables() {
        return variables.length;
    }

    public void unassignVariable(int literal) {
        assignedCounter--;
        variables[Math.abs(literal)] = 0;
    }

    public void assignVariable(int literal) {
        assignedCounter++;
        variables[Math.abs(literal)] = literal < 0 ? -1 : 1;
    }

    public void swapLiteralAssignment(int literal) {
        variables[Math.abs(literal)] *= -1;
    }

    public int getAssignedCounter() {
        return assignedCounter;
    }

    public List<Integer> getUnitLiterals() {
        return unitLiterals;
    }

    public boolean checkForConflictingVariableAssignment(int variable, boolean wantedTruthValue) {
        int variableValue = variables[variable];

        if (variableValue == 0) {
            return false;
        }

        return ((variableValue < 0 ? false : true) ^ wantedTruthValue);
    }

    public int[] getVariables() {
        return variables;
    }

    public int getVariableWithMostOccurences() {

       return IntStream.range(1, variables.length).boxed().filter(a -> variables[a] == 0)
        .max((a,b) -> Integer.compare(positivelyWatchedDisjunctiveConstraints[a].size() + negativelyWatchedDisjunctiveConstraints[a].size(), positivelyWatchedDisjunctiveConstraints[b].size() + positivelyWatchedDisjunctiveConstraints[b].size())).orElse(-1);
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

}
