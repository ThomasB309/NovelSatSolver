package cas.thomas.SolverAlgorithms;

import cas.thomas.Formulas.Formula;
import cas.thomas.VariableSelection.VariableSelectionStrategy;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class mDPLL extends SolverAlgorithm {

    public mDPLL(VariableSelectionStrategy variableSelectionStrategy) {
        this.variableSelectionStrategy = variableSelectionStrategy;
    }

    @Override
    public String solve(Formula formula) {
        boolean solution = mDPPLAlgorithm(formula, false);

        System.out.println(formula.getVariablesForSolutionChecker());

        if (solution == true) {
            return "SATISFIABLE";
        } else {
            return "UNSATISFIABLE";
        }

    }

    private boolean mDPPLAlgorithm(Formula formula, boolean firstBranchingDecision) {
        int numberOfVariables = formula.getNumberOfVariables();
        Deque<Integer> trail = new ArrayDeque<>();
        int branchingDecision = firstBranchingDecision ? 1 : -1;

        while (formula.getAssignedCounter() < numberOfVariables - 1) {

            if (!unitPropagation(trail, formula)) {
                int nextLiteral;
                if ((nextLiteral = findLastLiteralNotTriedBothValues(trail, branchingDecision, formula)) == -1) {
                    return false;
                }

                trail.push(-trail.pop());
                formula.propagateAfterSwappingVariableAssigment(Math.abs(nextLiteral), !firstBranchingDecision);

            } else {
                int nextVariable = variableSelectionStrategy.getNextVariable(formula);

                if (nextVariable == -1) {
                    continue;
                }

                formula.propagate(nextVariable, firstBranchingDecision);
                trail.push(nextVariable);
            }



        }

        return true;


    }

    private boolean unitPropagation(Deque<Integer> trail, Formula formula) {
        List<Integer> unitLiterals = formula.getUnitLiterals();
        while (unitLiterals.size() > 0) {
            int nextLiteral = unitLiterals.remove(0);

            if (formula.variableAlreadyHasTheSameValue(nextLiteral)) {
                continue;
            }

            if (!formula.propagate(nextLiteral)) {
                formula.emptyUnitLiterals();
                return false;
            }
            trail.push(-Math.abs(nextLiteral));
        }

        return true;
    }

    private int findLastLiteralNotTriedBothValues(Deque<Integer> trail, int firstBranchingDecision, Formula formula) {
        Iterator<Integer> trailIterator = trail.iterator();

        while (trailIterator.hasNext()) {
            int nextLiteral = trailIterator.next();

            if (nextLiteral > 0) {
                return nextLiteral;
            }

            formula.unassignVariable(nextLiteral);
            trailIterator.remove();

        }

        return -1;
    }

}
