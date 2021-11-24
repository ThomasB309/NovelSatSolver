package cas.thomas.Parsing;

import cas.thomas.ConflictHandling.DPLLConflictHandler;
import cas.thomas.ConflictHandling.ConflictHandlingStrategy;
import cas.thomas.ConflictHandling.CDCLConflictHandler;
import cas.thomas.Exceptions.ClauseContainsZeroException;
import cas.thomas.Exceptions.ClauseNotTerminatedByZeroException;
import cas.thomas.Exceptions.EmptyClauseException;
import cas.thomas.Exceptions.IncorrectFirstLineException;
import cas.thomas.Formulas.Formula;
import cas.thomas.RestartHandling.NoRestartsSchedulingStrategy;
import cas.thomas.RestartHandling.ReluctantDoublingRestartStrategy;
import cas.thomas.RestartHandling.RestartSchedulingStrategy;
import cas.thomas.SolutionChecker.SolutionCheckerFormula;
import cas.thomas.SolverAlgorithms.mDPLL;
import cas.thomas.SolverAlgorithms.SolverAlgorithm;
import cas.thomas.VariableSelection.FirstOpenVariableSelection;
import cas.thomas.VariableSelection.VSIDS;
import cas.thomas.VariableSelection.VariableSelectionStrategy;
import cas.thomas.utils.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String... args) {

        long startTime = System.nanoTime();

        if (args.length != 1) {
            System.err.println("You have to specify exactly one input file!");
        }

        Properties properties = new Properties();

        /*
        This section of reading the properties and input files is only temporary for debugging purposes. Later on the
         user will be able to choose the input files with command line options.
         */

        try {
            properties.load(new FileReader(new File("C:\\Masterthesis\\NovelSatSolver\\src\\main\\resources\\config" +
                    ".properties")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (int a = 1; a <= 100; a++) {
            String[] input = null;

            try {
                input =
                        Files.readAllLines(Paths.get( "InputFiles",    "200","uf200-0" + a + ".cnf"),
                                StandardCharsets.UTF_8).toArray(new String[0]);
            } catch (IOException e) {
                System.err.println("Something went wrong while reading the specified input file!");
                System.exit(-1);
            }

            ClauseParser clauseParser = new ClauseParser();
            Formula formula = null;
            SolutionCheckerFormula solutionCheckerFormula = null;

            try {
                Pair<Formula, SolutionCheckerFormula> formulaPair = clauseParser.parseInput(input);
                formula = formulaPair.getFirstPairPart();
                solutionCheckerFormula = formulaPair.getSecondPairPart();
            } catch (IncorrectFirstLineException | NumberFormatException | ClauseNotTerminatedByZeroException | EmptyClauseException | ClauseContainsZeroException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }

            SolverAlgorithm dpllSolver = new mDPLL(getSelectionStrategy(properties),
                    getConflictHandlingStrategy(properties), getRestartSchedulingStrategy(properties),
                    getPhaseSavingStrategy(properties), getFirstBranchingDecision(properties));
            System.out.print(a + ": ");
            String isSatisfiable = dpllSolver.solve(formula);
            System.out.println(isSatisfiable);
            assert (solutionCheckerFormula.isTrue(formula.getVariablesForSolutionChecker()));
        }

        long endTime = System.nanoTime();

        System.out.println(TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS));


    }

    public static VariableSelectionStrategy getSelectionStrategy(Properties properties) {
        String selectionStrategy = properties.getProperty("variableSelectionStrategy");

        if (selectionStrategy != null) {
            if (selectionStrategy.equals("firstOpenVariable")) {
                return new FirstOpenVariableSelection();
            } else if (selectionStrategy.equals("VSIDS")) {
                return new VSIDS();
            }
        }

        return new FirstOpenVariableSelection();

    }

    public static ConflictHandlingStrategy getConflictHandlingStrategy(Properties properties) {
        String selectionStrategy = properties.getProperty("conflictHandlingStrategy");

        if (selectionStrategy != null) {
            if (selectionStrategy.equals("normal")) {
                return new DPLLConflictHandler();
            } else if (selectionStrategy.equals("clauseLearning")) {
                return new CDCLConflictHandler();
            }
        }

        return new DPLLConflictHandler();
    }

    public static RestartSchedulingStrategy getRestartSchedulingStrategy(Properties properties) {
        String restartStrategy = properties.getProperty("restartSchedulingStrategy");

        String numberOfInitialConflicts = properties.getProperty("numberOfInitialConflicts");
        int conflictNumber = 512;

        if (numberOfInitialConflicts != null) {
            try {
                conflictNumber = Integer.parseInt(numberOfInitialConflicts);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (restartStrategy != null) {
            if (restartStrategy.equals("noRestarts")) {
                return new NoRestartsSchedulingStrategy(conflictNumber);
            } else if (restartStrategy.equals("reluctantDoubling")) {
                return new ReluctantDoublingRestartStrategy(conflictNumber);
            }
        }

        return new NoRestartsSchedulingStrategy(conflictNumber);
    }

    public static boolean getPhaseSavingStrategy(Properties properties) {
        return Boolean.parseBoolean(properties.getProperty("phaseSaving"));
    }

    public static boolean getFirstBranchingDecision(Properties properties) {
        return Boolean.parseBoolean(properties.getProperty("firstBranchingDecision"));
    }
}
