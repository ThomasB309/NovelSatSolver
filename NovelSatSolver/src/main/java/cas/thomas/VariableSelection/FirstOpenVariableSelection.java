package cas.thomas.VariableSelection;

import cas.thomas.Formulas.Assignment;
import cas.thomas.Formulas.Formula;
import cas.thomas.Formulas.Variable;

public class FirstOpenVariableSelection implements VariableSelectionStrategy {

    @Override
    public Variable getNextVariable(Formula formula) {
        Variable[] variables = formula.getVariables();

        for (int i = 0; i < variables.length; i++) {
            if (variables[i].getState() == Assignment.OPEN) {
                return variables[i];
            }
        }

        return null;
    }
}
