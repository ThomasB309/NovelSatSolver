package cas.thomas.BenchmarkGeneration;

import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.SolutionChecker.SolutionCheckerConjunctiveFormula;
import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerFormula;
import cas.thomas.SolverAlgorithms.IncrementalCdclSolver;
import cas.thomas.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

public class PhaseTransition {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Choose a phase transition!");
            System.exit(-1);
        }

        switch (args[0]) {
            case "A":
        }

    }

    public static SolutionCheckerFormula random(int vars, int cls, int amo, int dnf, long seed) throws TimeoutException, UnitLiteralConflictException {
        Random rnd = new Random(seed);
        Integer[] plantedSolution = new Integer[vars + 1];
        plantedSolution[0] = 0;
        for (int i = 1; i <= vars; i++) {
            plantedSolution[i] = rnd.nextBoolean() ? i : -i;
        }

        List<Integer> solution = Arrays.asList(plantedSolution);

        List<SolutionCheckerConstraint> constraints = new ArrayList<>();

        createPhaseTransitionPlotForDNFConstraintsWithConstantTermCount(50, 3, 7, 1);

        return new SolutionCheckerConjunctiveFormula(constraints.toArray(SolutionCheckerConstraint[]::new), vars);
    }

    public static void createPhaseTransitionPlotForDNFConstraintsWithConstantTermCount(int vars, int terms,
                                                                                        int termLength,
                                                                                        int stepSize) throws TimeoutException {
        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);

        for (int i = 2; i <= termLength; i++) {
            solvingTimeList.add(calculatePhaseTransitionDNF(vars, rnd, terms, i, stepSize));
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        int counter = 2;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for DNF constraints with " + terms + " " +
                    "terms of length " + counter);

            counter++;
        }
    }

    public static List<Pair<Double, Double>> calculatePhaseTransitionDNF(int vars, Random rnd, int terms,
                                                                         int termLength, int stepSize) throws TimeoutException {
        List<Pair<Double, Double>> points = new LinkedList<>();
        List<Pair<Double, Double>> solvingTimePoints = new LinkedList<>();
        double xTick = 0;
        double yTick = -1;
        double xTickSolvingTime = 0;
        double yTickSolvingTime = 0;
        int dnfCounter = 1;
        while (yTick != 0) {
            int allCounter = 0;
            int trueCounter = 0;
            long time = 0;
            for (int i = 0; i < 1000; i++) {
                IncrementalCdclSolver solver = new IncrementalCdclSolver();
                for (int j = 0; j < dnfCounter; j++) {
                    solver.addDnf(getRandomDNFTerms(vars, rnd, terms, termLength));
                }

                //System.out.println(i);
                long startTime = System.nanoTime();
                if (solver.solve(new int[]{})) {
                    trueCounter++;
                }
                long endTime = System.nanoTime();

                time += (endTime - startTime);

                allCounter++;
            }


            xTick = (double) dnfCounter / vars;
            yTick = (double) trueCounter / allCounter;
            xTickSolvingTime = xTick;
            yTickSolvingTime = Math.max(yTickSolvingTime, TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS));

            addPoints(points, solvingTimePoints, xTick, yTick, xTickSolvingTime, time);

            System.out.println(xTick + " " + yTick);

            dnfCounter += stepSize;

        }

        createPlotCSV(points, "Phase transition of DNF constraints with " + terms + " terms of length " + termLength);

        return solvingTimePoints;
    }

    private static void createTikzPlotNormalizedByY(int maxValue, List<Pair<Double, Double>> points,
                                                    String name) {

        System.out.println(name);
        for (Pair<Double, Double> point : points) {
            System.out.println(point.getFirstPairPart() + " " + point.getSecondPairPart() / (maxValue));
        }
    }

    private static int[][] getRandomDNFTerms(int vars, Random rnd, int terms, int literalsPerTerm) {
        int[][] clause = new int[terms][literalsPerTerm];
        boolean containsDuplicate;
        do {
            containsDuplicate = false;
            for (int i = 0; i < clause.length; i++) {
                HashSet<Integer> usedVariables = new HashSet<>();
                for (int j = 0; j < clause[i].length; j++) {
                    int var = rnd.nextInt(vars) + 1;

                    if (usedVariables.contains(var)) {
                        containsDuplicate = true;
                    }
                    usedVariables.add(var);
                    clause[i][j] = rnd.nextBoolean() ? var : -var;
                }
            }
        } while (containsDuplicate);

        return clause;
    }

    private static void addPoints(List<Pair<Double, Double>> points, List<Pair<Double, Double>> solvingTimePoints, double xTick, double yTick, double xTickSolvingTime, long time) {
        points.add(new Pair<>(xTick, yTick));
        solvingTimePoints.add(new Pair<>(xTickSolvingTime, (double) TimeUnit.MILLISECONDS.convert(time,
                TimeUnit.NANOSECONDS)));
    }

    private static void createPlotCSV(List<Pair<Double, Double>> points, String name) {
        System.out.println(name);
        for (Pair<Double, Double> point : points) {
            System.out.println(point.getFirstPairPart() + " " + point.getSecondPairPart());
        }
    }

    public static void createPhaseTransitionPlotForAMOConstraintsWithAConstantNumberOfClausesAndDNFConstraints(int vars,
                                                                                                          int amoLength, int amoStepSize
            , int iterations, int clauseLength, int clauseStepSize, int termCount, int termLength, int dnfStepSize)
                                                                                                                        throws TimeoutException {


        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);

        int cls = 0;
        int dnf = 0;
        for (int i = 0; i < iterations; i++) {
            solvingTimeList.add(calculatePhaseTransitionAMODNFClS(vars, amoLength, amoStepSize, cls, dnf,
                    clauseLength, termCount, termLength, rnd));
            cls += clauseStepSize;
            dnf += dnfStepSize;
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        int counter = 1;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for DNF constraints with " + counter + " " +
                    "terms of length " + termLength);

            counter++;
        }

    }

    private static List<Pair<Double,Double>> calculatePhaseTransitionAMODNFClS(int vars, int amoLength, int amoStepSize
            , int cls, int dnf, int clauseLength, int termCount, int termLength,
             Random rnd                                                                  ) throws TimeoutException {

        List<Pair<Double, Double>> points = new LinkedList<>();
        List<Pair<Double, Double>> solvingTimePoints = new LinkedList<>();
        double xTick = 0;
        double yTick = -1;
        double xTickSolvingTime = 0;
        double yTickSolvingTime = 0;
        int amoCounter = 1;
        while (yTick != 0) {
            int allCounter = 0;
            int trueCounter = 0;
            long time = 0;
            for (int i = 0; i < 1000; i++) {
                IncrementalCdclSolver solver = instantiateSolver(vars, cls, clauseLength, dnf, termCount, termLength,
                        amoLength, amoCounter, rnd);

                long startTime = System.nanoTime();

                if (solver.solve(new int[]{})) {
                    trueCounter++;
                }
                long endTime = System.nanoTime();

                time += (endTime - startTime);

                allCounter++;
            }

            xTick = (double) amoCounter / vars;
            yTick = (double) trueCounter / allCounter;

            xTickSolvingTime = xTick;
            yTickSolvingTime = Math.max(yTickSolvingTime, TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS));

            addPoints(points, solvingTimePoints, xTick, yTick, xTickSolvingTime, time);

            //System.out.println(xTick + " " + yTick);

            amoCounter += amoStepSize;

        }

        createPlotCSV(points, "Phase transition of AMO constraints with " + amoLength + " literals and " + dnf +
                " DNF constraints with " + termCount + " terms of length " + termLength + " and " + cls + " clauses " +
                "of" +
                " length " + clauseLength);

        return solvingTimePoints;
    }

    public static void createPlotsForAMOPhaseTransitionWithAConstantNumberOfDNFConstraints(int vars, int dnf,
                                                                                            int terms, int termLength, int amoLength, int amoStepSize, int dnfStepSize) throws TimeoutException, UnitLiteralConflictException {
        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);

        for (int i = 0; i < dnf; i += dnfStepSize) {
            solvingTimeList.add(calculatePhaseTransitionAMODNF(vars, i, terms, termLength, rnd, amoLength, amoStepSize));
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        int counter = 1;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for DNF constraints with " + counter + " " +
                    "terms of length " + termLength);

            counter++;
        }
    }

    public static List<Pair<Double, Double>> calculatePhaseTransitionAMODNF(int vars, int dnf, int terms, int termLength,
                                                                            Random rnd,
                                                                            int amoLength, int stepSize) throws TimeoutException,
            UnitLiteralConflictException {

        List<Pair<Double, Double>> points = new LinkedList<>();
        List<Pair<Double, Double>> solvingTimePoints = new LinkedList<>();
        double xTick = 0;
        double yTick = -1;
        double xTickSolvingTime = 0;
        double yTickSolvingTime = 0;
        int amoCounter = 1;
        while (yTick != 0) {
            int allCounter = 0;
            int trueCounter = 0;
            long time = 0;
            for (int i = 0; i < 1000; i++) {
                IncrementalCdclSolver solver = instantiateSolver(vars, dnf, terms, termLength, rnd, amoLength, amoCounter);

                long startTime = System.nanoTime();
                if (solver.solve(new int[]{})) {
                    trueCounter++;
                }
                long endTime = System.nanoTime();

                time += (endTime - startTime);

                allCounter++;
            }

            xTick = (double) amoCounter / vars;
            yTick = (double) trueCounter / allCounter;

            xTickSolvingTime = xTick;
            yTickSolvingTime = Math.max(yTickSolvingTime, TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS));

            addPoints(points, solvingTimePoints, xTick, yTick, xTickSolvingTime, time);

            amoCounter += stepSize;

        }

        createPlotCSV(points, "Phase transition of AMO constraints with " + amoLength + " literals and " + dnf +
                " DNF constraints with " + terms + " terms of length " + termLength);

        return solvingTimePoints;
    }

    private static IncrementalCdclSolver instantiateSolver(int vars, int dnf, int terms, int termLength, Random rnd,
                                                           int amoLength,
                                                           int amoCounter) {
        IncrementalCdclSolver solver = new IncrementalCdclSolver();

        for (int j = 0; j < dnf; j++) {
            solver.addDnf(getRandomDNFTerms(vars, rnd, terms, termLength));
        }

        for (int j = 0; j < amoCounter; j++) {
            solver.addAtMostOne(generateRandomLiterals(vars, rnd, amoLength));
        }
        return solver;
    }

    private static IncrementalCdclSolver instantiateSolver(int vars, int cls, int clauseLength, int dnf, int terms,
                                                           int termLength, int amoLength, int amoCounter, Random rnd) {
        IncrementalCdclSolver solver = new IncrementalCdclSolver();

        for (int i = 0; i < cls; i++) {
            solver.addClause(generateRandomLiterals(vars, rnd, clauseLength));
        }

        for (int j = 0; j < dnf; j++) {
            solver.addDnf(getRandomDNFTerms(vars, rnd, terms, termLength));
        }

        for (int j = 0; j < amoCounter; j++) {
            solver.addAtMostOne(generateRandomLiterals(vars, rnd, amoLength));
        }

        return solver;
    }

    private static int[] generateRandomLiterals(int vars, Random rnd, int i2) {
        int[] clause = new int[i2];
        boolean containsDuplicate;
        do {
            containsDuplicate = false;
            HashSet<Integer> usedVariables = new HashSet<>();
            for (int i = 0; i < clause.length; i++) {
                int var = rnd.nextInt(vars) + 1;

                if (usedVariables.contains(var)) {
                    containsDuplicate = true;
                }
                usedVariables.add(var);
                clause[i] = rnd.nextBoolean() ? var : -var;
            }
        } while (containsDuplicate);

        return clause;
    }

    public static void createPhaseTransitionPlotForDNFConstraintsWithConstantTermLength(int vars, int terms,
                                                                                         int termLength, int stepSize) throws TimeoutException {
        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);

        for (int i = 1; i <= terms; i++) {
            solvingTimeList.add(calculatePhaseTransitionDNF(vars, rnd, i, termLength, stepSize));
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        int counter = 1;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for DNF constraints with " + counter + " " +
                    "terms of length " + termLength);

            counter++;
        }
    }

    public static void createPhaseTransitionPlotForAMOConstraints(int vars, int amoLength, int stepSize) throws TimeoutException,
            UnitLiteralConflictException {

        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);

        for (int i = 2; i <= amoLength; i++) {
            solvingTimeList.add(calculatePhaseTransitionAMO(vars, rnd, i, stepSize));
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        int counter = 2;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for AMO constraints with " + counter + " " +
                    "literals");

            counter++;
        }

    }

    private static List<Pair<Double, Double>> calculatePhaseTransitionAMO(int vars,
                                                                          Random rnd, int amoLength, int stepSize) throws TimeoutException {
        List<Pair<Double, Double>> points = new LinkedList<>();
        List<Pair<Double, Double>> solvingTimePoints = new LinkedList<>();
        double xTick = 0;
        double yTick = -1;
        double xTickSolvingTime = 0;
        double yTickSolvingTime = 0;
        int amoCounter = 1;
        while (yTick != 0) {
            int allCounter = 0;
            int trueCounter = 0;
            long time = 0;
            for (int i = 0; i < 1000; i++) {
                IncrementalCdclSolver solver = new IncrementalCdclSolver();
                for (int j = 0; j < amoCounter; j++) {
                    solver.addAtMostOne(generateRandomLiterals(vars, rnd, amoLength));
                }

                long startTime = System.nanoTime();
                if (solver.solve(new int[]{})) {
                    trueCounter++;
                }
                long endTime = System.nanoTime();

                time += (endTime - startTime);

                allCounter++;
            }


            xTick = (double) amoCounter / vars;
            yTick = (double) trueCounter / allCounter;
            xTickSolvingTime = xTick;
            yTickSolvingTime = Math.max(yTickSolvingTime, TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS));

            addPoints(points, solvingTimePoints, xTick, yTick, xTickSolvingTime, time);

            //System.out.println(xTick + " " + yTick);

            amoCounter += stepSize;
        }

        createPlotCSV(points, "Phase transition of AMO constraint with " + amoLength + " literals");

        return solvingTimePoints;
    }

    public static void createPhaseTransitionPlotForAMOConstraintsWithClauses(int vars, int cls, int clsLength,
                                                                             int amoLength,
                                                                             int stepSize, int stepSizeCls) throws TimeoutException,
            UnitLiteralConflictException {

        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);

        for (int i = 0; i <= cls; i += stepSizeCls) {
            solvingTimeList.add(calculatePhaseTransitionAMOClause(vars, i, clsLength, rnd, amoLength, stepSize));
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        int counter = 0;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for AMO constraints with " + amoLength + " " +
                    "literals and " + counter + "clauses");

            counter += stepSizeCls;
        }

    }

    public static List<Pair<Double, Double>> calculatePhaseTransitionAMOClause(int vars, int cls, int clsLength,
                                                                               Random rnd, int amoLength, int stepSize) throws TimeoutException,
            UnitLiteralConflictException {
        List<Pair<Double, Double>> points = new LinkedList<>();
        List<Pair<Double, Double>> solvingTimePoints = new LinkedList<>();
        double xTick = 0;
        double yTick = -1;
        double xTickSolvingTime = 0;
        double yTickSolvingTime = 0;
        int amoCounter = 1;
        while (yTick != 0) {
            int allCounter = 0;
            int trueCounter = 0;
            long time = 0;
            for (int i = 0; i < 1000; i++) {
                IncrementalCdclSolver solver = instantiateSolver(vars, cls, clsLength, rnd, amoLength, amoCounter);

                long startTime = System.nanoTime();
                if (solver.solve(new int[]{})) {
                    trueCounter++;
                }
                long endTime = System.nanoTime();

                time += (endTime - startTime);

                allCounter++;
            }

            xTick = (double) amoCounter / vars;
            yTick = (double) trueCounter / allCounter;

            xTickSolvingTime = xTick;
            yTickSolvingTime = Math.max(yTickSolvingTime, TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS));

            addPoints(points, solvingTimePoints, xTick, yTick, xTickSolvingTime, time);

            //System.out.println(xTick + " " + yTick);

            amoCounter += stepSize;

        }

        createPlotCSV(points, "Phase transition of AMO constraints with " + amoLength + " literals and " + cls +
                " clauses");

        return solvingTimePoints;
    }

    private static IncrementalCdclSolver instantiateSolver(int vars, int cls, int clsLength, Random rnd, int amoLength, int amoCounter) {
        IncrementalCdclSolver solver = new IncrementalCdclSolver();

        for (int j = 0; j < cls; j++) {
            solver.addClause(generateRandomLiterals(vars, rnd, clsLength));
        }

        for (int j = 0; j < amoCounter; j++) {
            solver.addAtMostOne(generateRandomLiterals(vars, rnd, amoLength));
        }
        return solver;
    }

    public static void createPhaseTransitionPlotForDNFConstraintsWithClauses(int vars, int cls, int clsLength,
                                                                             int terms, int termLength,
                                                                             int stepSize, int stepSizeCls) throws TimeoutException,
            UnitLiteralConflictException {

        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);

        for (int i = 0; i <= cls; i += stepSizeCls) {
            solvingTimeList.add(calculatePhaseTransitionDNFClause(vars, i, clsLength, rnd, terms, termLength,
                    stepSize));
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        int counter = 0;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for DNF constraint with " + terms + " of " +
                    "length " + termLength + " and " + counter + " clauses");

            counter += stepSizeCls;
        }

    }

    public static List<Pair<Double, Double>> calculatePhaseTransitionDNFClause(int vars, int cls, int clsLength,
                                                                               Random rnd, int terms,
                                                                               int termLength, int stepSize) throws TimeoutException,
            UnitLiteralConflictException {
        List<Pair<Double, Double>> points = new LinkedList<>();
        List<Pair<Double, Double>> solvingTimePoints = new LinkedList<>();
        double xTick = 0;
        double yTick = -1;
        double xTickSolvingTime = 0;
        double yTickSolvingTime = 0;
        int dnfCounter = 1;
        while (yTick != 0) {
            int allCounter = 0;
            int trueCounter = 0;
            long time = 0;
            for (int i = 0; i < 1000; i++) {
                IncrementalCdclSolver solver = instantiateSolver(vars, cls, clsLength, rnd, terms, termLength,
                        dnfCounter);

                long startTime = System.nanoTime();
                if (solver.solve(new int[]{})) {
                    trueCounter++;
                }
                long endTime = System.nanoTime();

                time += (endTime - startTime);

                allCounter++;
            }

            xTick = (double) dnfCounter / vars;
            yTick = (double) trueCounter / allCounter;

            xTickSolvingTime = xTick;
            yTickSolvingTime = Math.max(yTickSolvingTime, TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS));

            addPoints(points, solvingTimePoints, xTick, yTick, xTickSolvingTime, time);

            //System.out.println(xTick + " " + yTick);

            dnfCounter += stepSize;
        }

        createPlotCSV(points,
                "Phase transition of DNF constraints with " + terms + " terms of length " + termLength + " and " + cls + " clauses");

        return solvingTimePoints;

    }

    private static IncrementalCdclSolver instantiateSolver(int vars, int cls, int clsLength, Random rnd, int terms,
                                                           int termLength, int amoCounter) {
        IncrementalCdclSolver solver = new IncrementalCdclSolver();

        for (int j = 0; j < cls; j++) {
            solver.addClause(generateRandomLiterals(vars, rnd, clsLength));
        }

        for (int j = 0; j < amoCounter; j++) {
            solver.addDnf(getRandomDNFTerms(vars, rnd, terms, termLength));
        }
        return solver;
    }


}
