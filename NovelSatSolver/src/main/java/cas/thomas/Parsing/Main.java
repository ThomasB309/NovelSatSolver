package cas.thomas.Parsing;

import cas.thomas.Exceptions.ClauseContainsZeroException;
import cas.thomas.Exceptions.ClauseNotTerminatedByZeroException;
import cas.thomas.Exceptions.EmptyClauseException;
import cas.thomas.Exceptions.IncorrectFirstLineException;
import cas.thomas.Formulas.Formula;
import cas.thomas.SolverAlgorithms.DPLL;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String... args) {

        if (args.length != 1) {
            System.err.println("You have to specify exactly one input file!");
        }

        for (int a = 1; a <= 100; a++) {
            String[] input = null;

            try {
                input =
                        Files.readAllLines(Paths.get("InputFiles", "flat30-60.tar", "flat30-" + a + ".cnf"),
                                StandardCharsets.UTF_8).toArray(new String[0]);
            } catch (IOException e) {
                System.err.println("Something went wrong while reading the specified input file!");
                System.exit(-1);
            }

            ClauseParser clauseParser = new ClauseParser();
            Formula formula = new Formula();

            try {
                formula = clauseParser.parseInput(input);
            } catch (IncorrectFirstLineException | NumberFormatException | ClauseNotTerminatedByZeroException | EmptyClauseException | ClauseContainsZeroException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }

            int[] variableOrdering = new int[formula.getNumberOfVariables() + 1];

            for (int i = 0; i < formula.getNumberOfVariables(); i++) {
                variableOrdering[i] = i + 1;
            }

            DPLL dpllSolver = new DPLL(variableOrdering);

            System.out.println(dpllSolver.solve(formula));
        }


    }
}
