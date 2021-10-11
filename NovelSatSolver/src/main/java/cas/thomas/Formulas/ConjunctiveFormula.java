package cas.thomas.Formulas;

import java.util.List;

public class ConjunctiveFormula extends Formula {

    public ConjunctiveFormula(Constraint[] constraints, int numberOfClauses, int numberOfVariables, List<Integer> listOfUnitVariables) {
        super(constraints, numberOfClauses, numberOfVariables, listOfUnitVariables);
    }

    public ConjunctiveFormula(Formula formula) {
        super(formula);
    }

    @Override
    protected void checkIfIsEmptyClause(int fullfilledCounter) {
        if (fullfilledCounter == this.constraints.length) {
            this.setIsEmptyClause();
        }
    }

    @Override
    protected Formula copyClause(Formula formula) {
        return new ConjunctiveFormula(formula);
    }
}
