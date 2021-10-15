package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Formula {

    private Variable[] variables;
    private List<Literal> unitLiterals;
    private Constraint[] constraints;

    public Formula(Variable[] variables, List<Literal> listOfUnitLiterals, Constraint[] constraints) {
        this.variables = variables;
        this.unitLiterals = listOfUnitLiterals;
        this.constraints = constraints;
    }

    private Formula() {

    }


    public Formula copy() {
        Formula formula = new Formula();

        formula.variables = Arrays.copyOf(this.variables, this.variables.length);
        formula.unitLiterals = new ArrayList<>(this.unitLiterals);
        formula.constraints = Arrays.copyOf(this.constraints, this.constraints.length);

        return formula;
    }

    public void conditionPositively(Variable variable) {
        unitLiterals.addAll(variable.conditionPositively());
    }

    public void conditionNegatively(Variable variable) {
        unitLiterals.addAll(variable.conditionNegatively());
    }

    public List<Literal> getUnitLiterals() {
        return this.unitLiterals;
    }

    public void condition(Literal literal) {
        unitLiterals.addAll(literal.condition());
    }

    public int getVariableSize() {
        return this.variables.length;
    }

    public Variable getLiteral() {
        for (int i = 0; i < variables.length; i++) {
            if (variables[i].getState() == Assignment.OPEN) {
                return variables[i];
            }
        }

        return null;
    }

}
