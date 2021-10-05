package cas.thomas.Formulas;

public class EmptyClause extends Clause {

    public EmptyClause(int formulaPosition, int numberOfVariables, int[] variables) {
        super(formulaPosition, numberOfVariables);
    }

    public EmptyClause(int formulaPosition, int numberOfVariables) {
        super(formulaPosition, numberOfVariables);
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
    public boolean isFullfilled() {
        return false;
    }


}
