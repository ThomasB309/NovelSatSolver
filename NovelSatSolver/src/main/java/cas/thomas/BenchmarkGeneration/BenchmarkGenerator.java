package cas.thomas.BenchmarkGeneration;

import cas.thomas.SolutionChecker.SolutionCheckerAMOConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerConjunctiveFormula;
import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDNFConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDisjunctiveConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerFormula;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class BenchmarkGenerator {

    public static void main(String[] args) {

        if (args.length != 7) {
            System.err.println("There have to be exactly 5 arguments: 1. Number of Formulas, 2. Number of Variables, " +
                    "3. Number of clauses, 4. Number of AMO constraints, 5. Number of DNF constraints, 6. Folder " +
                    "location, 7. File prefix");
            System.exit(-1);
        }

        try {
            final int numberOfFormulas = Integer.parseInt(args[0]);
            final int numberOfVariables = Integer.parseInt(args[1]);
            final int numberOfClauses = Integer.parseInt(args[2]);
            final int numberOfAMOConstraints = Integer.parseInt(args[3]);
            final int numberOfDNFConstraints = Integer.parseInt(args[4]);

            IntStream.range(1, numberOfFormulas + 1).parallel().forEach(a -> {

                try {
                    random(numberOfVariables, numberOfClauses, numberOfAMOConstraints, numberOfDNFConstraints, a).toDimacsFile(Paths.get(
                            args[6],
                            args[7] + a +
                                    ".cnf"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        } catch (NumberFormatException e) {
            System.err.println("One of the numbers is not an integer!");
            System.exit(-1);
        }

    }

    public static SolutionCheckerFormula random(int vars, int cls, int amo, int dnf, long seed) {
        Random rnd = new Random(seed);
        Integer[] plantedSolution = new Integer[vars + 1];
        plantedSolution[0] = 0;
        for (int i = 1; i <= vars; i++) {
            plantedSolution[i] = rnd.nextBoolean() ? i : -i;
        }

        List<Integer> solution = Arrays.asList(plantedSolution);

        List<SolutionCheckerConstraint> constraints = new ArrayList<>();

        generateClauseConstraints(vars, cls, constraints, rnd, solution);
        generateAMOConstraints(vars, amo, constraints, rnd, solution);
        generateDNFConstraints(vars, dnf, constraints, rnd, solution);

        System.out.println("Finished another one!");

        return new SolutionCheckerConjunctiveFormula(constraints.toArray(SolutionCheckerConstraint[]::new), vars);
    }

    public static void generateClauseConstraints(int vars, int cls, List<SolutionCheckerConstraint> constraints, Random rnd,
                                                 List<Integer> solution) {
        int constraintCounter = 0;
        while (constraintCounter < cls) {
            int[] clause = generateRandomLiterals(vars, rnd, 5);

            SolutionCheckerDisjunctiveConstraint clauseConstraint = new SolutionCheckerDisjunctiveConstraint(clause);

            if (clauseConstraint.isTrue(solution)) {
                constraints.add(clauseConstraint);
                constraintCounter++;
            }
        }
    }

    public static void generateAMOConstraints(int vars, int amo, List<SolutionCheckerConstraint> constraints,
                                              Random rnd,
                                              List<Integer> solution) {

        int constraintCounter = 0;
        while (constraintCounter < amo) {
            int[] clause = generateRandomLiterals(vars, rnd, 3);

            SolutionCheckerAMOConstraint amoConstraint = new SolutionCheckerAMOConstraint(clause);

            if (amoConstraint.isTrue(solution)) {
                constraints.add(amoConstraint);
                constraintCounter++;
            }
        }
    }

    public static void generateDNFConstraints(int vars, int dnf, List<SolutionCheckerConstraint> constraints,
                                              Random rnd, List<Integer> solution) {

        int constraintCounter = 0;
        while (constraintCounter < dnf) {
            int[][] clause = getRandomDNFTerms(vars, rnd, 3, 3);

            SolutionCheckerDNFConstraint dnfConstraint = new SolutionCheckerDNFConstraint(clause);

            constraints.add(dnfConstraint);
            constraintCounter++;

            if (dnfConstraint.isTrue(solution)) {
                constraints.add(dnfConstraint);
                constraintCounter++;
            }
        }
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


}
