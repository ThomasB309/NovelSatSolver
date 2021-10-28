package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;

import java.util.ArrayList;
import java.util.List;

public abstract class Constraint {

    protected int firstWatchedIndex;
    protected int secondWatchedIndex;
    protected boolean isUnitConstraint;
    protected Literal[] literals;
    protected List<Literal> unitLiterals;

    public Constraint(Literal[] literals) {

        this.literals = literals;
        this.isUnitConstraint = false;
        this.unitLiterals = new ArrayList<>();

    }


    public abstract List<Literal> condition(Literal literal);

    public boolean isUnitConstraint() {
        return this.isUnitConstraint;
    }

    public List<Literal> getUnitLiterals() {
        return this.unitLiterals;
    }

    protected abstract Literal[] getWatchedLiterals();

    public abstract SolutionCheckerConstraint getSolutionCheckerConstraint();

}
