package cas.thomas.Formulas;

import cas.thomas.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Formula {

    private Clause[] clauses;
    private boolean containsEmptyClause;
    private boolean isEmptyClause;
    private int numberOfClauses;
    private int numberOfVariables;
    private List<Integer> listOfUnitVariables;

    public Formula(Clause[] clauses, int numberOfClauses, int numberOfVariables, List<Integer> listOfUnitVariables) {
        this.clauses = clauses;
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
        Formula formula = new Formula(this);
        Clause[] clauses = Arrays.copyOf(this.clauses, this.clauses.length);
        List<Integer> setOfUnitVariables = new ArrayList<>(this.listOfUnitVariables);

        formula.clauses = clauses;
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
        Clause[] conditionedClauses = new Clause[this.clauses.length];
        Formula conditionedFormula = new Formula(this);

        conditionedFormula.setListOfUnitVariables(this.listOfUnitVariables);

        int fullfilledCounter = 0;

        for (int i = 0; i < this.clauses.length; i++) {

            Clause clause = clauses[i].condition(variable);

            if (clause.isUnitClause()) {
                conditionedFormula.listOfUnitVariables.add(clause.findUnitClauseVariable());
            }

            conditionedClauses[i] = clause;

            if (clause.isEmpty()) {
                conditionedFormula.setContainsEmptyClause();
            }

            if (clause.isSatisfied()) {
                fullfilledCounter++;
            }

        }

        if (fullfilledCounter == this.clauses.length) {
            conditionedFormula.setIsEmptyClause();
        }


        conditionedFormula.setClauses(conditionedClauses);

        return conditionedFormula;
    }

    public Pair<Set<Integer>, Formula> unitResolution() {
        Formula newFormula = this.unitVariableConditioning();
        Set<Integer> unitVariables = new HashSet<>(listOfUnitVariables);

        do {
            newFormula = newFormula.unitVariableConditioning();
            unitVariables.addAll(newFormula.listOfUnitVariables);
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

    public void setClauses(Clause[] clauses) {
        this.clauses = clauses;
    }

    public int getNumberOfClauses() {
        return numberOfClauses;
    }

    public int getNumberOfVariables() {
        return numberOfVariables;
    }

    private void changeListOfUnitClauseVariables(Clause clause, Formula formula) {
        return;
    }

    private Clause changeListOfUnitClauseVariables(UnitClause clause, Formula formula) {
        formula.listOfUnitVariables.add(clause.getUnitClauseVariable());

        return clause;
    }

    public int getFirstLiteral() {
        for (int i = 0; i < clauses.length; i++) {
            for (int a = 0; a < clauses[i].variables.length; a++) {
                if (clauses[i].variables[a] != 0) {
                    return a * clauses[i].variables[a];
                }
            }
        }

        return 0;
    }

    private void setListOfUnitVariables(List<Integer> listOfUnitVariables) {
        this.listOfUnitVariables = new ArrayList<>(listOfUnitVariables.size());
    }
}
