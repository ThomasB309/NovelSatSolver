package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.List;

public class AMOConstraint extends Constraint {


    public AMOConstraint(Literal[] literals) {
        super(literals);
    }

    @Override
    public List<Literal> condition(Literal literal) {
        return null;
    }

    @Override
    protected Literal[] getWatchedLiterals() {
        return new Literal[0];
    }
}
