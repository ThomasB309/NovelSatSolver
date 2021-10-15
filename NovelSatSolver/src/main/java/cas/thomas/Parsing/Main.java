package cas.thomas.Parsing;

import cas.thomas.Exceptions.ClauseContainsZeroException;
import cas.thomas.Exceptions.ClauseNotTerminatedByZeroException;
import cas.thomas.Exceptions.EmptyClauseException;
import cas.thomas.Exceptions.IncorrectFirstLineException;
import cas.thomas.Formulas.Formula;
import cas.thomas.SolverAlgorithms.mDPLL;
import cas.thomas.SolverAlgorithms.ISolverAlgorithm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String... args) {

        long startTime = System.nanoTime();

        if (args.length != 1) {
            System.err.println("You have to specify exactly one input file!");
        }

        for (int a = 1; a <= 1000; a++) {
            String[] input = null;

            try {
                input =
                        Files.readAllLines(Paths.get( "InputFiles", "uf20-0" + a+ ".cnf"),
                                StandardCharsets.UTF_8).toArray(new String[0]);
            } catch (IOException e) {
                System.err.println("Something went wrong while reading the specified input file!");
                System.exit(-1);
            }

            ClauseParser clauseParser = new ClauseParser();
            Formula formula = null;

            try {
                formula = clauseParser.parseInput(input);
            } catch (IncorrectFirstLineException | NumberFormatException | ClauseNotTerminatedByZeroException | EmptyClauseException | ClauseContainsZeroException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }

            ISolverAlgorithm dpllSolver = new mDPLL();
            System.out.print(a + ": ");
            System.out.println(dpllSolver.solve(formula));
        }

        long endTime = System.nanoTime();

        System.out.println(TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS));


    }
}
