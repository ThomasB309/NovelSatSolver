package cas.thomas.SolverAlgorithms;

import cas.thomas.Formulas.Formula;
import cas.thomas.VariableSelection.VariableSelectionStrategy;

public abstract class SolverAlgorithm {

    protected VariableSelectionStrategy variableSelectionStrategy;

    public abstract String solve(Formula formula);
}
