package cas.thomas.SolutionChecker;

import cas.thomas.utils.Pair;

import java.util.List;

public abstract class SolutionCheckerConstraint {


    public abstract boolean isTrue(List<Integer> literals);

    protected boolean compareLiterals(int literalA, int literalB) {
        int absLiteralA = Math.abs(literalA);
        int absLiteralB = Math.abs(literalB);

        if (absLiteralA == absLiteralB) {
            if (literalA < 0 && literalB < 0) {
                return true;
            } else if (literalA > 0 && literalB > 0) {
                return true;
            }
        }

        return false;
    }

    protected boolean isEqualLiteral(int literalA, int literalB) {
        if (Math.abs(literalA) == Math.abs(literalB)) {
            return true;
        }

        return false;
    }

    public abstract String toDimacsString();

    public abstract Pair<Integer, Integer> toDimacsCNFString(StringBuilder cnfString, int maxVariables);
}
