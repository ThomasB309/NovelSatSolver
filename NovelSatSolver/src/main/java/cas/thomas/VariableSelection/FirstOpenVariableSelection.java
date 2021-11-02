package cas.thomas.VariableSelection;

import cas.thomas.Formulas.Formula;

public class FirstOpenVariableSelection implements VariableSelectionStrategy {

    @Override
    public int getNextVariable(Formula formula) {
        int[] variables = formula.getVariables();

        for (int i = 1; i < variables.length; i++) {
            if (variables[i] == 0) {
                return i;
            }
        }

        return -1;
    }
}
