package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.List;

public class DisjunctiveConstraint extends Constraint {

    public DisjunctiveConstraint(int numberOfVariables, int[] variables) {
        super(numberOfVariables, variables);
    }

    public DisjunctiveConstraint(int numberOfVariables) {
        super(numberOfVariables);
    }

    public DisjunctiveConstraint(int numberOfVariables, int nonZeroCounter) {
        super(numberOfVariables, nonZeroCounter);
    }

    @Override
    public Constraint condition(int variable) {
        int absoluteVariable = Math.abs(variable);

        assert (absoluteVariable < variables.length);

        int compareValue = compareValues(this.variables[absoluteVariable], variable);

        if (compareValue == -1) {

            if (this.nonZeroCounter == 1) {
                return new EmptyConstraint(this.variables.length);
            }

            DisjunctiveConstraint clause = new DisjunctiveConstraint(this.variables.length, this.nonZeroCounter - 1);

            clause.setVariables(this.variables);

            clause.variables[absoluteVariable] = 0;

            return clause;
        } else if (compareValue == 1) {
            return new SatisfiedConstraint(this.variables.length);
        }

        DisjunctiveConstraint copiedClause = new DisjunctiveConstraint(this.variables.length, this.nonZeroCounter);
        copiedClause.setVariables(this.variables);

        return copiedClause;
    }

    @Override
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

    @Override
    public boolean isEmpty() {
        return nonZeroCounter == 0;
    }

    @Override
    public boolean needsUnitResolution() {
        return nonZeroCounter == 1;
    }

    @Override
    public List<Integer> findUnitClauseVariable() {

        List<Integer> unitVariables = new ArrayList<>();

        if (nonZeroCounter != 1) {
            return unitVariables;
        }

        for (int i = 1; i < variables.length; i++) {
            if (variables[i] < 0) {
                unitVariables.add(-i);
            } else if (variables[i] > 0) {
                unitVariables.add(i);
            }
        }

        return unitVariables;
    }
}
