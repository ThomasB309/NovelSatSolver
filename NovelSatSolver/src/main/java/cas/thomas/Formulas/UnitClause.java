package cas.thomas.Formulas;

public class UnitClause extends Clause {

    private int unitClauseVariable;

    public UnitClause(int numberOfVariables, int[] variables) {
        super(numberOfVariables, variables);
    }

    public UnitClause(int numberOfVariables, int nonZeroCounter) {
        super(numberOfVariables, nonZeroCounter);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public void setUnitClassVariable(int unitClauseVariable) {
        this.unitClauseVariable = unitClauseVariable;
    }

    public int getUnitClauseVariable() {
        return this.unitClauseVariable;
    }


}
