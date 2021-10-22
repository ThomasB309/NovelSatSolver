package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.List;

public class Variable {

    private Assignment state;

    private int uniqueID;
    private List<Constraint> positivelyWatched;
    private List<Constraint> negativelyWatched;

    public Variable(int uniqueID) {
        this.state = Assignment.OPEN;
        this.uniqueID = uniqueID;
        this.positivelyWatched = new ArrayList<>();
        this.negativelyWatched = new ArrayList<>();
    }

    public void setAssigment(Assignment assignment) {
        this.state = assignment;
    }

    public void addPositivelyWatched(Constraint constraint) {
        this.positivelyWatched.add(constraint);
    }

    public void addNegativelyWatched(Constraint constraint) {
        this.negativelyWatched.add(constraint);
    }

    public List<Literal> conditionNegatively() {
        this.state = Assignment.POSITIVE;
        return condition(this.negativelyWatched, false);
    }

    public List<Literal> conditionPositively() {
        this.state = Assignment.NEGATIVE;
        return condition(this.positivelyWatched, true);
    }

    private List<Literal> condition(List<Constraint> watchedList, boolean truthValue) {
        List<Literal> unitLiterals = new ArrayList<>();
        List<Integer> removableIndices = new ArrayList<>();
        for (int i = 0; i < watchedList.size(); i++) {
            Literal conditionedLiteral = new Literal(this, truthValue);
            List<Literal> partialUnitLiterals = watchedList.get(i).condition(new Literal(this, truthValue));

            Literal[] watchedLiterals = watchedList.get(i).getWatchedLiterals();

            if (!watchedLiterals[0].equals(conditionedLiteral) && !watchedLiterals[1].equals(conditionedLiteral)) {
                removableIndices.add(i);
            }

            if (partialUnitLiterals != null) {
                unitLiterals.addAll(partialUnitLiterals);
            }
        }

        for (int i = removableIndices.size() - 1; i >= 0; i--) {
            watchedList.remove(removableIndices.get(i).intValue());
        }


        return unitLiterals;
    }

    public boolean equals(Object comparator) {
        if (!(comparator instanceof Variable)) {
            return false;
        }

        Variable comparatorConverted = (Variable) comparator;

        return this.uniqueID == comparatorConverted.uniqueID;
    }

    public int hashCode() {
        return this.uniqueID;
    }

    public Literal getAnyLiteral() {
        if (this.positivelyWatched.size() > 0) {
            return new Literal(this, false);
        } else if (this.negativelyWatched.size() > 0) {
            return  new Literal(this, false);
        } else {
            return null;
        }
    }

    public Assignment getState() {
        return this.state;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public String toString() {
        if (this.state == Assignment.NEGATIVE) {
            return "-x" + this.uniqueID;
        } else if (this.state == Assignment.OPEN) {
            return "~x" + this.uniqueID;
        } else {
            return "x" + this.uniqueID;
        }
    }
}
