package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConjunctiveConstraint extends Constraint {

    public ConjunctiveConstraint(Literal[] literals) {
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

            firstWatchedIndex = -1;

        } else if (literals[secondWatchedIndex].equals(literal)) {
            Literal literalFirst = literals[secondWatchedIndex];
            literalFirst.setAssignment(Assignment.NEGATIVE);

            for (int i = 0; i < literals.length; i++) {
                if (i != firstWatchedIndex && i != secondWatchedIndex && literals[i].getAssignment() == Assignment.OPEN) {
                    firstWatchedIndex = i;
                    return null;
                }
            }

            secondWatchedIndex = -1;
        }

        return Arrays.asList(firstWatchedIndex == -1 ? literals[secondWatchedIndex] : literals[firstWatchedIndex]);
    }

}
