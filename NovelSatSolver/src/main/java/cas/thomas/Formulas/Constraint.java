package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.utils.IntegerArrayQueue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Constraint {

    protected int[] literals;
    protected boolean hasConflict;
    protected int conflictLiteral;
    private boolean obsolete;

    public Constraint(int[] literals) {
        this.literals = literals;
        this.hasConflict = false;
        conflictLiteral = 0;
        obsolete = false;
    }


    public abstract boolean propagate(int propagatedLiteral, int[] variableAssignments, IntegerArrayQueue unitLiterals,
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

    public int getLBDScore(int[] variableDecisionLevels) {
        Set<Integer> distinctDecisionLevels = new HashSet<>();

        for (int i = 0; i < literals.length; i++) {
            distinctDecisionLevels.add(variableDecisionLevels[Math.abs(literals[i])]);
        }

        return distinctDecisionLevels.size();
    }

    public void setObsolete() {
        obsolete = true;
    }

    public boolean isObsolete() {
        return obsolete;
    }


}
