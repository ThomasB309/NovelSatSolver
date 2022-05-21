package cas.thomas.Evaluation;

public class ConstraintStatistics {

    private int numberOfDNFconstraints;
    private int numberOfTerms;
    private int numberOfDNFLiterals;
    private int numberOfClauses;
    private int numberOfClauseLiterals;
    private int numberOfAMOConstraints;
    private int numberOfAMOLiterals;
    private int fileCounter;
    private int numberOfVariables;

    public double getAverageNumberOfVariables(){
        return (1.0 * numberOfVariables) / fileCounter;
    }

    public int getFileCounter() {
        return fileCounter;
    }

    public double getNumberOfDNFconstraints() {
        return (1.0 * numberOfDNFconstraints) / fileCounter;
    }

    public double getAverageNumberOfTerms() {
        return (1.0 * numberOfTerms) / numberOfDNFconstraints;
    }

    public double getAverageNumberOfLiteralsPerTerm() {
        return (1.0 * numberOfDNFLiterals) / numberOfTerms;
    }

    public double getNumberOfClauses() {
        return (1.0 * numberOfClauses) / fileCounter;
    }

    public double getAverageLiteralsPerClause() {
        return (1.0 * numberOfClauseLiterals) / numberOfClauses;
    }

    public double getNumberOfAMOConstraints() {
        return (1.0 * numberOfAMOConstraints) / fileCounter;
    }

    public double getAverageLiteralsPerAMOConstraints() {
        return (1.0 * numberOfAMOLiterals) / numberOfAMOConstraints;
    }

    public void increaseFileCounter() {
        fileCounter++;
    }

    public void addDNFConstraint(int numberOfTerms, int numberOfLiterals) {
        numberOfDNFconstraints++;
        this.numberOfTerms += numberOfTerms;
        numberOfDNFLiterals += numberOfLiterals;
    }

    public void addClause(int numberOfLiterals) {
        numberOfClauses++;
        numberOfClauseLiterals += numberOfLiterals;
    }

    public void addAMOConstraint(int numberOfLiterals) {
        numberOfAMOConstraints++;
        numberOfAMOLiterals += numberOfLiterals;
    }

    public void addNumberOfVariables(int numberOfVariables) {
        this.numberOfVariables += numberOfVariables;
    }
}
