package cas.thomas.utils;

import java.util.Arrays;

public class IntegerArrayQueue {

    private int headPointer = 0;
    private int tailPointer = 0;
    private static final int INITIAL_CAPACITY = 10;
    private int[] internalArray;
    private int elementCounter = 0;

    public IntegerArrayQueue() {
        internalArray = new int[INITIAL_CAPACITY];
    }

    public IntegerArrayQueue(int initialCapacity) {
        internalArray = new int[initialCapacity];
    }

    public void checkArrayLength() {
        if (elementCounter == internalArray.length) {
            internalArray = Arrays.copyOf(internalArray, elementCounter * 2);
        }
    }

    public void offer(int i){

        checkArrayLength();

        if (elementCounter == internalArray.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        internalArray[tailPointer] = i;
        tailPointer++;
        elementCounter++;

        if (tailPointer == internalArray.length) {
            tailPointer = 0;
        }


    }

    public int poll() {
        if (elementCounter == 0) {
            throw new ArrayIndexOutOfBoundsException();
        }

        int element = internalArray[headPointer];
        internalArray[headPointer] = 0;
        headPointer++;
        elementCounter--;

        if (headPointer == internalArray.length) {
            headPointer = 0;
        }

        return element;
    }

    public int[] getInternalArray() {
        if (tailPointer == headPointer) {
            return internalArray;
        } else {
            return Arrays.copyOf(internalArray, tailPointer);
        }
    }

    public int size() {
        return elementCounter;
    }
}
