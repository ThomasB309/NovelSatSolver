package cas.thomas.utils;

public class Pair<X,Y> {

    private X firstPairPart;
    private Y secondPairPart;

    public Pair(X firstPairPart, Y secondPairPart) {
        this.firstPairPart = firstPairPart;
        this.secondPairPart = secondPairPart;
    }


    public X getFirstPairPart() {
        return firstPairPart;
    }

    public Y getSecondPairPart() {
        return secondPairPart;
    }
}
