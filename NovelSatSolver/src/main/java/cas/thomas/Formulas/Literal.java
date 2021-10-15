package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.List;

public class Literal {

    private Variable variable;
    private boolean negated;
    private Assignment assignment;

    public Literal(Variable variable, boolean negated) {
        this.variable = variable;
        this.negated = negated;
        this.assignment = Assignment.OPEN;
    }

    public int getUniqueID() {
        return this.variable.getUniqueID();
    }

    public boolean isNegated() {
        return this.negated;
    }

    public void setState(boolean negated) {
        this.negated = negated;
    }

    public List<Literal> condition() {
        return negated ? this.variable.conditionPositively() : this.variable.conditionNegatively();
    }

    public boolean equals(Object comparator) {
        if (!(comparator instanceof Literal)) {
            return false;
        }

        Literal convertedComparator = (Literal) comparator;
        return variable.equals(convertedComparator.variable);
    }

    public int hashCode() {
        return this.variable.hashCode();
    }

    public Assignment getAssignment() {
        return this.assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Variable getVariable() {
        return variable;
    }

    public void addConstraintToVariableWatchlist(Constraint constraint) {
        if (this.negated) {
            this.variable.addNegativelyWatched(constraint);
        } else {
            this.variable.addPositivelyWatched(constraint);
        }
    }

    public String toString() {
        return this.variable.toString();
    }

}
