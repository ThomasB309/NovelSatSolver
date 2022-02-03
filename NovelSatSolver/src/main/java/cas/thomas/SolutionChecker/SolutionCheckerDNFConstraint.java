package cas.thomas.SolutionChecker;

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

        for (ListIterator<Integer> listIterator = literals.listIterator(); listIterator.hasNext();) {
            int currentLiteral = listIterator.next();
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            stateOfLiterals[currentLiteralAbsoluteValue] = currentLiteral;

        }

        boolean formualSatisfied = false;

        for (int i = 0; i < terms.length; i++) {
            int trueCounter = 0;
            for (int j = 0; j < terms[i].length; j++) {
                int currentLiteral = terms[i][j];
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

        dimacsString += " 0";

        return dimacsString;
    }

    @Override
    public String toDimacsCNFString() {
        return null;
    }
}
