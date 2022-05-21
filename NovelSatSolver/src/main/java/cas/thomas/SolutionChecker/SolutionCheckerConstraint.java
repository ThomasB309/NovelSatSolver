package cas.thomas.SolutionChecker;

import cas.thomas.Evaluation.ConstraintStatistics;
import cas.thomas.utils.Pair;

import java.util.List;

public abstract class SolutionCheckerConstraint {


    public abstract boolean isTrue(int[] literals);

    public abstract String toDimacsString();

    public abstract Pair<Integer, Integer> toDimacsCNFString(StringBuilder cnfString, int maxVariables);

    public abstract void addStatistics(ConstraintStatistics constraintStatistics);
}
