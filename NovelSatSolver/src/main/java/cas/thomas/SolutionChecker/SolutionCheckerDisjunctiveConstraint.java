package cas.thomas.SolutionChecker;

import cas.thomas.utils.Pair;

import java.util.Arrays;
import java.util.List;

public class SolutionCheckerDisjunctiveConstraint extends SolutionCheckerConstraint {

    private int[] literals;

    public SolutionCheckerDisjunctiveConstraint(int[] literals) {
        super();
        this.literals = Arrays.copyOf(literals, literals.length);
    }

    @Override
    public boolean isTrue(int[] literals) {
        for (int a = 0; a < this.literals.length; a++) {
            int constraintLiteral = this.literals[a];
            int constraintLiteralAbsoluteValue = Math.abs(constraintLiteral);
            if (constraintLiteral * literals[constraintLiteralAbsoluteValue] > 0) {
                return true;
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
        dimacsString += "0\n";

        return dimacsString;
    }

    @Override
    public Pair<Integer, Integer> toDimacsCNFString(StringBuilder cnfString, int maxVariables) {
        cnfString.append(toDimacsString());
        return new Pair<>(maxVariables, 1);
    }

}
