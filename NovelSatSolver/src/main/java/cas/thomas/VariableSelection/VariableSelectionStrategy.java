package cas.thomas.VariableSelection;

import cas.thomas.Formulas.Formula;

public interface VariableSelectionStrategy {

    public int getNextVariable(Formula formula);
}
