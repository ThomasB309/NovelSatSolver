package cas.thomas.VariableSelection;

import cas.thomas.Formulas.Formula;

public interface VariableSelectionStrategy {

    int getNextVariable(int[] variables, double[] variableOccurences, boolean conflictLastRound, int lastLiteral);
}
