package cas.thomas.SolutionChecker;

import java.util.List;

public class SolutionCheckerConjunctiveFormula extends SolutionCheckerFormula {

    public SolutionCheckerConjunctiveFormula(SolutionCheckerConstraint[] constraints) {
        super(constraints);
    }

    @Override
    public boolean isTrue(List<Integer> variables) {
        for (int i = 0; i < constraints.length; i++) {
            if (!constraints[i].isTrue(variables)) {
                return false;
            }
        }

        return true;
    }


}
