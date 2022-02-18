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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {

    public static void main(String... args) {

        long startTime = System.nanoTime();

        if (args.length < 2) {
            System.out.println("Not enough input parameters!");
            System.exit(-1);
        } else if (args.length > 3) {
            System.out.println("Too many parameters!");
            System.exit(-1);
        }

        String executionMethod = args[0];

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
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Path inputPath = Paths.get(new File(args[1]).toURI());


        List<Path> cnfFiles = new ArrayList<>();

        try {

            if (Files.isDirectory(inputPath)) {

                cnfFiles =
                        Files.list(inputPath).filter(path -> path.toString().endsWith(".cnf") || path.toString().endsWith(".txt")).collect(Collectors.toList());

            } else if (Files.exists(inputPath)) {

                cnfFiles = new ArrayList<>();
                cnfFiles.add(inputPath);
            } else {
                System.err.println("The input file or directory doesn't exist!");
                System.exit(-1);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        int counter = 1;
        for (Path inputFile : cnfFiles) {

            String[] input = new String[0];

            try {
                input = Files.readAllLines(inputFile,
                        StandardCharsets.UTF_8).toArray(new String[0]);
            } catch (IOException e) {
                System.err.println(e.getMessage());
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


            if (executionMethod.equals("-c") || executionMethod.equals("--convert")) {

                if (args.length < 3) {
                    System.err.println("Missing output parameter!");
                    System.exit(-1);
                }

                Path outputPath = Paths.get(new File(args[2]).toURI());

                if (!Files.isDirectory(outputPath)) {
                    System.err.println("The output directory does not exist!");
                    System.exit(-1);
                }

                try {
                    solutionCheckerFormula.toDimacsCNFFile(Paths.get(outputPath.toString(),
                            inputFile.getFileName().toString()));
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.exit(-1);
                }

            } else if (executionMethod.equals("-s") || executionMethod.equals("--solve")) {
                String isSatisfiable = dpllSolver.solve(formula);
                System.out.print(counter + ": ");
                System.out.println(isSatisfiable);
                //assert (solutionCheckerFormula.isTrue(formula.getVariablesForSolutionChecker()));
            } else {
                System.err.println("Incorrect execution method!");
                System.exit(-1);
            }

            counter++;
        }

        long endTime = System.nanoTime();

        System.out.println(TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS));


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
