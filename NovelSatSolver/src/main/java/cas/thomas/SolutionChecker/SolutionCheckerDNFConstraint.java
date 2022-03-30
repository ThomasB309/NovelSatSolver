package cas.thomas.SolutionChecker;

import cas.thomas.utils.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class SolutionCheckerDNFConstraint extends SolutionCheckerConstraint {

    private int[][] terms;
    private int maxLiteral;

    public SolutionCheckerDNFConstraint(int[][] terms) {
        super();
        this.maxLiteral = Integer.MIN_VALUE;
        this.terms = new int[terms.length][];
        for (int i = 0; i < terms.length; i++) {
            this.terms[i] = Arrays.copyOf(terms[i], terms[i].length);
        }
    }

    @Override
    public boolean isTrue(List<Integer> literals) {
        int[] stateOfLiterals = new int[literals.stream().map(a -> Math.abs(a)).max(Integer::compareTo).get() + 1];

        for (ListIterator<Integer> listIterator = literals.listIterator(); listIterator.hasNext(); ) {
            int currentLiteral = listIterator.next();
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            stateOfLiterals[currentLiteralAbsoluteValue] = currentLiteral;

        }

        boolean formualSatisfied = false;

        for (int i = 0; i < terms.length; i++) {
            int trueCounter = 0;
            for (int j = 0; j < terms[i].length; j++) {
                int currentLiteral = terms[i][j];
                if (Math.abs(currentLiteral) >= stateOfLiterals.length) {
                    break;
                }

                if (isEqualLiteral(currentLiteral, stateOfLiterals[Math.abs(currentLiteral)])) {
                    if (!compareLiterals(currentLiteral, stateOfLiterals[Math.abs(currentLiteral)])) {
                        break;
                    } else {
                        trueCounter++;
                    }
                }
            }
            if (trueCounter == terms[i].length) {
                formualSatisfied = true;
                break;
            }
        }
        return formualSatisfied;
    }

    @Override
    public String toDimacsString() {
        String dimacsString = "DNF";

        for (int i = 0; i < terms.length; i++) {
            for (int j = 0; j < terms[i].length; j++) {
                dimacsString += " " + terms[i][j];
            }
            dimacsString += " 0";
        }

        dimacsString += " 0\n";

        return dimacsString;
    }

    @Override
    public Pair<Integer, Integer> toDimacsCNFString(StringBuilder cnfString, int maxVariable) {
        int nextHelperVariable = maxVariable + 1;
        int constraintCounter = 0;
        int[] dnfClause = new int[terms.length];
        for (int i = 0; i < terms.length; i++) {
            int[] term = terms[i];
            if (term.length < 2) {
                dnfClause[i] = term[0];
            } else {
                int termHelper = nextHelperVariable++;
                dnfClause[i] = termHelper;
                int[] termClause = new int[term.length + 1];
                termClause[0] = termHelper;
                for (int j = 0; j < term.length; j++) {
                    int lit = term[j];
                    termClause[j + 1] = -lit;
                    cnfString.append(new SolutionCheckerDisjunctiveConstraint(new int[]{-termHelper, lit}).toDimacsString());
                    constraintCounter++;

                }
                cnfString.append(new SolutionCheckerDisjunctiveConstraint(termClause).toDimacsString());
                constraintCounter++;
            }
        }
        cnfString.append(new SolutionCheckerDisjunctiveConstraint(dnfClause).toDimacsString());
        constraintCounter++;

        return new Pair<>(nextHelperVariable - 1, constraintCounter);
    }
}
