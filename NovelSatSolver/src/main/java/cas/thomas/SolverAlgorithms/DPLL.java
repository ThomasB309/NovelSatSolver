package cas.thomas.SolverAlgorithms;

import cas.thomas.Formulas.Clause;
import cas.thomas.Formulas.Formula;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DPLL implements ISolverAlgorithm {

    private int[] variableOrdering;


    public DPLL(int[] variableOrdering) {
        this.variableOrdering = variableOrdering;
    }

    @Override
    public String solve(Formula formula) {
        Set<Integer> solution = DPLL(formula, 0);

        if (solution != null) {
            return solution.toString();
        } else {
            return "UNSATISFIABLE";
        }
    }

    private Set<Integer> DPLL(Formula formula, int depth) {
        Set<Integer> tempLiteralSet;

        if (formula.isEmptyClause()) {
            return new HashSet<>();
        } else if (formula.containsEmptyClause()) {
            return null;
        } else if ((tempLiteralSet = DPLL(formula.condition(variableOrdering[depth]), depth + 1)) != null) {
            tempLiteralSet.add(variableOrdering[depth]);

            return tempLiteralSet;
        } else if ((tempLiteralSet = DPLL(formula.condition(-variableOrdering[depth]), depth + 1)) != null) {
            tempLiteralSet.add(-variableOrdering[depth]);

            return tempLiteralSet;
        } else {
            return null;
        }
    }
}
