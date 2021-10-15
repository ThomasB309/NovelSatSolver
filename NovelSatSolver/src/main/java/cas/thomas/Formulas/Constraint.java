package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (literals.length > 1) {
            firstWatchedIndex = 0;
            secondWatchedIndex = 1;

            literals[firstWatchedIndex].addConstraintToVariableWatchlist(this);
            literals[secondWatchedIndex].addConstraintToVariableWatchlist(this);
        } else if (literals.length == 1) {
            firstWatchedIndex = 0;
            secondWatchedIndex = 1;

            literals[firstWatchedIndex].addConstraintToVariableWatchlist(this);
        } else {
            firstWatchedIndex = -1;
            secondWatchedIndex = -1;
        }
    }


    public abstract List<Literal> condition(Literal literal);

    public boolean isUnitConstraint() {
        return this.isUnitConstraint;
    }

    public List<Literal> getUnitLiterals() {
        return this.unitLiterals;
    }

    protected abstract Literal[] getWatchedLiterals();

}
