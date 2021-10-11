package cas.thomas.Formulas;

import java.util.ArrayList;
import java.util.List;

public class ConjunctiveConstraint extends Constraint {

    public ConjunctiveConstraint(int numberOfVariables, int[] variables) {
        super(numberOfVariables, variables);
    }

    public ConjunctiveConstraint(int numberOfVariables) {
        super(numberOfVariables);
    }

    public ConjunctiveConstraint(int numberOfVariables, int nonZeroCounter) {
        super(numberOfVariables, nonZeroCounter);
    }

    @Override
    public Constraint condition(int variable) {
        int absoluteVariable = Math.abs(variable);

        assert (absoluteVariable < variables.length);

        int compareValue = compareValues(this.variables[absoluteVariable], variable);

        if (compareValue == -1) {

            return new EmptyConstraint(this.variables.length);

        } else if (compareValue == 1) {

            if (this.nonZeroCounter == 1) {
                return new SatisfiedConstraint(this.variables.length);
            }

            ConjunctiveConstraint clause = new ConjunctiveConstraint(this.variables.length,
                    this.nonZeroCounter - 1);

            clause.setVariables(this.variables);

            clause.variables[absoluteVariable] = 0;

            return clause;
        }

        DisjunctiveConstraint copiedClause = new DisjunctiveConstraint(this.variables.length, this.nonZeroCounter);
        copiedClause.setVariables(this.variables);

        return copiedClause;
    }

    @Override
    public boolean needsUnitResolution() {
        return false;
    }

    @Override
    public List<Integer> findUnitClauseVariable() {
        return new ArrayList<>();
    }
}
