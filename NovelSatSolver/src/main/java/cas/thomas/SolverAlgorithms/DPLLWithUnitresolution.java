package cas.thomas.SolverAlgorithms;

import cas.thomas.Formulas.Formula;
import cas.thomas.utils.Pair;

import java.util.HashSet;
import java.util.Set;

public class DPLLWithUnitresolution implements ISolverAlgorithm {

    private int[] variableOrdering;


    public DPLLWithUnitresolution(int[] variableOrdering) {
        this.variableOrdering = variableOrdering;
    }

    @Override
    public String solve(Formula formula) {
        Set<Integer> solution = DPLLWithUnitResolution(formula);

        if (solution != null) {
            return solution.toString();
        } else {
            return "UNSATISFIABLE";
        }

    }

    private Set<Integer> DPLLWithUnitResolution(Formula formula) {
        Set<Integer> tempLiteralSet;
        Pair<Set<Integer>, Formula> temp = formula.unitResolution();

        Formula currentFormula = temp.getSecondPairPart();
        Set<Integer> unitVariables = temp.getFirstPairPart();

        if (currentFormula.isEmptyClause()) {
            return unitVariables;
        } else if (currentFormula.containsEmptyClause()) {
            return null;
        } else {
            int literal = currentFormula.getFirstLiteral();

            if ((tempLiteralSet = DPLLWithUnitResolution(currentFormula.condition(literal))) != null) {
                tempLiteralSet.addAll(unitVariables);
                tempLiteralSet.add(literal);

                return tempLiteralSet;
            } else if ((tempLiteralSet = DPLLWithUnitResolution(currentFormula.condition(-literal))) != null) {
                tempLiteralSet.addAll(unitVariables);
                tempLiteralSet.add(-literal);

                return tempLiteralSet;
            } else {
                return null;
            }
        }
    }
}
