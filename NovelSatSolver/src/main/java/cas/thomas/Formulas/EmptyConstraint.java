package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.List;

public class EmptyConstraint extends Constraint {

    public EmptyConstraint(int numberOfVariables, int[] variables) {
        super(numberOfVariables);
    }

    public EmptyConstraint(int numberOfVariables) {
        super(numberOfVariables);
    }

    @Override
    public Constraint condition(int variable) {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isSatisfied() {
        return false;
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
