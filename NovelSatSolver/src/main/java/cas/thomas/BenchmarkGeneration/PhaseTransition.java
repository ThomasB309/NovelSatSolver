package cas.thomas.BenchmarkGeneration;

import cas.thomas.Exceptions.SolverTimeoutException;
import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.SolutionChecker.SolutionCheckerAMOConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerConjunctiveFormula;
import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDNFConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDisjunctiveConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerFormula;
import cas.thomas.SolverAlgorithms.IncrementalCdclSolver;
import cas.thomas.utils.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final String[] COLORS = new String[]{"red", "blue", "green", "black", "brown", "purple", "orange",
            "yellow", "pink"};

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Choose a phase transition!");
            System.exit(-1);
        }

        switch (args[0]) {
            case "A":
        }

    }

    public static SolutionCheckerFormula random(int vars, int cls, int amo, int dnf, long seed) throws TimeoutException, UnitLiteralConflictException, SolverTimeoutException {
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
                                                                                        int stepSize) throws TimeoutException, SolverTimeoutException {
        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);

        int counter = 0;
        for (int i = 2; i <= termLength; i++) {
            solvingTimeList.add(calculatePhaseTransitionDNF(vars, rnd, terms, i, stepSize, counter));
            counter++;
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        counter = 2;
        int colorCounter = 0;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for DNF constraints with " + terms + " " +
                    "terms of length " + counter, colorCounter);

            counter++;
            colorCounter++;
        }
    }

    public static List<Pair<Double, Double>> calculatePhaseTransitionDNF(int vars, Random rnd, int terms,
                                                                         int termLength, int stepSize,
                                                                         int colorCounter) throws TimeoutException, SolverTimeoutException {
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

            dnfCounter += stepSize;

        }

        createPlotCSV(points, "Phase transition of DNF constraints with " + terms + " terms of length " + termLength,
                colorCounter);

        return solvingTimePoints;
    }

    private static void createTikzPlotNormalizedByY(int maxValue, List<Pair<Double, Double>> points,
                                                    String name, int counter) {


        System.out.println(name);
        System.out.println("\\addplot+ [" + COLORS[counter] + ", dashed, no marks] table {");
        for (Pair<Double, Double> point : points) {
            System.out.println(point.getFirstPairPart() + " " + point.getSecondPairPart() / (maxValue));
        }
        System.out.println("}");
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

    private static void createPlotCSV(List<Pair<Double, Double>> points, String name, int counter) {
        System.out.println(name);
        System.out.println("\\addplot+ [" + COLORS[counter] + ", no marks] table {");
        for (Pair<Double, Double> point : points) {
            System.out.println(point.getFirstPairPart() + " " + point.getSecondPairPart());
        }
        System.out.println("}");
    }

    public static void createPhaseTransitionPlotForAMOConstraintsWithAConstantNumberOfClausesAndDNFConstraints(int vars,
                                                                                                          int amoLength, int amoStepSize
            , int iterations, int clauseLength, int clauseStepSize, int termCount, int termLength, int dnfStepSize)
            throws TimeoutException, SolverTimeoutException {


        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);

        int cls = 0;
        int dnf = 0;
        int counter = 0;
        for (int i = 0; i < iterations; i++) {
            solvingTimeList.add(calculatePhaseTransitionAMODNFClS(vars, amoLength, amoStepSize, cls, dnf,
                    clauseLength, termCount, termLength, rnd, counter));
            cls += clauseStepSize;
            dnf += dnfStepSize;
            counter++;
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        counter = 1;
        int colorCounter = 0;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for DNF constraints with " + counter + " " +
                    "terms of length " + termLength, colorCounter);

            counter++;
            colorCounter++;
        }

    }

    public static void createPhaseTransitionBenchmarks(int vars, int clauseCount, int clauseLength, int amoCount,
                                                  int amoLength, int dnfCount, int termCount, int termLength,
                                                  int numberOfBenchmarks, Path filePath) throws TimeoutException, IOException, SolverTimeoutException {

        List<SolutionCheckerConjunctiveFormula> satisfiableFormulas = new ArrayList<>();
        List<SolutionCheckerConjunctiveFormula> unsatisfiableFormulas = new ArrayList<>();

        int loopCounter = 0;
        while (satisfiableFormulas.size() < numberOfBenchmarks || unsatisfiableFormulas.size() < numberOfBenchmarks) {
            Random rnd = new Random(loopCounter);
            IncrementalCdclSolver solver = new IncrementalCdclSolver();
            SolutionCheckerConstraint[] constraints = new SolutionCheckerConstraint[clauseCount + amoCount + dnfCount];
            int counter = 0;
            for (int a = 0; a < clauseCount; a++) {
                int[] clause = generateRandomLiterals(vars, rnd, clauseLength);
                solver.addClause(clause);
                constraints[counter] = new SolutionCheckerDisjunctiveConstraint(clause);
                counter++;
            }

            for (int a = 0; a < amoCount; a++) {
                int[] clause = generateRandomLiterals(vars, rnd, amoLength);
                solver.addAtMostOne(clause);
                constraints[counter] = new SolutionCheckerAMOConstraint(clause);
                counter++;
            }

            for (int a = 0; a < dnfCount; a++) {
                int[][] terms = getRandomDNFTerms(vars, rnd, termCount, termLength);
                solver.addDnf(terms);
                constraints[counter] = new SolutionCheckerDNFConstraint(terms);
                counter++;
            }

            boolean satisfiable = solver.solve(new int[0]);

            System.out.println(satisfiable);

            if (satisfiable && satisfiableFormulas.size() < numberOfBenchmarks) {
                satisfiableFormulas.add(new SolutionCheckerConjunctiveFormula(constraints,vars));
            } else if (!satisfiable && unsatisfiableFormulas.size() < numberOfBenchmarks) {
                unsatisfiableFormulas.add(new SolutionCheckerConjunctiveFormula(constraints, vars));
            }

            System.out.println(loopCounter);
            loopCounter++;

        }

        int counter = 1;
        String path = String.valueOf(filePath);
        Files.createDirectory(Paths.get(path, "sat"));
        Files.createDirectory(Paths.get(path, "sat", "rcnf"));
        Files.createDirectory(Paths.get(path, "sat", "cnf"));
        Files.createDirectory(Paths.get(path, "unsat"));
        Files.createDirectory(Paths.get(path, "unsat", "rcnf"));
        Files.createDirectory(Paths.get(path, "unsat", "cnf"));
        for (SolutionCheckerConjunctiveFormula formula : satisfiableFormulas) {
            formula.toDimacsFile(Paths.get(String.valueOf(filePath), "sat","rcnf","sat_" + counter + ".rcnf"));
            formula.toDimacsCNFFile(Paths.get(String.valueOf(filePath), "sat","cnf","sat_" + counter + ".cnf"));
            counter++;
        }

        counter = 1;
        for (SolutionCheckerConjunctiveFormula formula : unsatisfiableFormulas) {
            formula.toDimacsFile(Paths.get(String.valueOf(filePath), "unsat","rcnf","unsat_" + counter + ".rcnf"));
            formula.toDimacsCNFFile(Paths.get(String.valueOf(filePath), "unsat","cnf","unsat_" + counter + ".cnf"));
            counter++;
        }

    }

    private static List<Pair<Double,Double>> calculatePhaseTransitionAMODNFClS(int vars, int amoLength, int amoStepSize
            , int cls, int dnf, int clauseLength, int termCount, int termLength,
             Random rnd, int colorCounter) throws TimeoutException, SolverTimeoutException {

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

            amoCounter += amoStepSize;

        }

        createPlotCSV(points, "Phase transition of AMO constraints with " + amoLength + " literals and " + dnf +
                " DNF constraints with " + termCount + " terms of length " + termLength + " and " + cls + " clauses " +
                "of" +
                " length " + clauseLength, colorCounter);

        return solvingTimePoints;
    }

    public static void createPlotsForAMOPhaseTransitionWithAConstantNumberOfDNFConstraints(int vars, int dnf,
                                                                                            int terms, int termLength, int amoLength, int amoStepSize, int dnfStepSize) throws TimeoutException, UnitLiteralConflictException, SolverTimeoutException {
        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);

        int counter = 0;
        for (int i = 0; i < dnf; i += dnfStepSize) {
            solvingTimeList.add(calculatePhaseTransitionAMODNF(vars, i, terms, termLength, rnd, amoLength,
                    amoStepSize, counter));

            counter++;
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        counter = 1;
        int colorCounter = 0;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes,
                    "Phase transition of AMO constraints with " + amoLength + " literals and " + dnf +
                    " DNF constraints with " + terms + " terms of length " + termLength, colorCounter);

            counter++;
            colorCounter++;
        }
    }

    public static List<Pair<Double, Double>> calculatePhaseTransitionAMODNF(int vars, int dnf, int terms, int termLength,
                                                                            Random rnd,
                                                                            int amoLength, int stepSize,
                                                                            int colorCounter) throws TimeoutException,
            UnitLiteralConflictException, SolverTimeoutException {

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
                " DNF constraints with " + terms + " terms of length " + termLength, colorCounter);

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
                                                                                         int termLength, int stepSize) throws TimeoutException, SolverTimeoutException {
        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);
        int counter = 0;
        for (int i = 1; i <= terms; i++) {
            solvingTimeList.add(calculatePhaseTransitionDNF(vars, rnd, i, termLength, stepSize, counter));
            counter++;
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        counter = 1;
        int colorCounter = 0;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for DNF constraints with " + counter + " " +
                    "terms of length " + termLength, colorCounter);

            counter++;
            colorCounter++;
        }
    }

    public static void createPhaseTransitionPlotForAMOConstraints(int vars, int amoLength, int stepSize) throws TimeoutException,
            UnitLiteralConflictException, SolverTimeoutException {

        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);

        int counter = 0;
        for (int i = 2; i <= amoLength; i++) {
            solvingTimeList.add(calculatePhaseTransitionAMO(vars, rnd, i, stepSize, counter));
            counter++;
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        counter = 2;
        int colorCounter = 0;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for AMO constraints with " + counter + " " +
                    "literals", colorCounter);

            counter++;
            colorCounter++;
        }

    }

    private static List<Pair<Double, Double>> calculatePhaseTransitionAMO(int vars,
                                                                          Random rnd, int amoLength, int stepSize,
                                                                          int colorCounter) throws TimeoutException, SolverTimeoutException {
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

            amoCounter += stepSize;
        }

        createPlotCSV(points, "Phase transition of AMO constraint with " + amoLength + " literals", colorCounter);

        return solvingTimePoints;
    }

    public static void createPhaseTransitionPlotForAMOConstraintsWithClauses(int vars, int cls, int clsLength,
                                                                             int amoLength,
                                                                             int stepSize, int stepSizeCls) throws TimeoutException,
            UnitLiteralConflictException, SolverTimeoutException {

        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);
        int counter = 0;
        for (int i = 0; i <= cls; i += stepSizeCls) {
            solvingTimeList.add(calculatePhaseTransitionAMOClause(vars, i, clsLength, rnd, amoLength, stepSize, counter));
            counter++;
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        counter = 0;
        int colorCounter = 0;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for AMO constraints with " + amoLength + " " +
                    "literals and " + counter + "clauses", colorCounter);

            counter += stepSizeCls;
            colorCounter++;
        }

    }

    public static List<Pair<Double, Double>> calculatePhaseTransitionAMOClause(int vars, int cls, int clsLength,
                                                                               Random rnd, int amoLength,
                                                                               int stepSize, int colorCounter) throws TimeoutException,
            UnitLiteralConflictException, SolverTimeoutException {
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

            amoCounter += stepSize;

        }

        createPlotCSV(points, "Phase transition of AMO constraints with " + amoLength + " literals and " + cls +
                " clauses", colorCounter);

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
            UnitLiteralConflictException, SolverTimeoutException {

        List<List<Pair<Double, Double>>> solvingTimeList = new LinkedList<>();

        Random rnd = new Random(1);
        int counter = 0;
        for (int i = 0; i <= cls; i += stepSizeCls) {
            solvingTimeList.add(calculatePhaseTransitionDNFClause(vars, i, clsLength, rnd, terms, termLength,
                    stepSize, counter));
            counter++;
        }

        int max = Integer.MIN_VALUE;

        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            max =
                    Math.max((int) Math.ceil(solvingTimes.stream().max(Comparator.comparing(Pair::getSecondPairPart)).get().getSecondPairPart()), max);
        }

        counter = 0;
        int colorCounter = 0;
        for (List<Pair<Double, Double>> solvingTimes : solvingTimeList) {
            createTikzPlotNormalizedByY(max, solvingTimes, "Solving time for DNF constraint with " + terms + " of " +
                    "length " + termLength + " and " + counter + " clauses",colorCounter);

            counter += stepSizeCls;
            colorCounter++;
        }

    }

    public static List<Pair<Double, Double>> calculatePhaseTransitionDNFClause(int vars, int cls, int clsLength,
                                                                               Random rnd, int terms,
                                                                               int termLength, int stepSize,
                                                                               int colorCounter) throws TimeoutException,
            UnitLiteralConflictException, SolverTimeoutException {
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

            dnfCounter += stepSize;
        }

        createPlotCSV(points,
                "Phase transition of DNF constraints with " + terms + " terms of length " + termLength + " and " + cls + " clauses", colorCounter);

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
