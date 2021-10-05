package cas.thomas.Formulas;

public class FulfilledClause extends Clause {

    public FulfilledClause(int formulaPosition, int numberOfVariables, int[] variables) {
        super(formulaPosition, numberOfVariables);
    }

    public FulfilledClause(int formulaPosition, int numberOfVariables) {
        super(formulaPosition, numberOfVariables);
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
    public boolean isFullfilled() {
        return true;
    }

}
