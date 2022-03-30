package cas.thomas.VariableSelection;

public interface VariableSelectionStrategy {

    int getNextVariable(int[] variables, double[] variableOccurences, boolean conflictLastRound, int lastLiteral);

    void addUnassignedVariable(int variable);
}
