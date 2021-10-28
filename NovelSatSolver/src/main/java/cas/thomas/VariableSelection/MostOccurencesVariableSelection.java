package cas.thomas.VariableSelection;

import cas.thomas.Formulas.Assignment;
import cas.thomas.Formulas.Formula;
import cas.thomas.Formulas.Variable;

import java.util.Arrays;
import java.util.Comparator;

public class MostOccurencesVariableSelection implements  VariableSelectionStrategy {


    @Override
    public Variable getNextVariable(Formula formula) {
        return Arrays.stream(formula.getVariables()).filter(a -> a.getState() == Assignment.OPEN).max(Comparator.comparingInt(Variable::getNumberOfOccurences)).get();
    }
}
