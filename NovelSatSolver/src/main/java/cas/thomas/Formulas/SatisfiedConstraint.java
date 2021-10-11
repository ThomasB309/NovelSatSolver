package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.List;

public class SatisfiedConstraint extends Constraint {

    public SatisfiedConstraint(int numberOfVariables, int[] variables) {
        super(numberOfVariables);
    }

    public SatisfiedConstraint(int numberOfVariables) {
        super(numberOfVariables);
    }

    @Override
    public Constraint condition(int variable) {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isSatisfied() {
        return true;
    }

    @Override
    public boolean needsUnitResolution() {
        return false;
    }

    @Override
    public List<Integer> findUnitClauseVariable() {
        return new ArrayList<>();
    }

}
