package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisjunctiveConstraint extends Constraint {


    public DisjunctiveConstraint(Literal[] literals) {
        super(literals);
    }

    @Override
    public List<Literal> condition(Literal literal) {

        if (literals[firstWatchedIndex].equals(literal)) {
            Literal literalFirst = literals[firstWatchedIndex];
            literalFirst.setAssignment(Assignment.NEGATIVE);

            for (int i = 0; i < literals.length; i++) {
                if (i != firstWatchedIndex && i != secondWatchedIndex && literals[i].getAssignment() == Assignment.OPEN) {
                    firstWatchedIndex = i;
                    return null;
                }
            }

            return Arrays.asList(literals[secondWatchedIndex]);


        } else {
            Literal literalFirst = literals[secondWatchedIndex];
            literalFirst.setAssignment(Assignment.NEGATIVE);

            for (int i = 0; i < literals.length; i++) {
                if (i != firstWatchedIndex && i != secondWatchedIndex && literals[i].getAssignment() == Assignment.OPEN) {
                    firstWatchedIndex = i;
                    return null;
                }
            }

            return Arrays.asList(literals[firstWatchedIndex]);
        }
    }

    @Override
    protected Literal[] getUnitLiterals() {
        return new Literal[0];
    }

    @Override
    protected Literal[] getWatchedLiterals() {
        return new Literal[] {literals[firstWatchedIndex], literals[secondWatchedIndex]};
    }

    public String toString() {
        String output = "";
        for (int i = 0; i < this.literals.length; i++) {
            output += literals[i].isNegated() ? "v -x" : "v x" + literals[i].getUniqueID();
        }

        return output;
    }



}
