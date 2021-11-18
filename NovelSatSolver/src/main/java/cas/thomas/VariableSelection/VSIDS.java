package cas.thomas.VariableSelection;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public class VSIDS implements VariableSelectionStrategy {

    PriorityQueue<Integer> maxScoreVariables;
    List<Integer> usedLiterals;

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

        if (usedLiterals == null) {
            usedLiterals = new ArrayList<>(variables.length);
        }

        if (conflictLastRound && lastLiteral != 0) {
            for (Iterator<Integer> listIterator = usedLiterals.listIterator(); listIterator.hasNext();) {
                int nextVariable = listIterator.next();

                if (variables[nextVariable] == 0) {
                    maxScoreVariables.add(nextVariable);
                    listIterator.remove();
                }
            }
        }

        do {
            int nextVariable = maxScoreVariables.poll();
            usedLiterals.add(nextVariable);
            if (variables[nextVariable] == 0) {
                return nextVariable;
            }
        } while (maxScoreVariables.size() > 0);

        return -1;
    }
}
