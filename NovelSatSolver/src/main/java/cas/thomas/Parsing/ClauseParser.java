package cas.thomas.Parsing;

import cas.thomas.Formulas.Clause;
import cas.thomas.Exceptions.ClauseContainsZeroException;
import cas.thomas.Exceptions.ClauseNotTerminatedByZeroException;
import cas.thomas.Exceptions.EmptyClauseException;
import cas.thomas.Exceptions.IncorrectFirstLineException;
import cas.thomas.Formulas.Formula;
import cas.thomas.Formulas.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClauseParser {

    public Formula parseInput(String[] lines) throws IncorrectFirstLineException, ClauseNotTerminatedByZeroException,
            EmptyClauseException, ClauseContainsZeroException {

        int indexOfFirstLine = findFirstLine(lines);

        String[] firstLine = lines[indexOfFirstLine].split("\\s+");

        if (firstLine.length != 4) {
            throw new IncorrectFirstLineException("Your first line has an incorrect amount of parameters!");
        }

        int numberOfVariables;
        int numberOfClauses;

        try {
            numberOfVariables = Integer.parseInt(firstLine[2]);
            numberOfClauses = Integer.parseInt(firstLine[3]);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The number of variables or the number of clauses are not an integer!");
        }

        if (indexOfFirstLine + numberOfClauses >= lines.length) {
            throw new IncorrectFirstLineException("Your first line specifies more clauses than there are available in" +
                    " the file.");
        }

        Clause[] clauses = new Clause[numberOfClauses];
        Variable[] variables = new Variable[numberOfVariables];
        List<Integer> listOfUnitVariables = new ArrayList<>();

        int clausecounter = 0;
        for (int i = indexOfFirstLine + 1; i < indexOfFirstLine + numberOfClauses + 1; i++) {

            String line = lines[i];

            if (line.startsWith("c")) {
                continue;
            }

            Clause nextClause = parseClause(lines[i], numberOfVariables, clausecounter, listOfUnitVariables);

            if (nextClause != null) {
                clauses[clausecounter] = nextClause;
            }

            clausecounter++;
        }

        if (clauses.length != numberOfClauses) {
            throw new IncorrectFirstLineException("The given amount of clauses doesn't match the specified amount of " +
                    "clauses!");
        }


        return new Formula(clauses, numberOfClauses, numberOfVariables, listOfUnitVariables);


    }

    private int findFirstLine(String[] lines) throws IncorrectFirstLineException {
        for (int i = 0; i < lines.length; i++) {
            String currentLine = lines[i];

            if (currentLine.startsWith("c ") || currentLine.equals("c")) {
                continue;
            } else if (currentLine.startsWith("p cnf")) {
                return i;
            } else {
                throw new IncorrectFirstLineException("Check your input! The first non-comment line either has an " +
                        "incorrect format or doesn't even exist!");
            }
        }

        throw new IncorrectFirstLineException("Your input does not contain a defining first line!");
    }

    private Clause parseClause(String line, int numberOfVariables, int formulaPosition,
                               List<Integer> listOfUnitVariables) throws ClauseNotTerminatedByZeroException,
            EmptyClauseException, ClauseContainsZeroException {
        try {

            int[] variables = checkAndParseInputVariables(line, numberOfVariables);

            if (variables.length == 1) {
                listOfUnitVariables.add(variables[0]);
            }

            return new Clause(numberOfVariables, variables);

        } catch(NumberFormatException e) {
            throw new NumberFormatException("Bad clause variables!");
        }
    }

    private int[] checkAndParseInputVariables(String line, int numberOfVariables) throws
    ClauseNotTerminatedByZeroException, EmptyClauseException, ClauseContainsZeroException {

        String[] lineParts = Arrays.stream(line.split("\\s+")).filter(part -> !part.equals("")).toArray(String[]::new);

        if (!lineParts[lineParts.length - 1].equals("0")) {
            throw new ClauseNotTerminatedByZeroException("One of the clauses was not terminated by a zero!");
        }

        int[] variables =
                Arrays.stream(Arrays.copyOf(lineParts, lineParts.length - 1)).mapToInt(Integer::parseInt).toArray();


        if (!(variables.length > 0)) {
            throw new EmptyClauseException("One of the clauses is empty!");
        }

        for (int i = 0; i < variables.length; i++) {
            if (variables[i] == 0) {
                throw new ClauseContainsZeroException("One of the clauses contains a 0");
            }
        }

        int max = Arrays.stream(variables).max().getAsInt();

        if (max > numberOfVariables) {
            throw new IllegalArgumentException("The number of variables doesn't match the variables");
        }

        return variables;

    }



}
