package cas.thomas.VariableSelection;

import cas.thomas.Formulas.Formula;

public interface VariableSelectionStrategy {

    int getNextVariable(Formula formula, boolean conflictLastRound);
}
