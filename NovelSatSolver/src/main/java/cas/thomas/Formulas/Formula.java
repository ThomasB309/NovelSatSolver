package cas.thomas.Formulas;

import java.text.Normalizer;

public class Formula {

    private Clause[] clauses;
    private boolean containsEmptyClause;
    private boolean isEmptyClause;
    private int numberOfClauses;
    private int numberOfVariables;

    public Formula(Clause[] clauses, int numberOfClauses, int numberOfVariables) {
        this.clauses = clauses;
        this.containsEmptyClause = false;
        this.isEmptyClause = false;
        this.numberOfClauses = numberOfClauses;
        this.numberOfVariables = numberOfVariables;
    }

    public Formula() {
        this.containsEmptyClause = false;
        this.isEmptyClause = false;
    }


    public Formula condition(int variable) {
        Clause[] conditionedClauses = new Clause[this.clauses.length];
        Formula conditionedFormula = new Formula();

        int fullfilledCounter = 0;

        for (int i = 0; i < this.clauses.length; i++) {

            Clause clause = clauses[i].condition(variable);

            conditionedClauses[i] = clause;

            if (clause.toString().equals("x1")) {
                System.out.println("log");
            }

            if (clause.isEmpty()) {
                conditionedFormula.setContainsEmptyClause();
            }

            if (clause.isFullfilled()) {
                fullfilledCounter++;
            }

        }

        if (fullfilledCounter == this.clauses.length) {
            conditionedFormula.setIsEmptyClause();
        }


        conditionedFormula.setClauses(conditionedClauses);

        return conditionedFormula;
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
}
