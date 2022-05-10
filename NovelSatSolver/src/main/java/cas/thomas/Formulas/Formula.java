package cas.thomas.Formulas;

import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.VariableSelection.VariableSelectionStrategy;
import cas.thomas.utils.IntegerArrayQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class Formula {

    int assignedCounter;
    private int[] variables;
    private int[] unitLiteralState;
    private double[] variableOccurences;
    private int[] phaseSavingLastAssignment;
    private List<Constraint>[] positivelyWatchedDisjunctiveConstraints;
    private List<Constraint>[] negativelyWatchedDisjunctiveConstraints;
    private List<Constraint>[] positivelWatchedAMOConstraints;
    private List<Constraint>[] negativelyWatchedAMOConstraints;
    private List<Constraint>[] positivelyWatchedDNFConstraints;
    private List<Constraint>[] negativelyWatchedDNFConstraints;
    private int currentDecisionLevel;
    private int[] decisionLevelOfVariables;
    private Constraint[] reasonConstraints;
    private Constraint[] reasonConstraintsBeforePropagation;
    private Constraint conflictClause;
    private boolean hasConflict;
    private IntegerArrayQueue unitLiterals;
    private int conflictLiteral;

    private Set<Integer> unitLiteralsBeforePropagation;
    private Set<Integer> assumptions;
    private long numberOfDNFPropagations;
    private long numberOfUselessDNFPropagations;
    private long numberOfClausePropagations;

    public Formula(int variableCount, Constraint[] reasonConstraints, double[] variableOccurences,
                   List<Constraint>[] positivelyWatchedDisjunctiveConstraints
            , List<Constraint>[] negativelyWatchedDisjunctiveConstraints,
                   List<Constraint>[] positivelWatchedAMOConstraints,
                   List<Constraint>[] negativelyWatchedAMOConstraints,
                   List<Constraint>[] positivelyWatchedDNFConstraints,
                   List<Constraint>[] negativelyWatchedDNFConstraints, List<Integer> unitLiteralsBeforePropagation) throws UnitLiteralConflictException {

        this.variables = new int[variableCount];
        this.unitLiteralState = new int[variableCount];
        this.phaseSavingLastAssignment = new int[variableCount];
        this.variableOccurences = variableOccurences;
        this.reasonConstraintsBeforePropagation = reasonConstraints;
        this.decisionLevelOfVariables = new int[variableCount];
        this.unitLiterals = new IntegerArrayQueue(variableCount + 1);
        this.conflictClause = null;
        this.hasConflict = false;
        this.positivelyWatchedDisjunctiveConstraints = positivelyWatchedDisjunctiveConstraints;
        this.negativelyWatchedDisjunctiveConstraints = negativelyWatchedDisjunctiveConstraints;
        this.positivelWatchedAMOConstraints = positivelWatchedAMOConstraints;
        this.negativelyWatchedAMOConstraints = negativelyWatchedAMOConstraints;
        this.positivelyWatchedDNFConstraints = positivelyWatchedDNFConstraints;
        this.negativelyWatchedDNFConstraints = negativelyWatchedDNFConstraints;
        this.assignedCounter = 0;
        this.currentDecisionLevel = 0;
        this.conflictLiteral = 0;
        this.unitLiteralsBeforePropagation = new HashSet<>(unitLiteralsBeforePropagation);
        this.assumptions = new HashSet<>();
        this.reasonConstraints = new Constraint[variables.length];

        setUnitLiteralsBeforePropagation();

    }

    public void setUnitLiteralsBeforePropagation() throws UnitLiteralConflictException {
        setUnitLiterals();
        setAssumptions();
        reasonConstraints = Arrays.copyOf(reasonConstraintsBeforePropagation,
                reasonConstraintsBeforePropagation.length);
    }

    private void setUnitLiterals() throws UnitLiteralConflictException {
        for (Integer literal : unitLiteralsBeforePropagation) {
            int literalAbsoluteValue = Math.abs(literal);
            if (unitLiteralState[literalAbsoluteValue] * literal < 0) {
                throw new UnitLiteralConflictException("The formula is not satisfiable");
            } else if (unitLiteralState[literalAbsoluteValue] == 0) {
                unitLiterals.offer(literal);
                unitLiteralState[Math.abs(literal)] = literal < 0 ? -1 : 1;
            }
        }
    }

    private void setAssumptions() throws UnitLiteralConflictException {
        for (Integer literal : assumptions) {
            int literalAbsoluteValue = Math.abs(literal);
            if (unitLiteralState[literalAbsoluteValue] * literal < 0) {
                throw new UnitLiteralConflictException("The formula is not satisfiable");
            } else if (unitLiteralState[literalAbsoluteValue] == 0) {
                unitLiterals.offer(literal);
                unitLiteralsBeforePropagation.add(literal);
                unitLiteralState[Math.abs(literal)] = literal < 0 ? -1 : 1;
            }
        }
    }

    public Formula(List<int[]> clauses, List<int[]> amoConstraints, List<int[][]> dnfConstraints, int maxVariable) throws UnitLiteralConflictException {
        instantiateIncremental(maxVariable);

        addClausesIncremental(clauses);

        addAMOConstraintsIncremental(amoConstraints);

        addDNFConstraintsIncremental(dnfConstraints);

        setUnitLiteralsBeforePropagation();
    }

    public void instantiateIncremental(int maxVariable) {
        int variableCount = maxVariable + 1;
        this.variables = new int[variableCount];
        this.unitLiteralState = new int[variableCount];
        this.phaseSavingLastAssignment = new int[variableCount];
        this.variableOccurences = new double[variableCount];
        this.reasonConstraintsBeforePropagation = new Constraint[variableCount];
        this.reasonConstraints = new Constraint[variableCount];
        this.decisionLevelOfVariables = new int[variableCount];
        this.unitLiterals = new IntegerArrayQueue(variableCount);
        this.conflictClause = null;
        this.hasConflict = false;
        this.positivelyWatchedDisjunctiveConstraints = new ArrayList[variableCount];
        this.negativelyWatchedDisjunctiveConstraints = new ArrayList[variableCount];
        this.positivelWatchedAMOConstraints = new ArrayList[variableCount];
        this.negativelyWatchedAMOConstraints = new ArrayList[variableCount];
        this.positivelyWatchedDNFConstraints = new ArrayList[variableCount];
        this.negativelyWatchedDNFConstraints = new ArrayList[variableCount];

        for (int i = 0; i < variableCount; i++) {
            positivelyWatchedDisjunctiveConstraints[i] = new ArrayList<>();
            negativelyWatchedDisjunctiveConstraints[i] = new ArrayList<>();
            positivelWatchedAMOConstraints[i] = new ArrayList<>();
            negativelyWatchedAMOConstraints[i] = new ArrayList<>();
            positivelyWatchedDNFConstraints[i] = new ArrayList<>();
            negativelyWatchedDNFConstraints[i] = new ArrayList<>();
        }


        this.assignedCounter = 0;
        this.currentDecisionLevel = 0;
        this.conflictLiteral = 0;
        this.unitLiteralsBeforePropagation = new HashSet<>();
        this.assumptions = new HashSet<>();
    }

    private void addClausesIncremental(List<int[]> clauses) throws UnitLiteralConflictException {
        for (int[] clause : clauses) {

            for (int i = 0; i < clause.length; i++) {
                int literalAbsoluteValue = Math.abs(clause[i]);
                variableOccurences[literalAbsoluteValue] += 1;
            }

            DisjunctiveConstraint disjunctiveConstraint = addDisjunctiveConstraint(clause);

            Set<Integer> unitLiteralsNeededBeforePropagation =
                    disjunctiveConstraint.getUnitLiteralsNeededBeforePropagation();

            addUnitLiteralsNeededBeforePropagation(disjunctiveConstraint, unitLiteralsNeededBeforePropagation);


        }
    }

    private void addAMOConstraintsIncremental(List<int[]> amoConstraints) {
        for (int[] amoConstraint : amoConstraints) {
            for (int i = 0; i < amoConstraint.length; i++) {
                int literalAbsoluteValue = Math.abs(amoConstraint[i]);
                variableOccurences[literalAbsoluteValue] += 1;
            }

            addAMOConstraint(amoConstraint);
        }
    }

    private void addDNFConstraintsIncremental(List<int[][]> dnfConstraints) throws UnitLiteralConflictException {
        for (int[][] term : dnfConstraints) {

            for (int i = 0; i < term.length; i++) {
                for (int j = 0; j < term[i].length; j++) {
                    int literalAbsoluteValue = Math.abs(term[i][j]);
                    variableOccurences[literalAbsoluteValue] += 1;
                }
            }

            DNFConstraint dnfConstraint = addDNFConstraints(term);

            Set<Integer> unitLiteralsNeededBeforePropagation = dnfConstraint.getUnitLiteralsNeededBeforePropagation();

            addUnitLiteralsNeededBeforePropagation(dnfConstraint, unitLiteralsNeededBeforePropagation);
        }
    }

    public DisjunctiveConstraint addDisjunctiveConstraint(int[] literals) {
        if (literals.length == 2) {
            return new BinaryDisjunctiveConstraint(literals, positivelyWatchedDisjunctiveConstraints,
                    negativelyWatchedDisjunctiveConstraints);
        } else {
            return new DisjunctiveConstraint(literals, positivelyWatchedDisjunctiveConstraints,
                    negativelyWatchedDisjunctiveConstraints);
        }
    }

    private void addUnitLiteralsNeededBeforePropagation(Constraint constraint,
                                                        Set<Integer> unitLiteralsNeededBeforePropagation) throws UnitLiteralConflictException {
        for (Integer literal : unitLiteralsNeededBeforePropagation) {
            int literalAbsoluteValue = Math.abs(literal);
            if (unitLiteralState[literalAbsoluteValue] * literal < 0) {
                throw new UnitLiteralConflictException("The formula is not satisfiable");
            } else if (unitLiteralState[literalAbsoluteValue] == 0) {
                unitLiterals.offer(literal);
                unitLiteralsBeforePropagation.add(literal);
                unitLiteralState[Math.abs(literal)] = literal < 0 ? -1 : 1;
                if (reasonConstraintsBeforePropagation[Math.abs(literal)] == null) {
                    reasonConstraintsBeforePropagation[Math.abs(literal)] = constraint;
                }
            }

        }
    }

    private AMOConstraint addAMOConstraint(int[] literals) {
        return new AMOConstraint(literals, positivelWatchedAMOConstraints,
                negativelyWatchedAMOConstraints);
    }

    public DNFConstraint addDNFConstraints(int[][] terms) {
        return terms.length == 2 ? new BinaryDNFConstraint(terms, positivelyWatchedDNFConstraints,
                negativelyWatchedDNFConstraints, variables) :
                new DNFConstraint(terms,
                        positivelyWatchedDNFConstraints,
                        negativelyWatchedDNFConstraints, variables.length);
    }

    public void propagate(int literal) {
        propagate(Math.abs(literal), literal >= 0);
    }

    public void pseudoPropagate(int literal) {
        assignedCounter++;
        variables[Math.abs(literal)] = literal >= 0 ? 1 : -1;
    }

    public void propagate(int variable, boolean truthValue) {

        assignedCounter++;

        if (truthValue) {
            variables[variable] = 1;
            propagateInDisjunctiveConstraints(negativelyWatchedDisjunctiveConstraints[variable], variable);
            propagateInAMOConstraints(positivelWatchedAMOConstraints[variable], variable);
            propagateInDNFConstraints(negativelyWatchedDNFConstraints[variable], variable);
        } else {
            variables[variable] = -1;
            propagateInDisjunctiveConstraints(positivelyWatchedDisjunctiveConstraints[variable], -variable);
            propagateInAMOConstraints(negativelyWatchedAMOConstraints[variable], -variable);
            propagateInDNFConstraints(positivelyWatchedDNFConstraints[variable], -variable);
        }

    }

    private void propagateInDisjunctiveConstraints(List<Constraint> watchedList, int literal) {
        for (Iterator<Constraint> constraintIterator = watchedList.iterator(); constraintIterator.hasNext(); ) {
            Constraint currentConstraint = constraintIterator.next();

            if (currentConstraint.isObsolete()) {
                constraintIterator.remove();
                continue;
            }

            numberOfClausePropagations++;

            if (!currentConstraint.propagate(literal, variables, unitLiteralState, unitLiterals,
                    positivelyWatchedDisjunctiveConstraints
                    , negativelyWatchedDisjunctiveConstraints, reasonConstraints)) {
                constraintIterator.remove();
            }

            int conflictLiteralClause = currentConstraint.resetConflictState();

            if (hasConflict(currentConstraint, conflictLiteralClause)) return;
        }
    }

    private void propagateInAMOConstraints(List<Constraint> watchedList, int literal) {
        for (Iterator<Constraint> constraintIterator = watchedList.iterator(); constraintIterator.hasNext(); ) {
            Constraint currentConstraint = constraintIterator.next();


            currentConstraint.propagate(literal, variables, unitLiteralState, unitLiterals,
                    positivelWatchedAMOConstraints,
                    negativelyWatchedAMOConstraints, reasonConstraints);

            int conflictLiteralAMO = currentConstraint.resetConflictState();

            if (hasConflict(currentConstraint, conflictLiteralAMO)) return;
        }
    }

    private void propagateInDNFConstraints(List<Constraint> watchedList, int literal) {
        for (Iterator<Constraint> constraintIterator = watchedList.iterator(); constraintIterator.hasNext(); ) {
            Constraint currentConstraint = constraintIterator.next();

            numberOfDNFPropagations++;

            if (currentConstraint.isObsolete()) {
                constraintIterator.remove();
                continue;
            }

            if (!currentConstraint.isStillWatched(literal, variables)) {
                numberOfUselessDNFPropagations++;
                currentConstraint.removeAddedLiteral(literal);
                constraintIterator.remove();
                continue;
            }

            currentConstraint.propagate(literal, variables, unitLiteralState, unitLiterals,
                    positivelyWatchedDNFConstraints
                    , negativelyWatchedDNFConstraints, reasonConstraints);

            if (!currentConstraint.isStillWatched(literal,variables)) {
                currentConstraint.removeAddedLiteral(literal);
                constraintIterator.remove();
            }


            int conflictLiteralClause = currentConstraint.resetConflictState();

            if (hasConflict(currentConstraint, conflictLiteralClause)) return;
        }
    }

    private boolean hasConflict(Constraint currentConstraint, int conflictLiteralClause) {
        if (conflictLiteral == 0) {
            conflictLiteral = conflictLiteralClause;
        }

        if (!hasConflict && conflictLiteral != 0) {
            conflictClause = currentConstraint;
            hasConflict = true;
            return true;
        }
        return false;
    }

    public void propagateAfterSwappingVariableAssigment(int variable, boolean truthValue) {
        assignedCounter--;
        assert(variables[variable] != 0);
        variables[variable] *= -1;
        propagate(variable, variables[variable] > 0);
    }

    public int getNumberOfVariables() {
        return variables.length;
    }

    public void unassignVariable(int literal) {
        int literalAbsoluteValue = Math.abs(literal);

        /*if (variables[literalAbsoluteValue] < 0) {
            ListIterator<Constraint> watchedList = positivelyWatchedDNFConstraints[literalAbsoluteValue].listIterator();
            while (watchedList.hasNext()) {
                watchedList.next().backtrack(literalAbsoluteValue * variables[literalAbsoluteValue], variables);
            }

        } else if (variables[literalAbsoluteValue] > 0) {
            ListIterator<Constraint> watchedList = negativelyWatchedDNFConstraints[literalAbsoluteValue].listIterator();
            while (watchedList.hasNext()) {
                watchedList.next().backtrack(literalAbsoluteValue * variables[literalAbsoluteValue], variables);
            }
        }*/

        assignedCounter--;
        variables[literalAbsoluteValue] = 0;
    }

    public int getAssignedCounter() {
        return assignedCounter;
    }

    public IntegerArrayQueue getUnitLiterals() {
        return unitLiterals;
    }

    public int[] getVariables() {
        return variables;
    }

    public void emptyUnitLiterals() {
        unitLiterals = new IntegerArrayQueue(variables.length + 1);
        unitLiteralState = new int[unitLiteralState.length];
    }

    public void addUnitLiteralBeforePropagation(int literal, Constraint reasonConstraint) {
        unitLiteralsBeforePropagation.add(literal);
        reasonConstraintsBeforePropagation[Math.abs(literal)] = reasonConstraint;
    }

    public int[] getVariablesForSolutionChecker() {

        return variables;
    }

    public boolean variableAlreadyHasTheSameValue(int literal) {
        return variables[Math.abs(literal)] * literal > 0;
    }

    public boolean hasConflict() {
        return hasConflict;
    }

    public Constraint getConflictClause() {
        return conflictClause;
    }

    public int getConflictLiteral() {
        return conflictLiteral;
    }

    public void setConflictLiteral(int literal) {
        this.conflictLiteral = literal;
    }

    public void resetConflictState() {
        hasConflict = false;
        conflictLiteral = 0;
    }

    public Constraint getReasonClauses(int literal) {
        int literalsAbsoluteValue = Math.abs(literal);
        Constraint reasonConstraint = reasonConstraints[literalsAbsoluteValue];

        if (reasonConstraint == null) {
            return null;
        } else if (reasonConstraint.isObsolete()) {
            reasonConstraints[literalsAbsoluteValue] = null;
            return null;
        }

        return reasonConstraints[literalsAbsoluteValue];
    }

    public void removeReasonClauses() {
        reasonConstraints = new Constraint[variables.length];
    }

    public boolean adjustVariableScores(int[] literals, long conflictIndex, VariableSelectionStrategy variableSelectionStrategy) {
        boolean infinite = false;
        double g = 1 / (0.8 + (0.01 * (conflictIndex / 5000)));

        if (g < 1.05) {
            g = 1.05;
        }

        for (int i = 0; i < literals.length; i++) {
            if (literals[i] >= 1) {
                int value = literals[i];

                final double addedValue = value * Math.pow(g, conflictIndex);
                final double variableScore = variableOccurences[i];

                if (Double.isInfinite(variableScore + addedValue)) {

                    double max = Arrays.stream(variableOccurences).max().getAsDouble();

                    for (int a = 0; a < variableOccurences.length; a++) {
                        variableOccurences[a] /= max;
                    }

                    conflictIndex = 1;

                    infinite = true;

                    variableSelectionStrategy.recreatePriorityQueue(variables, variableOccurences);
                }

                variableOccurences[i] += value * Math.pow(g, conflictIndex);
                variableSelectionStrategy.heapify(i);
            }
        }

        return infinite;
    }

    public void addAssumptions(int[] assumptions) throws UnitLiteralConflictException {
        for (Integer literal : assumptions) {
            int literalAbsoluteValue = Math.abs(literal);
            if (unitLiteralState[literalAbsoluteValue] * literal < 0) {
                throw new UnitLiteralConflictException("The formula is not satisfiable");
            } else if (unitLiteralState[literalAbsoluteValue] == 0) {
                unitLiterals.offer(literal);
                this.assumptions.add(literal);
                unitLiteralState[Math.abs(literal)] = literal < 0 ? -1 : 1;
            }
        }
    }

    public double[] getVariableOccurences() {
        return variableOccurences;
    }

    public void setPhaseSavingLastAssignment(int literal) {
        phaseSavingLastAssignment[Math.abs(literal)] = literal < 0 ? -1 : 1;
    }

    public int getPhaseSavingLastAssignment(int literal) {
        return phaseSavingLastAssignment[Math.abs(literal)];
    }

    public void increaseCurrentDecisionLevel() {
        currentDecisionLevel++;
    }

    public int getCurrentDecisionLevel() {
        return currentDecisionLevel;
    }

    public void setCurrentDecisionLevel(int currentDecisionLevel) {
        this.currentDecisionLevel = currentDecisionLevel;
    }

    public int[] getDecisionLevelOfVariables() {
        return decisionLevelOfVariables;
    }

    public int[] resetDecisionLevelOfVariables() {
        this.decisionLevelOfVariables = new int[variables.length];
        return decisionLevelOfVariables;
    }

    public int[] getUnitLiteralState() {
        return unitLiteralState;
    }

    public List<Constraint>[] getPositivelyWatchedDNFConstraints() {
        return positivelyWatchedDNFConstraints;
    }

    public List<Constraint>[] getNegativelyWatchedDNFConstraints() {
        return negativelyWatchedDNFConstraints;
    }

    public void setReasonConstraint(int literal, Constraint constraint) {
        reasonConstraints[Math.abs(literal)] = constraint;
    }

    public long getNumberOfDNFPropagations() {
        return numberOfDNFPropagations;
    }

    public long getNumberOfUselessDNFPropagations() {
        return numberOfUselessDNFPropagations;
    }

    public long getNumberOfClausePropagations() {
        return numberOfClausePropagations;
    }
}
