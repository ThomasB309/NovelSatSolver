package cas.thomas.Formulas;

import java.util.List;

public class Literal {

    private Variable variable;
    private boolean truthValue;

    public Literal(Variable variable, boolean truthValue) {
        this.variable = variable;
        this.truthValue = truthValue;
    }

    public int getUniqueID() {
        return this.variable.getUniqueID();
    }

    public boolean getTruthValue() {
        return this.truthValue;
    }

    public void setState(boolean negated) {
        this.truthValue = negated;
    }

    public List<Literal> condition() {
        return truthValue ? this.variable.conditionPositively() : this.variable.conditionNegatively();
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

    public Variable getVariable() {
        return variable;
    }

    public void addConstraintToVariableWatchlist(Constraint constraint) {
        if (this.truthValue) {
            this.variable.addPositivelyWatched(constraint);
        } else {
            this.variable.addNegativelyWatched(constraint);
        }
    }

    public String toString() {
        String partialLiteral = this.truthValue ? "x" + this.variable.getUniqueID() : "-x" + this.variable.getUniqueID();
        if (this.variable.getState() == Assignment.OPEN) {
            return partialLiteral;
        } else if (this.variable.getState() == Assignment.POSITIVE) {
            return partialLiteral + " " + (this.truthValue ? "(TRUE)" : "(FALSE)");
        } else {
            return partialLiteral + " " + (this.truthValue ? "(FALSE)" : "(TRUE)");
        }
    }

    public boolean isFalseWithCurrentVariableAssignment() {
        if (this.variable.getState() == Assignment.NEGATIVE && this.truthValue == true) {
            return true;
        } else if (this.variable.getState() == Assignment.POSITIVE && this.truthValue == false) {
            return true;
        } else {
            return false;
        }
    }

    public boolean propagationNeeded() {
        if (this.variable.getState() == Assignment.NEGATIVE && this.truthValue == true) {
            return true;
        } else if (this.variable.getState() == Assignment.POSITIVE && this.truthValue == false) {
            return true;
        } else if (this.variable.getState() == Assignment.OPEN) {
            return true;
        } else {
            return false;
        }
    }

}
