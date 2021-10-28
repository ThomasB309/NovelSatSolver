package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.List;

public class Variable {

    private Assignment state;

    private int uniqueID;
    private int positiveOccurenceCounter;
    private int negativeOccurenceCounter;
    private List<DisjunctiveConstraint> positivelyWatchedDisjunctive;
    private List<DisjunctiveConstraint> negativelyWatchedDisjunctive;
    private List<AMOConstraint> positivelyWatchedAMO;
    private List<AMOConstraint> negativelyWatchedAMO;

    public Variable(int uniqueID) {
        this.positiveOccurenceCounter = 0;
        this.negativeOccurenceCounter = 0;
        this.state = Assignment.OPEN;
        this.uniqueID = uniqueID;
        this.positivelyWatchedDisjunctive = new ArrayList<DisjunctiveConstraint>();
        this.negativelyWatchedDisjunctive = new ArrayList<DisjunctiveConstraint>();
        this.positivelyWatchedAMO = new ArrayList<>();
        this. negativelyWatchedAMO = new ArrayList<>();
    }

    public void setAssigment(Assignment assignment) {
        this.state = assignment;
    }

    public void addPositivelyWatched(DisjunctiveConstraint constraint) {
        this.positivelyWatchedDisjunctive.add(constraint);
    }

    public void addNegativelyWatched(DisjunctiveConstraint constraint) {
        this.negativelyWatchedDisjunctive.add(constraint);
    }

    public void addPositivelyWatched(AMOConstraint constraint) {
        this.positivelyWatchedAMO.add(constraint);
    }

    public void addNegativelyWatched(AMOConstraint constraint) {
        this.negativelyWatchedAMO.add(constraint);
    }

    public List<Literal> conditionNegatively() {
        this.state = Assignment.POSITIVE;
        return condition(this.negativelyWatchedDisjunctive, this.positivelyWatchedAMO,false);
    }

    public List<Literal> conditionPositively() {
        this.state = Assignment.NEGATIVE;
        return condition(this.positivelyWatchedDisjunctive, this.negativelyWatchedAMO,true);
    }

    private List<Literal> condition(List<DisjunctiveConstraint> watchedListDisjunctive,
                                    List<AMOConstraint> watchedListAMO, boolean truthValue) {
        List<Literal> unitLiterals = new ArrayList<>();
        List<Integer> removableIndices = new ArrayList<>();
        List<Integer> removableIndicesAMO = new ArrayList<>();
        for (int i = 0; i < watchedListDisjunctive.size(); i++) {
            Literal conditionedLiteral = new Literal(this, truthValue);
            List<Literal> partialUnitLiterals = watchedListDisjunctive.get(i).condition(new Literal(this, truthValue));

            Literal[] watchedLiterals = watchedListDisjunctive.get(i).getWatchedLiterals();

            if (!watchedLiterals[0].equals(conditionedLiteral) && !watchedLiterals[1].equals(conditionedLiteral)) {
                removableIndices.add(i);
            }

            if (partialUnitLiterals != null) {
                unitLiterals.addAll(partialUnitLiterals);
            }
        }

        for (int i = 0; i < watchedListAMO.size(); i++) {
            List<Literal> partialUnitLiterals = watchedListAMO.get(i).condition(new Literal(this, truthValue));

            removableIndicesAMO.add(i);

            if (partialUnitLiterals != null) {
                unitLiterals.addAll(partialUnitLiterals);
            }
        }

        for (int i = removableIndices.size() - 1; i >= 0; i--) {
            watchedListDisjunctive.remove(removableIndices.get(i).intValue());
        }

        /*for (int i = removableIndicesAMO.size() - 1; i >= 0; i--) {
            watchedListAMO.remove(removableIndicesAMO.get(i).intValue());
        }*/


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
        if (this.positivelyWatchedDisjunctive.size() > 0) {
            return new Literal(this, false);
        } else if (this.negativelyWatchedDisjunctive.size() > 0) {
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

    public void addPositiveOccurence() {
        this.positiveOccurenceCounter++;
    }

    public void addNegativeOccurence() {
        this.negativeOccurenceCounter++;
    }

    public int getNumberOfOccurences() {
        return this.positiveOccurenceCounter + this.negativeOccurenceCounter;
    }

    public int getNumberOfPositiveOccurences() {
        return this.positiveOccurenceCounter;
    }

    public int getNumberOfNegativeOccurences() {
        return this.negativeOccurenceCounter;
    }
}
