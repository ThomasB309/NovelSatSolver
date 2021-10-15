package cas.thomas.SolverAlgorithms;

import cas.thomas.Formulas.Assignment;
import cas.thomas.Formulas.Formula;
import cas.thomas.Formulas.Formula2;
import cas.thomas.Formulas.Literal;
import cas.thomas.Formulas.Variable;
import cas.thomas.utils.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class mDPLL implements ISolverAlgorithm {

    private int[] variableOrdering;


    public mDPLL() {

    }

    @Override
    public String solve(Formula formula) {
        boolean solution = mDPPLAlgorithm(formula, new HashSet<>());

        if (solution == true) {
            return "SATISFIABLE";
        } else {
            return "UNSATISFIABLE";
        }

    }

    private boolean mDPPLAlgorithm(Formula formula, Set<Literal> partialAssignment) {
        formula = formula.copy();
        partialAssignment = new HashSet<>(partialAssignment);
        List<Literal> unitliterals = formula.getUnitLiterals();
        System.out.println(partialAssignment);
        while (unitliterals.size() > 0) {
            Literal unitliteral = unitliterals.remove(0);

            if (partialAssignment.contains(unitliteral)) {
                return false;
            }

            partialAssignment.add(unitliteral);
            formula.condition(unitliteral);
        }

        if (partialAssignment.size() == formula.getVariableSize()) {
            return true;
        }

        Variable variable = formula.getLiteral();
        Literal literal = new Literal(variable, false);

        formula.condition(literal);
        partialAssignment.add(literal);

        if (mDPPLAlgorithm(formula, partialAssignment)) {
            return true;
        }

        literal.setState(true);
        formula.condition(literal);

        if (mDPPLAlgorithm(formula, partialAssignment)) {
            return true;
        }

        variable.setAssigment(Assignment.OPEN);

        return false;



    }
}
