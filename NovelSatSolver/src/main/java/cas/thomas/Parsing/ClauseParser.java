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
import cas.thomas.Formulas.Literal;
import cas.thomas.Formulas.Variable;
import cas.thomas.SolutionChecker.SolutionCheckerConjunctiveFormula;
import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerFormula;
import cas.thomas.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClauseParser {

    public Pair<Formula, SolutionCheckerFormula> parseInput(String[] lines) throws IncorrectFirstLineException,
            ClauseNotTerminatedByZeroException,
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
        SolutionCheckerConstraint[] solutionCheckerConstraints = new SolutionCheckerConstraint[numberOfClauses];
        List<Literal> listOfUnitVariables = new ArrayList<>();
        Map<Integer, Variable> variableMap = new HashMap<>();

        int clausecounter = 0;
        for (int i = indexOfFirstLine + 1; i < indexOfFirstLine + numberOfClauses + 1; i++) {

            String line = lines[i];

            if (line.startsWith("c")) {
                continue;
            }

            Constraint nextConstraint = parseClause(lines[i], listOfUnitVariables, variableMap);

            if (nextConstraint != null) {
                constraints[clausecounter] = nextConstraint;
                solutionCheckerConstraints[clausecounter] = nextConstraint.getSolutionCheckerConstraint();
            }

            clausecounter++;
        }

        if (constraints.length != numberOfClauses) {
            throw new IncorrectFirstLineException("The given amount of clauses doesn't match the specified amount of " +
                    "clauses!");
        }


        return new Pair<Formula, SolutionCheckerFormula>(new ConjunctiveFormula(variableMap.values().toArray(Variable[]::new), listOfUnitVariables, constraints), new SolutionCheckerConjunctiveFormula(solutionCheckerConstraints));


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

    private Constraint parseClause(String line,
                                   List<Literal> listOfUnitLiterals, Map<Integer, Variable> variableMap) throws ClauseNotTerminatedByZeroException,
            EmptyClauseException, ClauseContainsZeroException {

        try {

            String[] input = Arrays.stream(line.split("\\s+")).filter(part -> !part.equals("")).toArray(String[]::new);

            String identifier = input[0];

            boolean hasIdentifier = false;

            if (identifier.equals("AMO")) {
                hasIdentifier = true;
            }

            int[] intVariables = checkAndParseInputVariables(input, hasIdentifier);

            List<Literal> literals = new ArrayList<>();


            for (int i = 0; i < intVariables.length; i++) {
                int variable = Math.abs(intVariables[i]);
                boolean truthValue = intVariables[i] < 0 ? false : true;
                if (!variableMap.containsKey(variable)) {
                    variableMap.put(variable, new Variable(variable));
                }
                literals.add(new Literal(variableMap.get(variable), truthValue));

                if (truthValue) {
                    variableMap.get(variable).addPositiveOccurence();
                } else {
                    variableMap.get(variable).addNegativeOccurence();
                }
            }


            if (literals.size() == 1) {
                listOfUnitLiterals.add(literals.get(0));
            }

            if (identifier.equals("AMO")) {
                return new AMOConstraint(literals.toArray(Literal[]::new));
            } else {
                return new DisjunctiveConstraint(literals.toArray(Literal[]::new));
            }

        } catch(NumberFormatException e) {
            throw new NumberFormatException("Bad clause variables!");
        }
    }

    private AMOConstraint parseAMOConstraint(String[] lineParts) throws EmptyClauseException, ClauseContainsZeroException, ClauseNotTerminatedByZeroException {
        int[] variables = checkAndParseInputVariables(lineParts, true);
        return null;
    }

    private int[] checkAndParseInputVariables(String[] lineParts, boolean hasIdentifier) throws
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
