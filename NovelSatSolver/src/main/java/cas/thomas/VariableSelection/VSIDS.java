package cas.thomas.VariableSelection;

import cas.thomas.Formulas.Formula;
import java.util.Comparator;
import java.util.stream.IntStream;

public class VSIDS implements VariableSelectionStrategy {

    private int[] variableOccurences;
    private int[] variables;
    private int[] currentOrdering;
    private int conflictCounter = 0;

    @Override
    public int getNextVariable(Formula formula, boolean conflictLastRound) {

        if (conflictLastRound) {
            conflictCounter++;
        }

        if (variableOccurences == null) {
            variableOccurences = formula.getVariableOccurences();
        }

        if (variables == null) {
            variables = formula.getVariables();
        }

        if (currentOrdering == null) {
            currentOrdering =
                    IntStream.range(1, variableOccurences.length).boxed().sorted((a,b) -> Integer.compare(variableOccurences[a], variableOccurences[b]) * -1).mapToInt(a -> a.intValue()).toArray();
        }

        if (conflictCounter % 256 == 0 && conflictCounter > 0) {
            formula.halfVariableOccurenceCounter();
            currentOrdering =
                    IntStream.range(1, variableOccurences.length).boxed().sorted((a,b) -> Integer.compare(variableOccurences[a], variableOccurences[b]) * -1).mapToInt(a -> a.intValue()).toArray();
            conflictCounter = 0;
        }

        for (int i = 0; i < currentOrdering.length; i++) {
            int variable = currentOrdering[i];
            if (variables[variable] == 0) {
                return variable;
            }
        }

        return -1;
    }
}
