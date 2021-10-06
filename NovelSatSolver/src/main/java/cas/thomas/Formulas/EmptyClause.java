package cas.thomas.Formulas;

public class EmptyClause extends Clause {

    public EmptyClause(int numberOfVariables, int[] variables) {
        super(numberOfVariables);
    }

    public EmptyClause(int numberOfVariables) {
        super(numberOfVariables);
    }

    @Override
    public Clause condition(int variable) {
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


}
