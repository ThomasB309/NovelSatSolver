package cas.thomas.SolverAlgorithms;

import cas.thomas.Formulas.Assignment;
import cas.thomas.Formulas.Formula;
import cas.thomas.Formulas.Literal;
import cas.thomas.Formulas.Variable;
import cas.thomas.VariableSelection.VariableSelectionStrategy;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class mDPLL extends SolverAlgorithm {

    public mDPLL(VariableSelectionStrategy variableSelectionStrategy) {
        this.variableSelectionStrategy = variableSelectionStrategy;
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
        partialAssignment = new HashSet<>(partialAssignment);
        List<Literal> unitliterals = formula.getUnitLiterals();
        List<Variable> variablesToRevert = new LinkedList<>();

        while (unitliterals.size() > 0) {
            Literal unitliteral = unitliterals.remove(0);

            if (unitliteral.isFalseWithCurrentVariableAssignment()) {
                for (int i = 0; i < variablesToRevert.size(); i++) {
                    variablesToRevert.get(i).setAssigment(Assignment.OPEN);
                }
                formula.backtrackUnitLiterals();
                return false;
            } else if (unitliteral.getVariable().getState() == Assignment.OPEN) {
                variablesToRevert.add(unitliteral.getVariable());
                partialAssignment.add(unitliteral);
                formula.condition(unitliteral.getVariable(), unitliteral.getTruthValue());
            }
        }


        if (partialAssignment.size() == formula.getVariableSize()) {
            return true;
        }

        Variable variable = this.variableSelectionStrategy.getNextVariable(formula);

        variablesToRevert.add(variable);
        Literal literal = new Literal(variable, true);

        formula.condition(variable, true);
        partialAssignment.add(literal);

        if (mDPPLAlgorithm(formula, partialAssignment)) {
            return true;
        }

        literal.setState(false);
        formula.condition(variable, false);

        if (mDPPLAlgorithm(formula, partialAssignment)) {
            return true;
        }

        for (int i = 0; i < variablesToRevert.size(); i++) {
            variablesToRevert.get(i).setAssigment(Assignment.OPEN);
        }

        return false;


    }

}
