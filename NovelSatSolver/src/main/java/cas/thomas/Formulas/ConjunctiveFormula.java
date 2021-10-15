package cas.thomas.Formulas;

import java.util.List;

public class ConjunctiveFormula extends Formula {


    public ConjunctiveFormula(Variable[] variables, List<Literal> listOfUnitLiterals, Constraint[] constraints) {
        super(variables, listOfUnitLiterals, constraints);
    }
}
