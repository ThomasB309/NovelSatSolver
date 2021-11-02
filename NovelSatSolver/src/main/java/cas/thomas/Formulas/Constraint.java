package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;

import java.util.List;

public abstract class Constraint {

    protected int firstWatchedIndex;
    protected int secondWatchedIndex;
    protected int[] literals;

    public Constraint(int[] literals, List<Constraint>[] positivelyWatchedList,
                      List<Constraint>[] negativelyWatchedList) {
        this.literals = literals;
    }


    public abstract boolean propagate(int propagatedLiteral, int[] variableAssignments, List<Integer> unitLiterals,
                                      List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched);

    protected abstract int[] getWatchedLiterals();

    public abstract SolutionCheckerConstraint getSolutionCheckerConstraint();

}
