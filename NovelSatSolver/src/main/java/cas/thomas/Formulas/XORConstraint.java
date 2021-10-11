package cas.thomas.Formulas;

import java.util.List;

public class XORConstraint extends Constraint {

    public XORConstraint(int numberOfVariables, int[] variables) {
        super(numberOfVariables, variables);
    }

    public XORConstraint(int numberOfVariables) {
        super(numberOfVariables);
    }

    public XORConstraint(int numberOfVariables, int nonZeroCounter) {
        super(numberOfVariables, nonZeroCounter);
    }


    @Override
    public Constraint condition(int variable) {
        return null;
    }

    @Override
    public boolean needsUnitResolution() {
        return false;
    }

    @Override
    public List<Integer> findUnitClauseVariable() {
        return null;
    }

    @Override
    public String toString() {
        if (variables.length < 1) {
            return "";
        }

        String output = "XOR(";

        for (int i = 0; i < variables.length; i++) {
            if (output.equals("")) {
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
}
