package cas.thomas.Formulas;

import java.util.Arrays;

public class Clause {

    protected int[] variables;
    private int nonZeroCounter;

    public Clause(int numberOfVariables, int[] variables) {

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

    public Clause(int numberOfVariables) {
        this.variables = new int[numberOfVariables + 1];
    }

    public Clause(int numberOfVariables, int nonZeroCounter) {
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

    public Clause condition(int variable) {

        int absoluteVariable = Math.abs(variable);

        assert (absoluteVariable < variables.length);

        int compareValue = compareValues(this.variables[absoluteVariable], variable);

        if (compareValue == -1) {

            if (this.nonZeroCounter == 2) {
                UnitClause clause = new UnitClause(this.variables.length, this.nonZeroCounter - 1);
                clause.setVariables(this.variables);
                clause.variables[absoluteVariable] = 0;


                return clause;
            }

            if (this.nonZeroCounter == 1) {
                return new EmptyClause(this.variables.length);
            }

            Clause clause = new Clause(this.variables.length, this.nonZeroCounter - 1);

            clause.setVariables(this.variables);

            clause.variables[absoluteVariable] = 0;

            return clause;
        } else if (compareValue == 1) {
            return new SatisfiedClause(this.variables.length);
        }

        Clause copiedClause = new Clause(this.variables.length, this.nonZeroCounter);
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
        return nonZeroCounter == 0;
    }

    public boolean isSatisfied() {
        return false;
    }

    public int[] getVariables() {
        return this.variables;
    }

    public boolean isUnitClause() {
        return nonZeroCounter == 1;
    }

    public int findUnitClauseVariable() {
        for (int i = 1; i < variables.length; i++) {
            if (variables[i] < 0) {
                return -i;
            } else if (variables[i] > 0) {
                return  i;
            }
        }

        return 0;
    }

}
