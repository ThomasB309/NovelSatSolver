package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.List;

public class AMOConstraint extends Constraint {

    private boolean hasOneTrueVariable;
    private boolean hasBeenPropagated;

    public AMOConstraint(int numberOfVariables, int[] variables) {
        super(numberOfVariables, variables);
        this.hasOneTrueVariable = false;
        this.hasBeenPropagated = false;
    }

    public AMOConstraint(int numberOfVariables) {
        super(numberOfVariables);
        this.hasOneTrueVariable = false;
        this.hasBeenPropagated = false;
    }

    public AMOConstraint(int numberOfVariables, int nonZeroCounter) {
        super(numberOfVariables, nonZeroCounter);
        this.hasOneTrueVariable = false;
        this.hasBeenPropagated = false;
    }

    private AMOConstraint reducedAMOconstraint(int reducedVariable, boolean hasOneTrueVariable) {
        AMOConstraint clause = new AMOConstraint(this.variables.length, this.nonZeroCounter - 1);

        clause.hasBeenPropagated = this.hasBeenPropagated;

        clause.setVariables(this.variables);

        clause.variables[reducedVariable] = 0;

        clause.hasOneTrueVariable = hasOneTrueVariable;

        return clause;
    }

    @Override
    public Constraint condition(int variable) {
        int absoluteVariable = Math.abs(variable);

        assert (absoluteVariable < variables.length);

        int compareValue = compareValues(this.variables[absoluteVariable], variable);

        if (compareValue == -1) {

            if (this.nonZeroCounter > 1) {
                AMOConstraint clause = reducedAMOconstraint(absoluteVariable, this.hasOneTrueVariable);

                return clause;
            } else if (this.hasOneTrueVariable) {
                return new SatisfiedConstraint(this.variables.length);
            } else {
                return new EmptyConstraint(this.variables.length);
            }

        } else if (compareValue == 1) {

            if (this.hasOneTrueVariable == true) {
                return new EmptyConstraint(this.variables.length);
            }

            AMOConstraint clause = reducedAMOconstraint(absoluteVariable, true);

            return clause;

        }

        Constraint copiedConstraint = new AMOConstraint(this.variables.length, this.nonZeroCounter);
        copiedConstraint.setVariables(this.variables);

        return copiedConstraint;
    }

    @Override
    public String toString() {
        if (variables.length < 1) {
            return "";
        }

        String output = "AMO(";

        for (int i = 0; i < variables.length; i++) {
            if (output.equals("AMO(")) {
                if (variables[i] < 0) {
                    output += "-x" + i;
                } else if (variables[i] > 0) {
                    output += "x" + i;
                }
            } else {
                if (variables[i] < 0) {
                    output += " ; -x" + i;
                } else if (variables[i] > 0) {
                    output += " ; x" + i;
                }
            }
        }

        return output + ")";

    }

    @Override
    public boolean needsUnitResolution() {
        return this.hasOneTrueVariable && !hasBeenPropagated;
    }

    @Override
    public List<Integer> findUnitClauseVariable() {
        List<Integer> unitVariables = new ArrayList<>();

        if (!this.hasOneTrueVariable ||hasBeenPropagated) {
            return unitVariables;
        }

        for (int i = 1; i < variables.length; i++) {
            if (variables[i] < 0) {
                unitVariables.add(i);
            } else if (variables[i] > 0) {
                unitVariables.add(-i);
            }
        }

        return unitVariables;
    }


}
