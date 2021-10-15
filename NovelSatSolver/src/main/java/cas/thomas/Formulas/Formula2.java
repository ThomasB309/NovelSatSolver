package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Formula2 {

    private Variable[] variables;
    private List<Literal> unitLiterals;

    public Formula2(Variable[] variables) {
        this.variables = variables;
        this.unitLiterals = new ArrayList<>();
    }

    public void conditionPositively(Variable variable) {
        unitLiterals.addAll(variable.conditionPositively());
    }

    public void conditionNegatively(Variable variable) {
        unitLiterals.addAll(variable.conditionNegatively());
    }

}
