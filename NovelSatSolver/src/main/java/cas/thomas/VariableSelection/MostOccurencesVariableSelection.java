package cas.thomas.VariableSelection;

import cas.thomas.Formulas.Formula;

public class MostOccurencesVariableSelection implements  VariableSelectionStrategy {


    @Override
    public int getNextVariable(Formula formula) {
        return formula.getVariableWithMostOccurences();
    }
}
