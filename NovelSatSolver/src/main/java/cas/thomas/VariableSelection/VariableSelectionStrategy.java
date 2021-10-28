package cas.thomas.VariableSelection;

import cas.thomas.Formulas.Formula;
import cas.thomas.Formulas.Variable;

public interface VariableSelectionStrategy {

    public Variable getNextVariable(Formula formula);
}
