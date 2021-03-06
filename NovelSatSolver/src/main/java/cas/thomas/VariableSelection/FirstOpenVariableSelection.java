package cas.thomas.VariableSelection;


public class FirstOpenVariableSelection implements VariableSelectionStrategy {

    @Override
    public int getNextVariable(int[] variables, double[] variableOccurences, boolean conflictLastRound,
                               int lastLiteral) {
        for (int i = 1; i < variables.length; i++) {
            if (variables[i] == 0) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void addUnassignedVariable(int variable) {

    }

    @Override
    public void recreatePriorityQueue(int[] variables, double[]variableOccurences) {

    }

    @Override
    public void heapify(int variable) {

    }
}
