package cas.thomas.Formulas;

import java.util.Arrays;

public class Variable {

    private int[] indecesInClauses;
    private int[] valueInClause;
    private int comparisonValue;

    public Variable(int numberOfClauses, int... valueInClause) {
        this.indecesInClauses = new int[numberOfClauses];
        this.valueInClause = valueInClause;
    }

    public int getIndexInClause(int formulaIndex) {
        return indecesInClauses[formulaIndex];
    }

    public void setIndexInClause(int formulaIndex, int index) {
        indecesInClauses[formulaIndex] = index;
    }

    public int getComparisonValue() {
        return this.getComparisonValue();
    }
}
