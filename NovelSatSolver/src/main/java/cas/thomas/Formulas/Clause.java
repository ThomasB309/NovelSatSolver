package cas.thomas.Formulas;

import java.util.Arrays;

public class Clause {

    private int[] variables;
    private int formulaPosition;

    public Clause(int formulaPosition, int numberOfVariables, int[] variables) {

        this.variables = new int[numberOfVariables + 1];

        for (int i = 0; i < variables.length; i++) {
            int variable = variables[i];

            if (variable < 0) {
                this.variables[Math.abs(variable)] = -1;
            } else {
                this.variables[variable] = 1;
            }
        }

        this.formulaPosition = formulaPosition;
    }

    public Clause(int formulaPosition, int numberOfVariables) {
        this.variables = new int[numberOfVariables + 1];
        this.formulaPosition = formulaPosition;
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

    public Clause condition(int variable) {

        int absoluteVariable = Math.abs(variable);

        assert (absoluteVariable < variables.length);

        int compareValue = compareValues(this.variables[absoluteVariable], variable);

        if (compareValue == -1) {
            Clause clause = new Clause(this.formulaPosition, this.variables.length);

            clause.setVariables(this.variables);

            clause.variables[absoluteVariable] = 0;

            if (clause.isEmpty()) {
                return new EmptyClause(this.formulaPosition, this.variables.length);
            }

            return clause;
        } else if (compareValue == 1) {
            return new FulfilledClause(this.formulaPosition, this.variables.length);
        }

        Clause copiedClause = new Clause(this.formulaPosition, this.variables.length);
        copiedClause.setVariables(this.variables);

        return copiedClause;
    }

    public String toString() {

        if (variables.length < 1) {
            return "";
        }

        String output = "";

        for (int i = 0; i < variables.length; i++) {
            if (output.equals("")) {
                if (variables[i] < 0) {
                    output += "-x" + i;
                } else if (variables[i] > 0) {
                    output += "x" + i;
                }
            } else {
                if (variables[i] < 0) {
                    output += " v -x" + i;
                } else if (variables[i] > 0) {
                    output += " v x" + i;
                }
            }
        }

        return output;


    }

    private int compareValues(int a, int b) {
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
        for (int i = 0; i < variables.length; i++) {
            if (variables[i] != 0) {
                return false;
            }
        }

        return true;
    }

    public boolean isFullfilled() {
        return false;
    }


}
