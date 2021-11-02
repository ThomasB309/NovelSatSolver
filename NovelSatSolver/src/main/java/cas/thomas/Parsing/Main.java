package cas.thomas.Parsing;

import cas.thomas.Exceptions.ClauseContainsZeroException;
import cas.thomas.Exceptions.ClauseNotTerminatedByZeroException;
import cas.thomas.Exceptions.EmptyClauseException;
import cas.thomas.Exceptions.IncorrectFirstLineException;
import cas.thomas.Formulas.Formula;
import cas.thomas.SolutionChecker.SolutionCheckerFormula;
import cas.thomas.SolverAlgorithms.mDPLL;
import cas.thomas.SolverAlgorithms.SolverAlgorithm;
import cas.thomas.VariableSelection.FirstOpenVariableSelection;
import cas.thomas.VariableSelection.MostOccurencesVariableSelection;
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

        try {
            properties.load(new FileReader(new File("C:\\Masterthesis\\NovelSatSolver\\src\\main\\resources\\config" +
                    ".properties")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (int a = 1; a <= 1000; a++) {
            String[] input = null;

            try {
                input =
                        Files.readAllLines(Paths.get( "InputFiles",  "uf50-0" + a + ".cnf"),
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

            SolverAlgorithm dpllSolver = new mDPLL(getSelectionStrategy(properties));
            System.out.print(a + ": ");
            String isSatisfiable = dpllSolver.solve(formula);
            System.out.println(solutionCheckerFormula.isTrue(formula.getVariablesForSolutionChecker()));
        }

        long endTime = System.nanoTime();

        System.out.println(TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS));


    }

    public static VariableSelectionStrategy getSelectionStrategy(Properties properties) {
        Object selectionStrategy = properties.get("variableSelectionStrategy");

        if (selectionStrategy != null) {
            String selectionStrategyString = (String) selectionStrategy;

            if (selectionStrategyString.equals("firstOpenVariable")) {
                return new FirstOpenVariableSelection();
            } else if (selectionStrategyString.equals("mostOccurences")) {
                return new MostOccurencesVariableSelection();
            }
        }

        return new FirstOpenVariableSelection();

    }
}
