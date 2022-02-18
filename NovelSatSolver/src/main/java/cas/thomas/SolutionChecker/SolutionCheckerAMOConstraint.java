package cas.thomas.SolutionChecker;

import cas.thomas.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SolutionCheckerAMOConstraint extends SolutionCheckerConstraint {

    private int[] literals;

    public SolutionCheckerAMOConstraint(int[] literals) {
        super();
        this.literals = Arrays.copyOf(literals, literals.length);
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

    @Override
    public String toDimacsString() {
        String dimacsString = "AMO ";

        for (int i = 0; i < literals.length; i++) {
            dimacsString += literals[i] + " ";
        }

        dimacsString += "0\n";

        return dimacsString;
    }

    @Override
    public Pair<Integer, Integer> toDimacsCNFString(StringBuilder cnfString, int maxVariable) {
        List<SolutionCheckerDisjunctiveConstraint> clauses = new ArrayList<>();
        int nextHelperVariable = maxVariable + 1;
        int constraintCounter = 0;

        if (literals.length <= 1) {
            // at most of one literal is a redundant constraint and can be ignored
            return new Pair<>(maxVariable, constraintCounter);
        }

        if (literals.length < 6) {
            // pairwise encoding is better
            for (int i = 0; i < literals.length; i++) {
                for (int j = i + 1; j < literals.length; j++) {
                    cnfString.append(new SolutionCheckerDisjunctiveConstraint(new int[] { -literals[i],
                            -literals[j] }).toDimacsString());
                    constraintCounter++;
                }
            }
            return new Pair<>(maxVariable, constraintCounter);
        }

        int prevHelper = 0;
        for (int i = 0; i + 1 < literals.length; i++) {
            int lit = literals[i];
            int nextLit = literals[i + 1];
            int helper = nextHelperVariable++;
            cnfString.append(new SolutionCheckerDisjunctiveConstraint(new int[] { -lit, helper }).toDimacsString());
            cnfString.append(new SolutionCheckerDisjunctiveConstraint(new int[] { -helper, -nextLit }).toDimacsString());
            constraintCounter += 2;
            if (prevHelper != 0) {
                cnfString.append(new SolutionCheckerDisjunctiveConstraint(new int[] { -prevHelper, helper }).toDimacsString());
                constraintCounter++;
            }
            prevHelper = helper;
        }

        return new Pair<>(prevHelper, constraintCounter);
    }
}
