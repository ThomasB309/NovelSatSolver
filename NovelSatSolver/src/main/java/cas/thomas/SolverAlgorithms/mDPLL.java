package cas.thomas.SolverAlgorithms;

import cas.thomas.ConflictHandling.ConflictHandlingStrategy;
import cas.thomas.Evaluation.Statistics;
import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.Formulas.Formula;
import cas.thomas.RestartHandling.RestartSchedulingStrategy;
import cas.thomas.VariableSelection.VariableSelectionStrategy;
import cas.thomas.utils.IntegerArrayQueue;
import cas.thomas.utils.IntegerStack;

public class mDPLL extends SolverAlgorithm {

    public mDPLL(VariableSelectionStrategy variableSelectionStrategy,
                 ConflictHandlingStrategy conflictHandlingStrategy,
                 RestartSchedulingStrategy restartSchedulingStrategy, boolean phaseSaving,
                 boolean firstBranchingDecision, long timeout) {

        super(variableSelectionStrategy, conflictHandlingStrategy, restartSchedulingStrategy, phaseSaving,
                firstBranchingDecision, timeout);
    }

    @Override
    public String solve(Formula formula) {

        boolean solution = false;
        try {
            solution = mDPPLAlgorithm(formula, firstBranchingDecision);
        } catch (UnitLiteralConflictException e) {
            return "UNSATISFIABLE";
        }

        //System.out.println(formula.getVariablesForSolutionChecker().stream().filter(x -> x > 0).collect(Collectors
        // .toList()));

        if (solution == true) {
            return "SATISFIABLE";
        } else if (unkown) {
            return "UNKNOWN";
        } else {
            return "UNSATISFIABLE";
        }

    }

    protected boolean mDPPLAlgorithm(Formula formula, boolean firstBranchingDecision) throws UnitLiteralConflictException {
        int numberOfVariables = formula.getNumberOfVariables();
        boolean conflict = false;
        IntegerStack trail = new IntegerStack(numberOfVariables);
        int[] variables = formula.getVariables();
        double[] variableOccurences = formula.getVariableOccurences();
        int[] variableDecisionLevel = formula.getDecisionLevelOfVariables();
        long startTime = System.currentTimeMillis();

        while (formula.getAssignedCounter() < numberOfVariables - 1) {

            if (timeout > 0 && System.currentTimeMillis() - startTime >= timeout) {
                unkown = true;
                return false;
            }

            if (!unitPropagation(trail, formula, variableDecisionLevel)) {
                conflict = true;
                if (!conflictHandlingStrategy.handleConflict(trail, formula, firstBranchingDecision,
                        variableDecisionLevel, variableSelectionStrategy)) {
                    conflicts++;
                    return false;
                } else {
                    conflicts++;
                }

                if (restartSchedulingStrategy.handleRestart(trail, formula, variableSelectionStrategy)) {
                    restarts++;
                    variableDecisionLevel = formula.resetDecisionLevelOfVariables();
                }


            } else {

                formula.increaseCurrentDecisionLevel();

                int nextVariable = variableSelectionStrategy.getNextVariable(variables, variableOccurences, conflict,
                        trail.peekFirst() == 0 ? 0 : trail.peekFirst());

                if (nextVariable == -1) {
                    continue;
                }

                branchings++;
                variableDecisionLevel[nextVariable] = formula.getCurrentDecisionLevel();
                boolean branching = phaseSaving(formula, nextVariable);
                formula.propagate(nextVariable, branching);
                trail.push(nextVariable);
            }


        }

        return true;


    }

    private boolean unitPropagation(IntegerStack trail, Formula formula,
                                    int[] variableDecisionLevels) {
        IntegerArrayQueue unitLiterals = formula.getUnitLiterals();

        if (formula.hasConflict()) {
            int conflictLiteral = formula.getConflictLiteral();
            int conflictLiteralAbsoluteValue = Math.abs(conflictLiteral);

            variableDecisionLevels[conflictLiteralAbsoluteValue] = formula.getCurrentDecisionLevel();
            formula.pseudoPropagate(conflictLiteralAbsoluteValue * formula.getUnitLiteralState()[conflictLiteralAbsoluteValue]);
            trail.push(-conflictLiteralAbsoluteValue);

            return false;
        }

        while (unitLiterals.size() > 0) {
            int nextLiteral = unitLiterals.poll();

            if (formula.hasConflict()) {
                int conflictLiteral = formula.getConflictLiteral();
                int conflictLiteralAbsoluteValue = Math.abs(conflictLiteral);

                variableDecisionLevels[conflictLiteralAbsoluteValue] = formula.getCurrentDecisionLevel();
                formula.pseudoPropagate(conflictLiteralAbsoluteValue * formula.getUnitLiteralState()[conflictLiteralAbsoluteValue]);
                trail.push(-conflictLiteralAbsoluteValue);


                return false;
            }

            if (formula.variableAlreadyHasTheSameValue(nextLiteral)) {
                continue;
            }

            unitPropagations++;
            variableDecisionLevels[Math.abs(nextLiteral)] = formula.getCurrentDecisionLevel();
            formula.setPhaseSavingLastAssignment(nextLiteral);
            formula.propagate(nextLiteral);
            trail.push(-Math.abs(nextLiteral));
            formula.getUnitLiteralState()[Math.abs(nextLiteral)] = 0;
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
