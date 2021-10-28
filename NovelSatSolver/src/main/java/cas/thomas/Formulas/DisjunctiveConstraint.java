package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDisjunctiveConstraint;

import java.util.Arrays;
import java.util.List;

public class DisjunctiveConstraint extends Constraint {

    private int firstWatchedIndex;
    private int secondWatchedIndex;


    public DisjunctiveConstraint(Literal[] literals) {
        super(literals);

        assert (literals.length >= 1);


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

    @Override
    public List<Literal> condition(Literal literal) {

        if (literals[firstWatchedIndex].equals(literal)) {

            for (int i = 0; i < literals.length; i++) {
                Assignment variableState = literals[i].getVariable().getState();
                boolean nextLiteralTruthValue = literals[i].getTruthValue();
                if (i != firstWatchedIndex && i != secondWatchedIndex &&
                        ((variableState == Assignment.OPEN)
                                || (variableState == Assignment.POSITIVE && nextLiteralTruthValue)
                                || variableState == Assignment.NEGATIVE && !nextLiteralTruthValue)) {
                    firstWatchedIndex = i;

                    Literal newWatchedLiteral = literals[i];

                    if (newWatchedLiteral.getTruthValue()) {
                        newWatchedLiteral.getVariable().addPositivelyWatched(this);
                    } else {
                        newWatchedLiteral.getVariable().addNegativelyWatched(this);
                    }


                    return null;
                }
            }

            return literals[secondWatchedIndex].propagationNeeded() ?
                    Arrays.asList(literals[secondWatchedIndex]) : null;


        } else {
            for (int i = 0; i < literals.length; i++) {
                Assignment variableState = literals[i].getVariable().getState();
                boolean nextLiteralTruthValue = literals[i].getTruthValue();
                if (i != firstWatchedIndex && i != secondWatchedIndex &&
                        ((variableState == Assignment.OPEN)
                                || (variableState == Assignment.POSITIVE && nextLiteralTruthValue)
                                || variableState == Assignment.NEGATIVE && !nextLiteralTruthValue)) {
                    secondWatchedIndex = i;

                    Literal newWatchedLiteral = literals[i];

                    if (newWatchedLiteral.getTruthValue()) {
                        newWatchedLiteral.getVariable().addPositivelyWatched(this);
                    } else {
                        newWatchedLiteral.getVariable().addNegativelyWatched(this);
                    }


                    return null;
                }
            }

            return literals[firstWatchedIndex].propagationNeeded() ?
                    Arrays.asList(literals[firstWatchedIndex]) : null;
        }
    }

    @Override
    protected Literal[] getWatchedLiterals() {
        return new Literal[]{literals[firstWatchedIndex], literals[secondWatchedIndex]};
    }

    @Override
    public SolutionCheckerConstraint getSolutionCheckerConstraint() {
        return new SolutionCheckerDisjunctiveConstraint(Arrays.stream(this.literals).mapToInt(literal -> literal.getUniqueID() * (literal.getTruthValue() ? 1 : -1)).toArray());
    }

    public String toString() {
        String output = "";
        for (int i = 0; i < this.literals.length; i++) {
            output += output.equals("") ? literals[i].toString() : " v " + literals[i].toString();
        }

        return output;
    }


}
