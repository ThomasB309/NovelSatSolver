package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Formula {

    private Variable[] variables;
    private List<Literal> unitLiterals;
    private List<Literal> unitLiteralsBacktrack;
    private Constraint[] constraints;

    public Formula(Variable[] variables, List<Literal> listOfUnitLiterals, Constraint[] constraints) {
        this.variables = variables;
        this.unitLiterals = listOfUnitLiterals;
        this.unitLiteralsBacktrack = new ArrayList<>(listOfUnitLiterals);
        this.constraints = constraints;
    }

    private Formula() {

    }


    public Formula copy() {
        Formula formula = new Formula();

        formula.variables = Arrays.copyOf(this.variables, this.variables.length);
        formula.unitLiterals = new ArrayList<>(this.unitLiterals);
        formula.unitLiteralsBacktrack = new ArrayList<>(this.unitLiteralsBacktrack);
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

    public void condition(Variable variable, boolean truthValue) {
        unitLiterals.addAll(truthValue ? variable.conditionNegatively() : variable.conditionPositively());
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

    public void backtrackUnitLiterals() {
        this.unitLiterals.clear();
    }

    public Variable[] getVariables() {
        return variables;
    }

    public String toString() {
        String output = "";
        for (int i = 0; i < this.constraints.length; i++) {
            output += output.equals("") ? "(" + this.constraints[i].toString() + ")" :
                    " v (" + this.constraints[i].toString() + ")";
        }

        return output;
    }

}
