package cas.thomas.Formulas;

import java.util.List;

public class DisjunctiveFormula extends Formula {

    public DisjunctiveFormula(Constraint[] constraints, int numberOfClauses, int numberOfVariables, List<Integer> listOfUnitVariables) {
        super(constraints, numberOfClauses, numberOfVariables, listOfUnitVariables);
    }

    public DisjunctiveFormula(Formula formula) {
        super(formula);
    }

    @Override
    protected void checkIfIsEmptyClause(int fulfilledCounter) {
        if (fulfilledCounter >= 1) {
            this.setIsEmptyClause();
        }
    }

    @Override
    protected Formula copyClause(Formula formula) {
        return new DisjunctiveFormula(formula);
    }
}
