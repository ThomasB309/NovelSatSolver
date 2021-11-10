package cas.thomas.SolverAlgorithms;

import cas.thomas.ConflictHandling.ConflictHandlingStrategy;
import cas.thomas.Formulas.Formula;
import cas.thomas.RestartHandling.RestartSchedulingStrategy;
import cas.thomas.VariableSelection.VariableSelectionStrategy;
import cas.thomas.utils.Pair;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class mDPLL extends SolverAlgorithm {

    public mDPLL(VariableSelectionStrategy variableSelectionStrategy,
                 ConflictHandlingStrategy conflictHandlingStrategy,
                 RestartSchedulingStrategy restartSchedulingStrategy, boolean phaseSaving,
                 boolean firstBranchingDecision) {

        super(variableSelectionStrategy, conflictHandlingStrategy, restartSchedulingStrategy, phaseSaving,
                firstBranchingDecision);
    }

    @Override
    public String solve(Formula formula) {

        boolean solution = mDPPLAlgorithm(formula, firstBranchingDecision);

        System.out.println(formula.getVariablesForSolutionChecker());

        if (solution == true) {
            return "SATISFIABLE";
        } else {
            return "UNSATISFIABLE";
        }

    }

    private boolean mDPPLAlgorithm(Formula formula, boolean firstBranchingDecision) {
        int numberOfVariables = formula.getNumberOfVariables();
        boolean conflict = false;
        Deque<Integer> trail = new ArrayDeque<>();
        while (formula.getAssignedCounter() < numberOfVariables - 1) {

            if (!unitPropagation(trail, formula)) {
                conflict = true;
                if (!conflictHandlingStrategy.handleConflict(trail, formula, firstBranchingDecision)) {
                    return false;
                }

                restartSchedulingStrategy.handleRestart(trail, formula);


            } else {
                int nextVariable = variableSelectionStrategy.getNextVariable(formula, conflict);

                conflict = false;

                if (nextVariable == -1) {
                    continue;
                }

                boolean branching = phaseSaving(formula, nextVariable);
                formula.propagate(nextVariable, branching);
                trail.push(nextVariable);
            }



        }

        return true;


    }

    private boolean unitPropagation(Deque<Integer> trail, Formula formula) {
        List<Integer> unitLiterals = formula.getUnitLiterals();
        while (unitLiterals.size() > 0) {
            int nextLiteral = unitLiterals.remove(0);

            if (formula.hasConflict()) {
                formula.resetConflictState();
                formula.emptyUnitLiterals();
                return false;
            }

            if (formula.variableAlreadyHasTheSameValue(nextLiteral)) {
                continue;
            }

            formula.propagate(nextLiteral);
            trail.push(-Math.abs(nextLiteral));
        }

        return true;
    }

    private boolean phaseSaving(Formula formula, int nextVariable) {
        boolean branching = firstBranchingDecision;
        if (phaseSaving) {
            int lastAssignment = formula.getPhaseSavingLastAssignment(nextVariable);

            branching = lastAssignment == 0 ? firstBranchingDecision : (lastAssignment < 0 ? false : true);

            formula.setPhaseSavingLastAssignment(nextVariable * firstBranchingDecisionInteger);
        }

        return branching;

    }

}
