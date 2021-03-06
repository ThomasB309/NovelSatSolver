package cas.thomas.BenchmarkGeneration;

import cas.thomas.Exceptions.SolverTimeoutException;
import cas.thomas.Exceptions.UnitLiteralConflictException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

public class PhaseTransitionInputParser {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Choose a phase transition!");
            System.exit(-1);
        }

        try {
            switch (args[0]) {
                case "A":
                    parseAndExecuteAMOPhaseTransition(args);
                    break;
                case "D":
                    parseAndExecuteDNFPhaseTransition(args);
                    break;
                case "AC":
                    parseAndExecuteAMOPhaseTransitionWithAConstantNumberOfClauses(args);
                    break;
                case "DC":
                    parseAndExecuteDNFPhaseTransitionWithAConstantNumberOfClauses(args);
                    break;
                case "AD":
                    parseAndExecuteAMOPhaseTransitionWithAConstantNumberOfDNFConstraints(args);
                    break;
                case "ADC":
                    parseAndExecuteAMOPhaseTransitionWithAConstantNumberOfDNFConstraintsAndClauses(args);
                    break;
                case "G":
                    generateBenchmarks(args);
                    break;
                default:
                    System.err.println("Choose a valid phase transition!");
                    System.exit(-1);
            }
        } catch (SolverTimeoutException e) {
            e.printStackTrace();
        }
    }

    private static void parseAndExecuteAMOPhaseTransition(String[] args) throws SolverTimeoutException {
        if (args.length != 4) {
            System.err.println("You need three inputs: 1. Number of variables, 2. Length of the AMO constraints, 3. " +
                    "The step size");
            System.exit(-1);
        }

        try {

            int variables = Integer.valueOf(args[1]);
            int amoLength = Integer.valueOf(args[2]);
            int stepSize = Integer.valueOf(args[3]);

            if (variables <= 0 || amoLength <= 0 || stepSize <= 0) {
                System.err.println("Integers equal or less than 0 are not allowed");
                System.exit(-1);
            }

            PhaseTransition.createPhaseTransitionPlotForAMOConstraints(variables, amoLength, stepSize);

        } catch (NumberFormatException e) {
            System.err.println("One of the inputs is not a valid integer!");
            System.exit(-1);
        } catch (UnitLiteralConflictException e) {
        } catch (TimeoutException e) {
        }
    }

    private static void parseAndExecuteDNFPhaseTransition(String[] args) throws SolverTimeoutException {

        if (args.length != 6) {
            System.err.println("You need five inputs: 1. \"T\" for a constant term count or \"L\" for a constant " +
                    "term length" +
                    " 2" +
                    ". Number of variables, 3. The number of" +
                    " terms, 4. " +
                    "The term length, 5. The step size");
            System.exit(-1);
        }

        try {

            int variables = Integer.valueOf(args[2]);
            int termCount = Integer.valueOf(args[3]);
            int termLength = Integer.valueOf(args[4]);
            int stepSize = Integer.valueOf(args[5]);

            if (variables <= 0 || termCount <= 0 || termLength <= 0 || stepSize <= 0) {
                System.err.println("Integers equal or less than 0 are not allowed");
                System.exit(-1);
            }

            if (args[1].equals("T")) {
                PhaseTransition.createPhaseTransitionPlotForDNFConstraintsWithConstantTermCount(variables, termCount,
                        termLength, stepSize);
            } else if (args[1].equals("L")) {
                PhaseTransition.createPhaseTransitionPlotForDNFConstraintsWithConstantTermLength(variables, termCount
                        , termLength, stepSize);
            } else {
                System.err.println("The first parameter is not valid");
                System.exit(-1);
            }

        } catch (NumberFormatException e) {
            System.err.println("One of the inputs is not a valid integer!");
            System.exit(-1);
        } catch (TimeoutException e) {
        }

    }


    private static void parseAndExecuteAMOPhaseTransitionWithAConstantNumberOfClauses(String[] args) throws SolverTimeoutException {

        if (args.length != 7) {
            System.err.println("You need 6 inputs: 1. Number of variables, 2. Length of the AMO constraints, 3. " +
                    "The step size for the amo constraints, 4. The maximum amount of clauses, 5. The size of the " +
                    "clauses, 6. The step size for the clauses");
            System.exit(-1);
        }

        try {

            int variables = Integer.valueOf(args[1]);
            int amoLength = Integer.valueOf(args[2]);
            int stepSize = Integer.valueOf(args[3]);
            int cls = Integer.valueOf(args[4]);
            int clsLength = Integer.valueOf(args[5]);
            int stepSizeCls = Integer.valueOf(args[6]);

            if (variables <= 0 || amoLength <= 0 || stepSize <= 0 || cls <= 0 || clsLength <= 0 || stepSizeCls <= 0) {
                System.err.println("Integers equal or less than 0 are not allowed");
                System.exit(-1);
            }

            PhaseTransition.createPhaseTransitionPlotForAMOConstraintsWithClauses(variables, cls, clsLength,
                    amoLength, stepSize, stepSizeCls);

        } catch (NumberFormatException e) {
            System.err.println("One of the inputs is not a valid integer!");
            System.exit(-1);
        } catch (UnitLiteralConflictException e) {
        } catch (TimeoutException e) {
        }

    }

    private static void parseAndExecuteDNFPhaseTransitionWithAConstantNumberOfClauses(String[] args) throws SolverTimeoutException {


        if (args.length != 8) {
            System.err.println("You need 7 inputs: 1. Number of variables, 2. Number of terms, 3. The length of the " +
                    "terms, 4. The step size for the DNF constraints, 5. The maximum amount of clauses, 6. The " +
                    "length of the clauses, 7. The step size for the clauses");
            System.exit(-1);
        }

        try {

            int variables = Integer.valueOf(args[1]);
            int terms = Integer.valueOf(args[2]);
            int termLength = Integer.valueOf(args[3]);
            int stepSize = Integer.valueOf(args[4]);
            int cls = Integer.valueOf(args[5]);
            int clsLength = Integer.valueOf(args[6]);
            int stepSizeCls = Integer.valueOf(args[7]);

            if (variables <= 0 || terms <= 0 || termLength <= 0 || stepSize <= 0 || cls <= 0 || clsLength <= 0 || stepSizeCls <= 0) {
                System.err.println("Integers equal or less than 0 are not allowed");
                System.exit(-1);
            }

            PhaseTransition.createPhaseTransitionPlotForDNFConstraintsWithClauses(variables, cls, clsLength, terms,
                    termLength, stepSize, stepSizeCls);

        } catch (NumberFormatException e) {
            System.err.println("One of the inputs is not a valid integer!");
            System.exit(-1);
        } catch (UnitLiteralConflictException e) {
        } catch (TimeoutException e) {
        }

    }

    private static void parseAndExecuteAMOPhaseTransitionWithAConstantNumberOfDNFConstraints(String[] args) throws SolverTimeoutException {

        if (args.length != 8) {
            System.err.println("You need 7 inputs: 1. Number of variables, 2. Length of the AMO constraints, 3. The " +
                    "step size of the AMO constraints, 4. The maximum number of DNF constraints, 5. The number of " +
                    "terms, 6. The length of the terms, 7. The step size for the DNF constraints");
            System.exit(-1);
        }

        try {

            int variables = Integer.valueOf(args[1]);
            int amoLength = Integer.valueOf(args[2]);
            int stepSize = Integer.valueOf(args[3]);
            int dnf = Integer.valueOf(args[4]);
            int terms = Integer.valueOf(args[5]);
            int termLength = Integer.valueOf(args[6]);
            int stepSizeDNF = Integer.valueOf(args[7]);

            if (variables <= 0 || terms <= 0 || termLength <= 0 || stepSize <= 0 || amoLength <= 0 || stepSizeDNF <= 0) {
                System.err.println("Integers equal or less than 0 are not allowed");
                System.exit(-1);
            }

            PhaseTransition.createPlotsForAMOPhaseTransitionWithAConstantNumberOfDNFConstraints(variables, dnf, terms
                    , termLength, amoLength, stepSize, stepSizeDNF);

        } catch (NumberFormatException e) {
            System.err.println("One of the inputs is not a valid integer!");
            System.exit(-1);
        } catch (UnitLiteralConflictException e) {
        } catch (TimeoutException e) {
        }


    }

    private static void parseAndExecuteAMOPhaseTransitionWithAConstantNumberOfDNFConstraintsAndClauses(String[] args) throws SolverTimeoutException {

        if (args.length != 10) {
            System.err.println("You need 7 inputs: 1. Number of variables, 2. Length of the AMO constraints, 3. The " +
                    "step size of the AMO constraints, 4. The number of iterations, 5. The clause length, 6. The step" +
                    " size of the clauses, 7. The number of terms, 8. The length of the terms, 9. The DNF step size" +
                    " ");
            System.exit(-1);
        }

        try {

            int variables = Integer.valueOf(args[1]);
            int amoLength = Integer.valueOf(args[2]);
            int stepSize = Integer.valueOf(args[3]);
            int iterations = Integer.valueOf(args[4]);
            int clauseLength = Integer.valueOf(args[5]);
            int clauseStepSize = Integer.valueOf(args[6]);
            int terms = Integer.valueOf(args[7]);
            int termLength = Integer.valueOf(args[8]);
            int stepSizeDNF = Integer.valueOf(args[9]);

            if (variables <= 0 || terms <= 0 || termLength <= 0 || stepSize <= 0 || amoLength <= 0 || stepSizeDNF <= 0 || iterations <= 0 || clauseLength <= 0 || clauseStepSize <= 0) {
                System.err.println("Integers equal or less than 0 are not allowed");
                System.exit(-1);
            }

            PhaseTransition.createPhaseTransitionPlotForAMOConstraintsWithAConstantNumberOfClausesAndDNFConstraints(variables, amoLength, stepSize, iterations, clauseLength, clauseStepSize, terms, termLength, stepSizeDNF);

        } catch (NumberFormatException e) {
            System.err.println("One of the inputs is not a valid integer!");
            System.exit(-1);
        } catch (TimeoutException e) {
        }

    }

    private static void generateBenchmarks(String[] args) throws SolverTimeoutException {

        if (args.length != 11) {
            System.err.println("You need 10 inputs: 1. Number of variables, 2. Number of clauses, 3. The clause " +
                    "length, 4. The number of AMO constraints, 5. The AMO length, 6. Number of DNF constraints, 7. " +
                    "The number of terms, 8. The length of the terms, 9. The number of benchmarks," +
                    " 10. The file path ");
            System.exit(-1);
        }

        try {

            int variables = Integer.valueOf(args[1]);
            int clauseCount = Integer.valueOf(args[2]);
            int clauseLength = Integer.valueOf(args[3]);
            int amoCount = Integer.valueOf(args[4]);
            int amoLength = Integer.valueOf(args[5]);
            int dnfCount = Integer.valueOf(args[6]);
            int termCount = Integer.valueOf(args[7]);
            int termLength = Integer.valueOf(args[8]);
            int numberOfBenchmarks = Integer.valueOf(args[9]);
            Path filePath = Paths.get(args[10]);

            if (variables <= 0 || termCount < 0 || termLength < 0 || clauseCount < 0 || amoLength < 0 || amoCount < 0 || dnfCount < 0|| clauseLength < 0 || numberOfBenchmarks <= 0 ||filePath == null) {
                System.err.println("Integers equal or less than 0 are not allowed");
                System.exit(-1);
            }

            PhaseTransition.createPhaseTransitionBenchmarks(variables, clauseCount, clauseLength, amoCount, amoLength,
                    dnfCount, termCount, termLength, numberOfBenchmarks,filePath);

        } catch (NumberFormatException e) {
            System.err.println("One of the inputs is not a valid integer!");
            System.exit(-1);
        } catch (TimeoutException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
