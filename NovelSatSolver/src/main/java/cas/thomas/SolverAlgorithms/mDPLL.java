package cas.thomas.SolverAlgorithms;

import cas.thomas.ConflictHandling.ConflictHandlingStrategy;
import cas.thomas.Formulas.Constraint;
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
        int[] variables = formula.getVariables();
        double[] variableOccurences = formula.getVariableOccurences();
        int[] variableDecisionLevel = formula.getDecisionLevelOfVariables();

        while (formula.getAssignedCounter() < numberOfVariables - 1) {

            if (!unitPropagation(trail, formula, variableDecisionLevel)) {
                conflict = true;
                if (!conflictHandlingStrategy.handleConflict(trail, formula, firstBranchingDecision,
                        variableDecisionLevel)) {
                    return false;
                }

                restartSchedulingStrategy.handleRestart(trail, formula);


            } else {
                formula.increaseCurrentDecisionLevel();

                int nextVariable = variableSelectionStrategy.getNextVariable(variables, variableOccurences, conflict,
                        trail.peekFirst() == null ? 0 : trail.peekFirst());

                if (nextVariable == -1) {
                    continue;
                }

                variableDecisionLevel[nextVariable] = formula.getCurrentDecisionLevel();
                boolean branching = phaseSaving(formula, nextVariable);
                formula.propagate(nextVariable, branching);
                trail.push(nextVariable);
            }



        }

        return true;


    }

    private boolean unitPropagation(Deque<Integer> trail, Formula formula,
                                    int[] variableDecisionLevels) {
        List<Integer> unitLiterals = formula.getUnitLiterals();

        if (formula.hasConflict()) {
            formula.resetConflictState();
            formula.emptyUnitLiterals();
            return false;
        }

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

            variableDecisionLevels[Math.abs(nextLiteral)] = formula.getCurrentDecisionLevel();
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
