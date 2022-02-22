package cas.thomas.utils;

import java.util.Arrays;

public class IntegerStack {

    private int stackPointer = 0;
    private static final int INITIAL_CAPACITY = 10;
    private int[] internalArray;
    private int stackPointerWithoutPop = 0;

    public IntegerStack() {
        internalArray = new int[INITIAL_CAPACITY];
    }

    public IntegerStack(int initialCapacity) {
        internalArray = new int[initialCapacity];
    }

    private void checkArrayLength() {
        if (stackPointer == internalArray.length) {
            internalArray = Arrays.copyOf(internalArray, stackPointer * 2);
        }
    }

    public void push(int i){
        checkArrayLength();
        internalArray[stackPointer] = i;
        stackPointer++;
    }

    public int pop() {
        if (stackPointer == 0) {
            throw new ArrayIndexOutOfBoundsException();
        }

        stackPointer--;
        int element = internalArray[stackPointer];
        internalArray[stackPointer] = 0;

        return element;
    }

    public boolean hasNext() {
        return stackPointer != 0;
    }

    public int peekFirst() {
        if (stackPointer == 0) {
            return 0;
        }

        return internalArray[stackPointer - 1];
    }

    public void prepareIterationWithoutPop() {
        stackPointerWithoutPop = stackPointer;
    }

    public boolean hasNextWithoutPop() {
        return stackPointerWithoutPop != 0;
    }

    public int peekNextWithoutPop() {
        if (stackPointerWithoutPop == 0) {
            return 0;
        }

        stackPointerWithoutPop--;
        return internalArray[stackPointerWithoutPop];
    }

    public String toString(){
        return Arrays.toString(internalArray);
    }

    public int[] getInternalArray() {
        return Arrays.copyOf(internalArray, stackPointer);
    }

    public int size() {
        return stackPointer;
    }
}
