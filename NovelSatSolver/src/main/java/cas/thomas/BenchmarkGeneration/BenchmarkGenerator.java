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

        IntStream.range(1, 101).parallel().forEach(a -> {

            try {
                random(10000, 0, 10000,0,a).toDimacsFile(Paths.get("GeneratedBenchmarks",
                        "amo_40000_10000",
                        "amo_40000_10000_" + + a +
                                ".cnf"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

    }

    public static SolutionCheckerFormula random(int vars, int cls, int amo, int dnf,long seed) {
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

        return new SolutionCheckerConjunctiveFormula(constraints.toArray(SolutionCheckerConstraint[]::new), vars);
    }

    public static void generateClauseConstraints(int vars, int cls, List<SolutionCheckerConstraint> constraints, Random rnd,
                                          List<Integer> solution) {
        int constraintCounter = 0;
        while (constraintCounter< cls) {
            int[] clause = new int[rnd.nextInt(7) + 2];
            HashSet<Integer> usedVariables = new HashSet<>();
            for (int i = 0; i < clause.length; i++) {
                int var;
                do {
                    var = rnd.nextInt(vars) + 1;
                } while (usedVariables.contains(var));
                usedVariables.add(var);
                clause[i] = rnd.nextBoolean() ? var : -var;
            }

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
            int[] clause = new int[rnd.nextInt(5) + 8];
            HashSet<Integer> usedVariables = new HashSet<>();
            for (int i = 0; i < clause.length; i++) {
                int var;
                do {
                    var = rnd.nextInt(vars) + 1;
                } while (usedVariables.contains(var));
                usedVariables.add(var);
                clause[i] = rnd.nextBoolean() ? var : -var;
            }

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
        while (constraintCounter< dnf) {
            int[][] clause = new int[rnd.nextInt(3) + 2][rnd.nextInt(3) + 4];
            for (int i = 0; i < clause.length; i++) {
                HashSet<Integer> usedVariables = new HashSet<>();
                for (int j = 0; j < clause[i].length; j++) {
                    int var;
                    do {
                        var = rnd.nextInt(vars) + 1;
                    } while (usedVariables.contains(var));
                    usedVariables.add(var);
                    clause[i][j] = rnd.nextBoolean() ? var : -var;
                }
            }

            SolutionCheckerDNFConstraint dnfConstraint = new SolutionCheckerDNFConstraint(clause);

            if (dnfConstraint.isTrue(solution)) {
                constraints.add(dnfConstraint);
                constraintCounter++;
            }
        }
    }


}
