package cas.thomas.Evaluation;

public class Statistics {

    private long milliseconds;
    private long decisions;
    private long propagations;
    private long conflicts;
    private double millisecondsAverage;
    private double decisionsAverage;
    private double propagationsAverage;
    private double conflictsAverage;
    private int timeoutCounter;
    private int solvedCounter;
    private int addCounter = 0;
    private String name;

    public void setMilliseconds(long milliseconds) {
        this.milliseconds = milliseconds;
        this.millisecondsAverage = milliseconds;
    }

    public void setDecisions(long decisions) {
        this.decisions = decisions;
        this.decisionsAverage = decisions;
    }

    public void setPropagations(long propagations) {
        this.propagations = propagations;
        this.propagationsAverage = propagations;
    }

    public void setConflicts(long conflicts) {
        this.conflicts = conflicts;
        this.conflictsAverage = conflicts;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimeoutCounter(int timeoutCounter){
        this.timeoutCounter = timeoutCounter;
    }

    public int getTimeoutCounter() {
        return timeoutCounter;
    }

    public void setSolvedCounter(int solvedCounter) {
        this.solvedCounter = solvedCounter;
    }

    public int getSolvedCounter() {
        return solvedCounter;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Statistics [milliseconds=" + milliseconds + ", decisions=" + decisions + ", propagations="
                + propagations + ", conflicts=" + conflicts + "]";
    }

    public void add(Statistics statistics) {
        milliseconds += statistics.milliseconds;
        decisions += statistics.decisions;
        propagations += statistics.propagations;
        conflicts += statistics.conflicts;
        solvedCounter += statistics.solvedCounter;
        timeoutCounter += statistics.timeoutCounter;
        addCounter++;

        millisecondsAverage = (1.0 * milliseconds) / addCounter;
        decisionsAverage = (1.0 * decisions) / addCounter;
        propagationsAverage = (1.0 * propagations) / addCounter;
        conflictsAverage = (1.0 * conflicts) / addCounter;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public long getDecisions() {
        return decisions;
    }

    public long getPropagations() {
        return propagations;
    }

    public long getConflicts() {
        return conflicts;
    }

    public double getMillisecondsAverage() {
        return millisecondsAverage;
    }

    public double getDecisionsAverage() {
        return decisionsAverage;
    }

    public double getPropagationsAverage() {
        return propagationsAverage;
    }

    public double getConflictsAverage() {
        return conflictsAverage;
    }
}
