package cas.thomas.SolutionChecker;

import java.util.List;

public class SolutionCheckerDisjunctiveConstraint extends SolutionCheckerConstraint {


    public SolutionCheckerDisjunctiveConstraint(int[] literals) {
        super(literals);
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

}
