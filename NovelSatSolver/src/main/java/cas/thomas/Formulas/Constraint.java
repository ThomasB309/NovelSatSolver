package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Constraint {

    protected int[] variables;
    protected int nonZeroCounter;
    protected Map<Integer, Integer> variableMapping;

    public Constraint(int numberOfVariables, int[] variables) {

        this.variableMapping = new HashMap<>(variables.length, 1);

        this.variables = new int[variables.length];

        for (int i = 0; i < variables.length; i++) {

            variableMapping.put(Math.abs(variables[i]), i);
            variableMapping.put(-i, Math.abs(variables[i]));

            int mappedVariable = variableMapping.get(Math.abs(variables[i]));
            int variable = variables[i];

            if (variable < 0) {
                this.variables[mappedVariable] = -1;
            } else {
                this.variables[mappedVariable] = 1;
            }
        }

        this.nonZeroCounter = variables.length;
    }

    public Constraint(int numberOfVariables) {
        this.variables = new int[numberOfVariables + 1];
    }

    public Constraint(int numberOfVariables, int nonZeroCounter) {
        this.variables = new int[numberOfVariables + 1];
        this.nonZeroCounter = nonZeroCounter;
    }


    public void setVariables(int[] variableArray) {
        assert (variableArray.length == this.variables.length);

        this.variables = Arrays.copyOf(variableArray, variableArray.length);

    }

    public void setMap(Map<Integer, Integer> variableMapping) {
        this.variableMapping = new HashMap<>(variableMapping);
    }

    public boolean solve(int[] variables) {
        assert(this.variables.length == variables.length);

        for (int i = 0; i < variables.length; i++) {

            int variable = variables[i];

            if (compareValues(this.variables[Math.abs(variable)], variable) == 1) {
                return true;
            }

        }

        return false;
    }

    public abstract Constraint condition(int variable);

    protected int compareValues(int a, int b) {
        if (a == 1 && b > 0) {
            return 1;
        } else if (a == 1 && b < 0) {
            return -1;
        } else if (a == -1 && b > 0) {
            return -1;
        } else if (a == -1 && b < 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public boolean isEmpty() {
        return nonZeroCounter == 0;
    }

    public boolean isSatisfied() {
        return false;
    }

    public int[] getVariables() {
        return this.variables;
    }

    public int getVariableFromClauseArrayIndex(int index) {
        return variableMapping.get(-index);
    }

    public abstract boolean needsUnitResolution();

    public abstract List<Integer> findUnitClauseVariable();

}
