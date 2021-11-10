package cas.thomas.Formulas;

import cas.thomas.SolutionChecker.SolutionCheckerAMOConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerConstraint;

import java.util.List;

public class AMOConstraint extends Constraint {


    public AMOConstraint(int[] literals, List<Constraint>[] positivelyWatchedList,
                         List<Constraint>[] negativelyWatchedList) {
        super(literals);

        this.literals = literals;

        for (int i = 0; i < literals.length; i++) {
            int currentLiteral = literals[i];
            int currentLiteralAbsoluteValue = Math.abs(currentLiteral);

            if (currentLiteral < 0) {
                negativelyWatchedList[currentLiteralAbsoluteValue].add(this);
            } else {
                positivelyWatchedList[currentLiteralAbsoluteValue].add(this);
            }
        }
    }

    @Override
    public boolean propagate(int propagatedLiteral, int[] variableAssignments, List<Integer> unitLiterals,
                             List<Constraint>[] positivelyWatched, List<Constraint>[] negativelyWatched,
                             Constraint[] reasonClauses) {

        for (int i = 0; i < literals.length; i++) {
            int currentLiteral = literals[i];

            if (propagatedLiteral == currentLiteral) {
                for (int a = 0; a < literals.length; a++) {
                    if (a != i) {
                        unitLiterals.add(-literals[a]);
                    }
                }

                return true;
            }
        }

        return false;
    }


    @Override
    public SolutionCheckerConstraint getSolutionCheckerConstraint() {
        return new SolutionCheckerAMOConstraint(this.literals);
    }
}
