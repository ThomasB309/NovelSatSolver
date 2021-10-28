package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerAMOConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AMOConstraint extends Constraint {

    public AMOConstraint(Literal[] literals) {
        super(literals);
        for (int i = 0; i < literals.length; i++) {
            literals[i].addConstraintToVariableWatchList(this);
        }
    }

    @Override
    public List<Literal> condition(Literal literal) {

        List<Literal> unitLiterals = new ArrayList<>(this.literals.length);

        for (int i = 0; i < this.literals.length; i++) {
            Literal currentLiteral = this.literals[i];
            if (!literal.equals(currentLiteral)) {
                unitLiterals.add(new Literal(currentLiteral.getVariable(), !currentLiteral.getTruthValue()));
            }
        }

        return unitLiterals;
    }

    @Override
    protected Literal[] getWatchedLiterals() {
        return new Literal[0];
    }

    @Override
    public SolutionCheckerConstraint getSolutionCheckerConstraint() {
        return new SolutionCheckerAMOConstraint(Arrays.stream(literals).mapToInt(literal -> literal.getUniqueID() * (literal.getTruthValue() ? 1 : -1)).toArray());
    }
}
