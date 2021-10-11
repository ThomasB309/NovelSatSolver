package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Constraint {

    protected int[] variables;
    protected int nonZeroCounter;

    public Constraint(int numberOfVariables, int[] variables) {

        this.variables = new int[numberOfVariables + 1];

        for (int i = 0; i < variables.length; i++) {
            int variable = variables[i];

            if (variable < 0) {
                this.variables[Math.abs(variable)] = -1;
            } else {
                this.variables[variable] = 1;
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

    public abstract boolean needsUnitResolution();

    public abstract List<Integer> findUnitClauseVariable();

}
