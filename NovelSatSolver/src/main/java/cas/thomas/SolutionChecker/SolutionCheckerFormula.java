package cas.thomas.SolutionChecker;

import java.util.List;

public abstract class SolutionCheckerFormula {

    protected SolutionCheckerConstraint[] constraints;

    public SolutionCheckerFormula(SolutionCheckerConstraint[] constraints) {
        this.constraints = constraints;
    }

    public abstract boolean isTrue(List<Integer> variables);

}
