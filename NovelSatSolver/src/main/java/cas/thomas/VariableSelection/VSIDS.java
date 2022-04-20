package cas.thomas.VariableSelection;


import cas.thomas.utils.MaxHeap;

import java.util.Comparator;
import java.util.PriorityQueue;

public class VSIDS implements VariableSelectionStrategy {

    MaxHeap maxHeap;

    @Override
    public int getNextVariable(int[] variables, double[] variableOccurences, boolean conflictLastRound,
                               int lastLiteral) {

        if (maxHeap == null) {
            maxHeap =
                    new MaxHeap(variables.length, variableOccurences);
            for (int i = 1; i < variables.length; i++) {
                maxHeap.insert(i);
            }
        }

        do {
            int nextVariable = maxHeap.getMax();

            if (nextVariable == 0) {
                return -1;
            }

            if (variables[nextVariable] == 0) {
                return nextVariable;
            }
        } while (maxHeap.currentSize() > 0);

        return -1;
    }

    public void addUnassignedVariable(int variable) {
        if (maxHeap != null && !maxHeap.contains(variable)) {
            maxHeap.insert(variable);
        }
    }

    public void recreatePriorityQueue(int[] variables, double[] variableOccurences) {
        for (int i = 1; i < variables.length; i++) {
            maxHeap.heapifyVariable(i);
        }
    }

    public void heapify(int variable) {
        maxHeap.heapifyVariable(variable);
    }
}
