package cas.thomas.SolutionChecker;

import java.util.Arrays;
import java.util.List;

public class SolutionCheckerDisjunctiveConstraint extends SolutionCheckerConstraint {

    private int[] literals;

    public SolutionCheckerDisjunctiveConstraint(int[] literals) {
        super();
        this.literals = Arrays.copyOf(literals, literals.length);
    }

    @Override
    public boolean isTrue(List<Integer> literals) {
        for (int i = 0; i < literals.size(); i++) {
            int listLiteral = literals.get(i);
            for (int a = 0; a < this.literals.length; a++) {
                int constraintLiteral = this.literals[a];
                if (isEqualLiteral(listLiteral, constraintLiteral)) {
                    if (compareLiterals(listLiteral, constraintLiteral)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public String toDimacsString() {
        String dimacsString = "";
        for (int i = 0; i < literals.length; i++) {
            dimacsString += literals[i] + " ";
        }
        dimacsString += "0";

        return dimacsString;
    }

    @Override
    public String toDimacsCNFString() {
        return toDimacsString();
    }

}
