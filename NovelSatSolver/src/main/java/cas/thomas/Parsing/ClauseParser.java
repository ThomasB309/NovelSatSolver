package cas.thomas.Parsing;

import cas.thomas.Formulas.AMOConstraint;
import cas.thomas.Formulas.ConjunctiveFormula;
import cas.thomas.Formulas.Constraint;
import cas.thomas.Exceptions.ClauseContainsZeroException;
import cas.thomas.Exceptions.ClauseNotTerminatedByZeroException;
import cas.thomas.Exceptions.EmptyClauseException;
import cas.thomas.Exceptions.IncorrectFirstLineException;
import cas.thomas.Formulas.DisjunctiveConstraint;
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

        Constraint[] constraints = new Constraint[numberOfClauses];
        Variable[] variables = new Variable[numberOfVariables];
        List<Integer> listOfUnitVariables = new ArrayList<>();

        int clausecounter = 0;
        for (int i = indexOfFirstLine + 1; i < indexOfFirstLine + numberOfClauses + 1; i++) {

            String line = lines[i];

            if (line.startsWith("c")) {
                continue;
            }

            Constraint nextConstraint = parseClause(lines[i], numberOfVariables, clausecounter, listOfUnitVariables);

            if (nextConstraint != null) {
                constraints[clausecounter] = nextConstraint;
            }

            clausecounter++;
        }

        if (constraints.length != numberOfClauses) {
            throw new IncorrectFirstLineException("The given amount of clauses doesn't match the specified amount of " +
                    "clauses!");
        }


        return new ConjunctiveFormula(constraints, numberOfClauses, numberOfVariables, listOfUnitVariables);


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

    private Constraint parseClause(String line, int numberOfVariables, int formulaPosition,
                                   List<Integer> listOfUnitVariables) throws ClauseNotTerminatedByZeroException,
            EmptyClauseException, ClauseContainsZeroException {

        try {

            String[] input = Arrays.stream(line.split("\\s+")).filter(part -> !part.equals("")).toArray(String[]::new);

            String identifier = input[0];

            if (identifier.equals("AMO")) {
                return parseAMOConstraint(input, numberOfVariables);
            }

            int[] variables = checkAndParseInputVariables(input, numberOfVariables, false);

            if (variables.length == 1) {
                listOfUnitVariables.add(variables[0]);
            }

            return new DisjunctiveConstraint(numberOfVariables, variables);

        } catch(NumberFormatException e) {
            throw new NumberFormatException("Bad clause variables!");
        }
    }

    private AMOConstraint parseAMOConstraint(String[] lineParts, int numberOfVariables) throws EmptyClauseException, ClauseContainsZeroException, ClauseNotTerminatedByZeroException {
        int[] variables = checkAndParseInputVariables(lineParts, numberOfVariables, true);
        return new AMOConstraint(numberOfVariables, variables);
    }

    private int[] checkAndParseInputVariables(String[] lineParts, int numberOfVariables, boolean hasIdentifier) throws
    ClauseNotTerminatedByZeroException, EmptyClauseException, ClauseContainsZeroException {

        if (!lineParts[lineParts.length - 1].equals("0")) {
            throw new ClauseNotTerminatedByZeroException("One of the clauses was not terminated by a zero!");
        }

        int startRange = hasIdentifier ? 1 : 0;

        int[] variables =
                Arrays.stream(Arrays.copyOfRange(lineParts, startRange,lineParts.length - 1)).mapToInt(Integer::parseInt).toArray();


        if (!(variables.length > 0)) {
            throw new EmptyClauseException("One of the clauses is empty!");
        }

        for (int i = 0; i < variables.length; i++) {
            if (variables[i] == 0) {
                throw new ClauseContainsZeroException("One of the clauses contains a 0");
            }
        }


        return variables;

    }



}
