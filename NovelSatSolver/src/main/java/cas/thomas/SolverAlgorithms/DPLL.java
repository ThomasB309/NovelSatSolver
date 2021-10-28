package cas.thomas.SolverAlgorithms;

import cas.thomas.Formulas.Formula;

import java.util.Set;

public class DPLL extends SolverAlgorithm {

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
        return null;
    }
}
