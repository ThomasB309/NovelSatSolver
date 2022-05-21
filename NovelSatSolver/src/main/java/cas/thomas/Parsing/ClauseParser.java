package cas.thomas.Parsing;

import cas.thomas.Evaluation.ConstraintStatistics;
import cas.thomas.Exceptions.ClauseContainsZeroException;
import cas.thomas.Exceptions.ClauseNotTerminatedByZeroException;
import cas.thomas.Exceptions.EmptyClauseException;
import cas.thomas.Exceptions.IncorrectFirstLineException;
import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.Formulas.AMOConstraint;
import cas.thomas.Formulas.BinaryDNFConstraint;
import cas.thomas.Formulas.BinaryDisjunctiveConstraint;
import cas.thomas.Formulas.Constraint;
import cas.thomas.Formulas.DNFConstraint;
import cas.thomas.Formulas.DisjunctiveConstraint;
import cas.thomas.Formulas.Formula;
import cas.thomas.SolutionChecker.SolutionCheckerConjunctiveFormula;
import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerFormula;
import cas.thomas.utils.IntegerArrayQueue;
import cas.thomas.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ClauseParser {

    public Pair<Formula, SolutionCheckerFormula> parseInput(String[] lines) throws IncorrectFirstLineException,
            ClauseNotTerminatedByZeroException,
            EmptyClauseException, ClauseContainsZeroException, UnitLiteralConflictException {

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

        numberOfVariables++;
        Constraint[] reasonConstraints = new Constraint[numberOfVariables];
        double[] variableOccurences = new double[numberOfVariables];
        SolutionCheckerConstraint[] solutionCheckerConstraints = new SolutionCheckerConstraint[numberOfClauses];
        List<Constraint>[] positivelyWatchedDisjunctiveConstraints = (ArrayList<Constraint>[]) new ArrayList[numberOfVariables];
        List<Constraint>[] negativelyWatchedDisjunctiveConstraints =
                (ArrayList<Constraint>[]) new ArrayList[numberOfVariables];

        List<Constraint>[] positivelyWatchedAMOConstraints =
                (ArrayList<Constraint>[]) new ArrayList[numberOfVariables];

        List<Constraint>[] negativelyWatchedAMOConstraints =
                (ArrayList<Constraint>[]) new ArrayList[numberOfVariables];
        List<Constraint>[] positivelyWatchedDNFConstraints = (ArrayList<Constraint>[]) new ArrayList[numberOfVariables];
        List<Constraint>[] negativelyWatchedDNFConstraints = (ArrayList<Constraint>[]) new ArrayList[numberOfVariables];
        List<Integer> unitLiteralsBeforePropagation = new ArrayList<>();


        initializeWatchedLists(numberOfVariables, positivelyWatchedDisjunctiveConstraints, negativelyWatchedDisjunctiveConstraints, positivelyWatchedAMOConstraints, negativelyWatchedAMOConstraints, positivelyWatchedDNFConstraints, negativelyWatchedDNFConstraints);

        IntegerArrayQueue listOfUnitVariables = new IntegerArrayQueue(numberOfVariables + 1);
        int[] unitLiteralState = new int[numberOfVariables];

        int clausecounter = 0;
        for (int i = indexOfFirstLine + 1; i < indexOfFirstLine + numberOfClauses + 1; i++) {

            String line = lines[i];

            if (line.startsWith("c")) {
                continue;
            }

            Constraint nextConstraint = parseClause(lines[i], listOfUnitVariables, unitLiteralState,
                    positivelyWatchedDisjunctiveConstraints, negativelyWatchedDisjunctiveConstraints,
                    positivelyWatchedAMOConstraints, negativelyWatchedAMOConstraints, positivelyWatchedDNFConstraints
                    , negativelyWatchedDNFConstraints, numberOfVariables, unitLiteralsBeforePropagation, reasonConstraints);

            if (nextConstraint != null) {
                solutionCheckerConstraints[clausecounter] = nextConstraint.getSolutionCheckerConstraint();
                nextConstraint.addVariableOccurenceCount(variableOccurences);
            }

            clausecounter++;
        }


        return new Pair<>(new Formula(numberOfVariables, reasonConstraints, variableOccurences, positivelyWatchedDisjunctiveConstraints,
                negativelyWatchedDisjunctiveConstraints
                , positivelyWatchedAMOConstraints, negativelyWatchedAMOConstraints, positivelyWatchedDNFConstraints,
                negativelyWatchedDNFConstraints, unitLiteralsBeforePropagation),
                new SolutionCheckerConjunctiveFormula(solutionCheckerConstraints, numberOfVariables - 1));


    }

    public void getBenchmarkStats(String[] lines, ConstraintStatistics statistics) throws EmptyClauseException,
            ClauseContainsZeroException, UnitLiteralConflictException, IncorrectFirstLineException, ClauseNotTerminatedByZeroException {

        Pair<Formula, SolutionCheckerFormula> formulaPair = parseInput(lines);

        SolutionCheckerFormula solutionCheckerFormula = formulaPair.getSecondPairPart();

        solutionCheckerFormula.addStatistics(statistics);

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

    private void initializeWatchedLists(int numberOfVariables, List<Constraint>[] positivelyWatchedDisjunctiveConstraints, List<Constraint>[] negativelyWatchedDisjunctiveConstraints, List<Constraint>[] positivelyWatchedAMOConstraints, List<Constraint>[] negativelyWatchedAMOConstraints, List<Constraint>[] positivelyWatchedDNFConstraints, List<Constraint>[] negativelyWatchedDNFConstraints) {
        for (int i = 0; i < numberOfVariables; i++) {
            positivelyWatchedDisjunctiveConstraints[i] = new ArrayList<>();
            negativelyWatchedDisjunctiveConstraints[i] = new ArrayList<>();
            positivelyWatchedAMOConstraints[i] = new ArrayList<>();
            negativelyWatchedAMOConstraints[i] = new ArrayList<>();
            positivelyWatchedDNFConstraints[i] = new ArrayList<>();
            negativelyWatchedDNFConstraints[i] = new ArrayList<>();
        }
    }

    private Constraint parseClause(String line,
                                   IntegerArrayQueue listOfUnitLiterals, int[] unitLiteralState,
                                   List<Constraint>[] positivelyWatchedList,
                                   List<Constraint>[] negativelyWatchedList, List<Constraint>[] positivelyWatchedAMOList
            , List<Constraint>[] negativelyWatchedAMOList, List<Constraint>[] positivelyWatchedDNFConstraints,
                                   List<Constraint>[] negativelyWatchedDNFConstraints, int numberOfVariables,
                                   List<Integer> unitLiteralsBeforePropagation, Constraint[] reasonConstraints) throws ClauseNotTerminatedByZeroException,
            EmptyClauseException, ClauseContainsZeroException {

        try {

            String[] input = Arrays.stream(line.split("\\s+")).filter(part -> !part.equals("")).toArray(String[]::new);

            String identifier = input[0];

            boolean hasIdentifier = false;
            boolean isDNF = false;

            if (identifier.equals("AMO") || identifier.equals("DNF")) {
                hasIdentifier = true;

                if (identifier.equals("DNF")) {
                    isDNF = true;
                }
            }

            int[] intVariables = checkAndParseInputVariables(input, hasIdentifier, isDNF, numberOfVariables);

            if (identifier.equals("AMO")) {
                return new AMOConstraint(intVariables, positivelyWatchedAMOList, negativelyWatchedAMOList);
            } else if (identifier.equals("DNF")) {
                return parseDNFConstraint(line, listOfUnitLiterals, unitLiteralState, positivelyWatchedDNFConstraints, negativelyWatchedDNFConstraints, numberOfVariables, unitLiteralsBeforePropagation, reasonConstraints, intVariables);

            } else {
                return parseDisjunctiveConstraint(listOfUnitLiterals, unitLiteralState, positivelyWatchedList, negativelyWatchedList, unitLiteralsBeforePropagation, reasonConstraints, intVariables);
            }

        } catch (NumberFormatException e) {
            throw new NumberFormatException("Bad clause variables!");
        }
    }

    private int[] checkAndParseInputVariables(String[] lineParts, boolean hasIdentifier, boolean isDNF,
                                              int numberOfVariables) throws
            ClauseNotTerminatedByZeroException, EmptyClauseException, ClauseContainsZeroException {

        if (!lineParts[lineParts.length - 1].equals("0")) {
            throw new ClauseNotTerminatedByZeroException("One of the clauses was not terminated by a zero!");
        }

        int startRange = hasIdentifier ? 1 : 0;

        int[] variables =
                Arrays.stream(Arrays.copyOfRange(lineParts, startRange, lineParts.length - 1)).mapToInt(Integer::parseInt).toArray();


        if (!(variables.length > 0)) {
            throw new EmptyClauseException("One of the clauses is empty!");
        }

        if (!isDNF) {
            for (int i = 0; i < variables.length; i++) {
                if (variables[i] == 0) {
                    throw new ClauseContainsZeroException("One of the clauses contains a 0");
                }

                assert(numberOfVariables > Math.abs(variables[i]));
            }
        }


        return variables;

    }

    private DNFConstraint parseDNFConstraint(String line, IntegerArrayQueue listOfUnitLiterals, int[] unitLiteralState, List<Constraint>[] positivelyWatchedDNFConstraints, List<Constraint>[] negativelyWatchedDNFConstraints, int numberOfVariables, List<Integer> unitLiteralsBeforePropagation, Constraint[] reasonConstraints, int[] intVariables) throws EmptyClauseException {
        DNFConstraint dnfConstraint = parseDNFConstraint(intVariables, positivelyWatchedDNFConstraints,
                negativelyWatchedDNFConstraints, listOfUnitLiterals, unitLiteralState, line, numberOfVariables);

        Set<Integer> unitLiterals = dnfConstraint.getUnitLiteralsNeededBeforePropagation();

        addUnitLiterals(listOfUnitLiterals, unitLiteralState, unitLiteralsBeforePropagation, reasonConstraints, dnfConstraint, unitLiterals);

        return dnfConstraint;
    }

    private DisjunctiveConstraint parseDisjunctiveConstraint(IntegerArrayQueue listOfUnitLiterals, int[] unitLiteralState, List<Constraint>[] positivelyWatchedList, List<Constraint>[] negativelyWatchedList, List<Integer> unitLiteralsBeforePropagation, Constraint[] reasonConstraints, int[] intVariables) throws EmptyClauseException {
        DisjunctiveConstraint disjunctiveConstraint = intVariables.length == 2 ?
                new BinaryDisjunctiveConstraint(intVariables,
                        positivelyWatchedList,
                        negativelyWatchedList) : new DisjunctiveConstraint(intVariables, positivelyWatchedList,
                negativelyWatchedList);

        Set<Integer> unitLiterals = disjunctiveConstraint.getUnitLiteralsNeededBeforePropagation();

        addUnitLiterals(listOfUnitLiterals, unitLiteralState, unitLiteralsBeforePropagation,
                reasonConstraints, disjunctiveConstraint, unitLiterals);

        return disjunctiveConstraint;
    }

    private DNFConstraint parseDNFConstraint(int[] variables, List<Constraint>[] positivelyWatchedDNFConstraints,
                                             List<Constraint>[] negativelyWatchedDNFConstraints,
                                             IntegerArrayQueue listOfUnitLiterals, int[] unitLiteralState,
                                             String line, int numberOfVariables) {

        int termCount = 0;

        for (int i = 0; i < variables.length - 1; i++) {
            if (variables[i] == 0) {
                termCount++;
            }

            assert (numberOfVariables > Math.abs(variables[i]));
        }

        termCount++;

        int[][] solutionCheckerTerms =  new int[termCount][];
        int[][] terms = parseTerms(variables, termCount,solutionCheckerTerms);


        assert (terms.length > 0);
        for (int i = 0; i < terms.length; i++) {
            assert (terms[i].length > 0);
        }

        return terms.length == 2 ? new BinaryDNFConstraint(terms, positivelyWatchedDNFConstraints,
                negativelyWatchedDNFConstraints, new int[numberOfVariables], solutionCheckerTerms) :
                new DNFConstraint(terms,
                        positivelyWatchedDNFConstraints,
                        negativelyWatchedDNFConstraints, numberOfVariables, solutionCheckerTerms);
    }

    private void addUnitLiterals(IntegerArrayQueue listOfUnitLiterals, int[] unitLiteralState,
                                 List<Integer> unitLiteralsBeforePropagation, Constraint[] reasonConstraints,
                                 Constraint constraint, Set<Integer> unitLiterals) throws EmptyClauseException {
        for (Integer literal : unitLiterals) {
            int literalAbsoluteValue = Math.abs(literal);

            if (unitLiteralState[literalAbsoluteValue] * literal < 0) {
                throw new EmptyClauseException("The formula is not satisfiable");
            } else if (unitLiteralState[literalAbsoluteValue] == 0) {
                listOfUnitLiterals.offer(literal);
                unitLiteralsBeforePropagation.add(literal);
                unitLiteralState[Math.abs(literal)] = literal < 0 ? -1 : 1;
                if (reasonConstraints[Math.abs(literal)] == null) {
                    reasonConstraints[Math.abs(literal)] = constraint;
                }
            }
        }
    }

    private int[][] parseTerms(int[] variables, int termCount, int[][] solutionCheckerTerms) {
        int[][] terms = new int[termCount][];
        int start = 0;
        int end = 0;
        int counter = 0;
        for (int i = 0; i < variables.length - 1; i++) {
            int currentLiteral = variables[i];
            if (currentLiteral == 0) {
                terms[counter] = Arrays.copyOfRange(variables, start, end);
                solutionCheckerTerms[counter] = Arrays.copyOfRange(variables,start,end);
                counter++;
                start = ++end;
            } else {
                end++;
            }

        }

        terms[terms.length - 1] = Arrays.copyOfRange(variables, start, end);
        solutionCheckerTerms[solutionCheckerTerms.length - 1] = Arrays.copyOfRange(variables, start,end);
        return terms;
    }

    private void addVariableOccurenceCount(int[] literals, double[] variableOccurences) {
        for (int i = 0; i < literals.length; i++) {
            variableOccurences[Math.abs(literals[i])] += 1;
        }
    }


}
