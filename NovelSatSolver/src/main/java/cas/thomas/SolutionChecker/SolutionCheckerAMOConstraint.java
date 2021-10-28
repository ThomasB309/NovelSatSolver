package cas.thomas.SolutionChecker;

import java.util.List;

public class SolutionCheckerAMOConstraint extends SolutionCheckerConstraint {

    public SolutionCheckerAMOConstraint(int[] literals) {
        super(literals);
    }

    @Override
    public boolean isTrue(List<Integer> literals) {
        int trueCounter = 0;
        for (int i = 0; i < literals.size(); i++) {
            int listLiteral = literals.get(i);
            for (int a = 0; a < this.literals.length; a++) {
                int constraintLiteral = this.literals[a];
                if (isEqualLiteral(listLiteral, constraintLiteral)) {
                    if (compareLiterals(listLiteral, constraintLiteral)) {
                        trueCounter++;

                        if (trueCounter > 1) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;

    }
}
