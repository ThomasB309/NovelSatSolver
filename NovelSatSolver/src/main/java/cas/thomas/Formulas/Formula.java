package cas.thomas.Formulas;

import cas.thomas.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Formula {

    protected Constraint[] constraints;
    protected boolean containsEmptyClause;
    protected boolean isEmptyClause;
    private int numberOfClauses;
    private int numberOfVariables;
    private List<Integer> listOfUnitVariables;

    public Formula(Constraint[] constraints, int numberOfClauses, int numberOfVariables, List<Integer> listOfUnitVariables) {
        this.constraints = constraints;
        this.containsEmptyClause = false;
        this.isEmptyClause = false;
        this.numberOfClauses = numberOfClauses;
        this.numberOfVariables = numberOfVariables;
        this.listOfUnitVariables = listOfUnitVariables;
    }

    public Formula(Formula formula) {
        this.containsEmptyClause = formula.containsEmptyClause;
        this.isEmptyClause = formula.isEmptyClause;
        this.numberOfClauses = formula.numberOfClauses;
        this.numberOfVariables = formula.numberOfClauses;
        this.listOfUnitVariables = new ArrayList<>();
    }

    public Formula copy(){
        Formula formula = copyClause(this);
        Constraint[] constraints = Arrays.copyOf(this.constraints, this.constraints.length);
        List<Integer> setOfUnitVariables = new ArrayList<>(this.listOfUnitVariables);

        formula.constraints = constraints;
        formula.listOfUnitVariables = setOfUnitVariables;
        formula.numberOfVariables = this.numberOfVariables;
        formula.numberOfClauses = this.numberOfClauses;

        return formula;

    }


    public Formula condition(int[] variables) {
        Formula formula = null;

        if (variables.length > 0) {
            formula = this.condition(variables[0]);

            for (int i = 1; i < variables.length; i++) {
                formula = formula.condition(variables[i]);
            }
        }

        return formula;
    }


    public Formula condition(int variable) {
        Constraint[] conditionedConstraints = new Constraint[this.constraints.length];
        Formula conditionedFormula = copyClause(this);

        conditionedFormula.setListOfUnitVariables(this.listOfUnitVariables);

        int fullfilledCounter = 0;

        for (int i = 0; i < this.constraints.length; i++) {

            Constraint constraint = constraints[i].condition(variable);

            if (constraint.needsUnitResolution()) {
                conditionedFormula.listOfUnitVariables.addAll(constraint.findUnitClauseVariable());
            }

            conditionedConstraints[i] = constraint;

            if (constraint.isEmpty()) {
                conditionedFormula.setContainsEmptyClause();
            }

            if (constraint.isSatisfied()) {
                fullfilledCounter++;
            }

        }

        if (fullfilledCounter == this.constraints.length) {
            conditionedFormula.setIsEmptyClause();
        }


        conditionedFormula.setClauses(conditionedConstraints);

        return conditionedFormula;
    }

    public Pair<Set<Integer>, Formula> unitResolution() {
        Formula newFormula = this.unitVariableConditioning();
        Set<Integer> unitVariables = new HashSet<>(listOfUnitVariables);

        do {
            unitVariables.addAll(newFormula.listOfUnitVariables);
            newFormula = newFormula.unitVariableConditioning();
        } while (newFormula.listOfUnitVariables.size() > 0);

        return new Pair<>(unitVariables, newFormula);

    }

    private Formula unitVariableConditioning() {

        if (listOfUnitVariables.size() > 0) {
            Formula newFormula = condition(listOfUnitVariables.get(0));

            for (int i = 1; i < listOfUnitVariables.size(); i++) {
                newFormula = newFormula.condition(listOfUnitVariables.get(i));
            }

            return newFormula;
        } else {
            return this.copy();
        }
    }

    public boolean containsEmptyClause() {
        return this.containsEmptyClause;
    }

    public boolean isEmptyClause() {
        return this.isEmptyClause;
    }

    public void setContainsEmptyClause() {
        this.containsEmptyClause = true;
    }

    public void setIsEmptyClause() {
        this.isEmptyClause = true;
    }

    public void setClauses(Constraint[] constraints) {
        this.constraints = constraints;
    }

    public int getNumberOfClauses() {
        return numberOfClauses;
    }

    public int getNumberOfVariables() {
        return numberOfVariables;
    }

    public int getFirstLiteral() {
        for (int i = 0; i < constraints.length; i++) {
            for (int a = 0; a < constraints[i].variables.length; a++) {
                if (constraints[i].variables[a] != 0) {
                    return constraints[i].getVariableFromClauseArrayIndex(a) * constraints[i].variables[a];
                }
            }
        }

        return 0;
    }

    public String toString() {
        String output = "";

        for (int i = 0; i < constraints.length - 1; i++) {
            output += "(" + constraints[i].toString() + ")" + " Ʌ ";
        }

        output += "(" + constraints[constraints.length - 1].toString() + ")";

        return output;
    }

    private void setListOfUnitVariables(List<Integer> listOfUnitVariables) {
        this.listOfUnitVariables = new ArrayList<>(listOfUnitVariables.size());
    }

    protected abstract void checkIfIsEmptyClause(int fulfilledCounter);

    protected abstract Formula copyClause(Formula formula);
}
