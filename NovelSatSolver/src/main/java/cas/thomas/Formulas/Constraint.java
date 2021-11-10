package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;

import java.util.List;

public abstract class Constraint {

    protected int[] literals;
    protected boolean hasConflict;
    protected int conflictLiteral;

    public Constraint(int[] literals) {
        this.literals = literals;
        this.hasConflict = false;
        conflictLiteral = 0;
    }


    public abstract boolean propagate(int propagatedLiteral, int[] variableAssignments, List<Integer> unitLiterals,
                                      List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched,
                                      Constraint[] reasonClauses);

    public abstract SolutionCheckerConstraint getSolutionCheckerConstraint();

    public boolean resetConflictState() {
        boolean conflictState = hasConflict;
        hasConflict = false;
        conflictLiteral = 0;
        return conflictState;
    }

    public int[] getLiterals() {
        return literals;
    }


}
