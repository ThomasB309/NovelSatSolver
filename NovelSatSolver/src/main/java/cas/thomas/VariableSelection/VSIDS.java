package cas.thomas.VariableSelection;


import java.util.Comparator;
import java.util.PriorityQueue;

public class VSIDS implements VariableSelectionStrategy {

    PriorityQueue<Integer> maxScoreVariables;

    @Override
    public int getNextVariable(int[] variables, double[] variableOccurences, boolean conflictLastRound,
                               int lastLiteral) {

        if (maxScoreVariables == null) {
            maxScoreVariables =
                    new PriorityQueue<>(Comparator.comparingDouble(a -> -variableOccurences[Math.abs(a)]));
            for (int i = 1; i < variables.length; i++) {
                maxScoreVariables.add(i);
            }
        }

        do {
            Integer nextVariable = maxScoreVariables.poll();

            if (nextVariable == null) {
                return -1;
            }

            if (variables[nextVariable] == 0) {
                return nextVariable;
            }
        } while (maxScoreVariables.size() > 0);

        return -1;
    }

    public void addUnassignedVariable(int variable) {
        if (maxScoreVariables != null) {
            maxScoreVariables.add(variable);
        }
    }
}
