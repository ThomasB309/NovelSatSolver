package cas.thomas.Formulas;

import java.util.List;

public class DNFConstraint extends Constraint {

    private DisjunctiveFormula formula;

    public DNFConstraint(int numberOfVariables, int[] variables) {
        super(numberOfVariables, variables);
    }

    public DNFConstraint(int numberOfVariables) {
        super(numberOfVariables);
    }

    public DNFConstraint(int numberOfVariables, int nonZeroCounter) {
        super(numberOfVariables, nonZeroCounter);
    }

    @Override
    public Constraint condition(int variable) {
        return null;
    }

    @Override
    public boolean needsUnitResolution() {
        return false;
    }

    @Override
    public List<Integer> findUnitClauseVariable() {
        return null;
    }
}
