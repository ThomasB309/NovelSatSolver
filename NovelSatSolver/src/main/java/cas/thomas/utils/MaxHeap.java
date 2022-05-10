package cas.thomas.utils;

public class MaxHeap {

    private int[] heapArray;
    private int[] variableToArrayPositionMapping;
    private double[] score;
    private int currentPosition;
    private static final double EPSILON = 0.000001d;


    public MaxHeap (int numberOfVariables, double[] score) {
        heapArray = new int[numberOfVariables];
        variableToArrayPositionMapping = new int[numberOfVariables];

        for (int i = 0; i < variableToArrayPositionMapping.length; i++) {
            variableToArrayPositionMapping[i] = -1;
        }

        this.score = score;
        currentPosition = 0;
    }

    private int parentNodePosition(int node) {
        return (node - 1) / 2;
    }

    private int leftChildNode(int parentNode) {
        return (2 * parentNode) + 1;
    }

    private int rightChildNode (int parentNode) {
        return (2 * parentNode) + 2;
    }

    private boolean isLeaf(int node) {
        if (node > (currentPosition / 2) && node <= currentPosition) {
            return true;
        }

        return false;
    }

    private void swapNodes(int firstNodePosition, int secondNodePosition) {
        int swapTemp = heapArray[firstNodePosition];
        heapArray[firstNodePosition] = heapArray[secondNodePosition];
        heapArray[secondNodePosition] = swapTemp;

        variableToArrayPositionMapping[heapArray[firstNodePosition]] = firstNodePosition;
        variableToArrayPositionMapping[heapArray[secondNodePosition]] = secondNodePosition;
    }

    public void insert(int variable) {

        int nextPosition = currentPosition;
        heapArray[nextPosition] = variable;
        variableToArrayPositionMapping[variable] = nextPosition;

        bubbleUp(variable, nextPosition);

        currentPosition++;
    }

    public int getMax() {

        int max = heapArray[0];

        variableToArrayPositionMapping[max] = -1;

        if (currentPosition == 0) {
            return max;
        }

        heapArray[0] = heapArray[currentPosition - 1];
        heapArray[currentPosition - 1] = 0;
        currentPosition--;

        boolean continueMovingDown = true;
        int parentPosition = 0;
        int parentVariable = heapArray[0];

        while (true) {
            int nextPosition = -1;

            if (isLeaf(parentPosition)) {
                return max;
            }

            int leftChildPosition = leftChildNode(parentPosition);
            int leftChildVariable = heapArray[leftChildPosition];

            if ((nextPosition = compareParentWithChild(parentPosition, parentVariable, leftChildVariable, leftChildPosition)) != -1) {
                parentPosition = nextPosition;
                parentVariable = heapArray[parentPosition];
                continue;
            }

            int rightChildPosition = rightChildNode(parentPosition);

            if (rightChildPosition >= heapArray.length) {
                return max;
            }


            int rightChildVariable = heapArray[rightChildPosition];

            if ((nextPosition = compareParentWithChild(parentPosition, parentVariable,
                    rightChildVariable, rightChildPosition)) != -1) {
                parentPosition = nextPosition;
                parentVariable = heapArray[parentPosition];
                continue;
            }

            return max;

        }

    }

    private int compareParentWithChild(int parentNode, int parentVariable, int childVariable, int childPosition) {
        if (compareDouble(score[parentVariable], score[childVariable])) {
            swapNodes(parentNode, childPosition);
            return childPosition;
        }

        return -1;
    }

    public void heapifyVariable(int variable) {
        int currentPosition = variableToArrayPositionMapping[variable];

        if (currentPosition == -1) {
            return;
        }

        bubbleUp(variable, currentPosition);


    }

    private void bubbleUp(int variable, int currentPosition) {
        int parentNodePosition = parentNodePosition(currentPosition);
        int parentVariable = heapArray[parentNodePosition];

        while (currentPosition != 0) {
            if (compareDouble(score[parentVariable], score[variable])) {
                swapNodes(parentNodePosition, currentPosition);
                currentPosition = parentNodePosition;
                parentNodePosition = parentNodePosition(currentPosition);
                parentVariable = heapArray[parentNodePosition];
            } else {
                break;
            }
        }
    }

    private boolean compareDouble(double first, double second) {
        double comparison = first - second;

        if (comparison < 0 && Math.abs(comparison) > EPSILON) {
            return true;
        }

        return false;
    }

    public int currentSize() {
        return currentPosition;
    }

    public boolean contains(int variable) {
        return variableToArrayPositionMapping[variable] != -1;
    }
}
