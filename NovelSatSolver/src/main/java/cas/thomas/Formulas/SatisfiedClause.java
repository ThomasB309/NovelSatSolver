package cas.thomas.Formulas;

public class SatisfiedClause extends Clause {

    public SatisfiedClause(int numberOfVariables, int[] variables) {
        super(numberOfVariables);
    }

    public SatisfiedClause(int numberOfVariables) {
        super(numberOfVariables);
    }

    @Override
    public Clause condition(int variable) {
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

}
