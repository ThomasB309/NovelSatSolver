package cas.thomas.Evaluation;

import cas.thomas.ConflictHandling.CDCLConflictHandler;
import cas.thomas.ConflictHandling.DPLLConflictHandler;
import cas.thomas.Exceptions.ClauseContainsZeroException;
import cas.thomas.Exceptions.ClauseNotTerminatedByZeroException;
import cas.thomas.Exceptions.EmptyClauseException;
import cas.thomas.Exceptions.IncorrectFirstLineException;
import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.Formulas.Formula;
import cas.thomas.Parsing.ClauseParser;
import cas.thomas.RestartHandling.NoRestartsSchedulingStrategy;
import cas.thomas.RestartHandling.ReluctantDoublingRestartStrategy;
import cas.thomas.SolutionChecker.SolutionCheckerFormula;
import cas.thomas.SolverAlgorithms.SolverAlgorithm;
import cas.thomas.SolverAlgorithms.mDPLL;
import cas.thomas.VariableSelection.VSIDS;
import cas.thomas.utils.Pair;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.core.Solver;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Evaluation {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("You only need to specify an input path!");
            System.exit(-1);
        }

        Path inputPath = Paths.get(new File(args[0]).toURI());
        Path inputPathCNF = Paths.get(new File(args[1]).toURI());
        int timeout = Integer.parseInt(args[2]);

        if (!Files.isDirectory(inputPath)) {
            System.err.println("The specified input path needs to be a directory!");
            System.exit(-1);
        }

        Statistics dpllNoRestartsFalse = new Statistics();
        Statistics dpllRestartsFalse = new Statistics();
        Statistics dpllNoRestarts = new Statistics();
        Statistics dpllRestarts = new Statistics();
        Statistics cdclNoRestartsFalse = new Statistics();
        Statistics cdclRestartsFalse = new Statistics();
        Statistics cdclNoRestarts = new Statistics();
        Statistics cdclRestarts = new Statistics();
        Statistics sat4j = new Statistics();

        dpllNoRestartsFalse.setName("DPLL_NR_F");
        dpllRestartsFalse.setName("DPLL_R_F");
        dpllNoRestarts.setName("DPLL_NR_T");
        dpllRestarts.setName("DPLL_R_T");
        cdclNoRestartsFalse.setName("CDCL_NR_F");
        cdclRestartsFalse.setName("CDCL_R_F");
        cdclNoRestarts.setName("CDCL_NR_T");
        cdclRestarts.setName("CDCL_R_T");

        Statistics dpllNoRestartsFalseCNF = new Statistics();
        Statistics dpllRestartsFalseCNF = new Statistics();
        Statistics dpllNoRestartsCNF = new Statistics();
        Statistics dpllRestartsCNF = new Statistics();
        Statistics cdclNoRestartsFalseCNF = new Statistics();
        Statistics cdclRestartsFalseCNF = new Statistics();
        Statistics cdclNoRestartsCNF = new Statistics();
        Statistics cdclRestartsCNF = new Statistics();

        dpllNoRestartsFalseCNF.setName("DPLL_NR_F_CNF");
        dpllRestartsFalseCNF.setName("DPLL_R_F_CNF");
        dpllNoRestartsCNF.setName("DPLL_NR_T_CNF");
        dpllRestartsCNF.setName("DPLL_R_T_CNF");
        cdclNoRestartsFalseCNF.setName("CDCL_NR_F_CNF");
        cdclRestartsFalseCNF.setName("CDCL_R_F_CNF");
        cdclNoRestartsCNF.setName("CDCL_NR_T_CNF");
        cdclRestartsCNF.setName("CDCL_R_T_CNF");



        sat4j.setName("SAT4J");

        int counter = 0;
        try {
            for (Path inputFile :
                    Files.list(inputPath).filter(path -> path.toString().endsWith(".cnf") || path.toString().endsWith(
                    ".txt") || path.toString().endsWith(".rcnf")).collect(Collectors.toList())) {


                //dpllNoRestartsFalse.add(solve(true, false, false, inputFile, timeout));
                //dpllRestartsFalse.add(solve(true, true, false, inputFile,timeout));
                dpllNoRestarts.add(solve(true, false, true, inputFile,timeout));
                //dpllRestarts.add(solve(true, true, true, inputFile,timeout));
                //cdclNoRestartsFalse.add(solve(false, false, false, inputFile, timeout));
                //cdclRestartsFalse.add(solve(false, true, false, inputFile,timeout));
                //cdclNoRestarts.add(solve(false, false, true, inputFile,timeout));
                //cdclRestarts.add(solve(false, true, true, inputFile,timeout));

                System.out.println(counter);
                counter++;


            }
        } catch (IOException e) {
            System.err.println("Something went wrong while reading the input files!");
            System.exit(-1);
        }

        counter = 0;
        try {
            for (Path inputFile :
                    Files.list(inputPathCNF).filter(path -> path.toString().endsWith(".cnf") || path.toString().endsWith(
                            ".txt") || path.toString().endsWith(".rcnf")).collect(Collectors.toList())) {


                /*dpllNoRestartsFalseCNF.add(solve(true, false, false, inputFile, timeout));
                dpllRestartsFalseCNF.add(solve(true, true, false, inputFile,timeout));
                dpllNoRestartsCNF.add(solve(true, false, true, inputFile,timeout));
                dpllRestartsCNF.add(solve(true, true, true, inputFile,timeout));*/
                /*cdclNoRestartsFalseCNF.add(solve(false, false, false, inputFile, timeout));
                cdclRestartsFalseCNF.add(solve(false, true, false, inputFile,timeout));
                cdclNoRestartsCNF.add(solve(false, false, true, inputFile,timeout));
                cdclRestartsCNF.add(solve(false, true, true, inputFile,timeout));*/
                sat4j.add(solveSat4J(inputFile, timeout));

                System.out.println(counter);
                counter++;

            }
        } catch (IOException e) {
            System.err.println("Something went wrong while reading the input files!");
            System.exit(-1);
        } catch (ContradictionException e) {
            e.printStackTrace();
        } catch (ParseFormatException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        printTable(dpllNoRestartsFalse,dpllNoRestarts,dpllRestartsFalse, dpllRestarts, cdclNoRestartsFalse,
                cdclNoRestarts, cdclRestarts, cdclRestartsFalse, dpllNoRestartsFalseCNF,dpllNoRestartsCNF,
                dpllRestartsFalseCNF, dpllRestartsCNF, cdclNoRestartsFalseCNF,
                cdclNoRestartsCNF, cdclRestartsCNF, cdclRestartsFalseCNF, sat4j);
    }


    private static SolverAlgorithm getSolver(boolean dpll, boolean restarts, boolean firstBranchingDecision,
                                             long timeout) {
        return new mDPLL(new VSIDS(), dpll ? new DPLLConflictHandler() : new CDCLConflictHandler(), restarts ?
                new ReluctantDoublingRestartStrategy(512) : new NoRestartsSchedulingStrategy(0), true,
                firstBranchingDecision, timeout);
    }

    private static Statistics solveSat4J(Path inputFile, int timeout) throws ContradictionException, IOException,
            ParseFormatException, TimeoutException {
        ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(timeout);
        Reader reader = new DimacsReader(solver);

        Statistics stats = new Statistics();

        long startTime = System.currentTimeMillis();

        try {

            IProblem problem = reader.parseInstance(inputFile.toString());

            problem.isSatisfiable();

            stats.setSolvedCounter(1);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
        } catch (ParseFormatException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        } catch (ContradictionException e) {
            System.out.println("Unsatisfiable (trivial)!");
        } catch (TimeoutException e) {
            stats.setTimeoutCounter(1);
        }

        Map<String, Number> statistics = solver.getStat();
        stats.setMilliseconds(System.currentTimeMillis() - startTime);
        stats.setDecisions(statistics.get("decisions").longValue());
        stats.setConflicts(statistics.get("conflicts").longValue());
        stats.setPropagations(statistics.get("propagations").longValue());

        return stats;


    }

    private static Statistics solve(boolean dpll, boolean restarts, boolean firstBranchingDecision, Path inputFile,
                             long timeout) {
        SolverAlgorithm solver = getSolver(dpll,restarts,firstBranchingDecision, timeout);

        Statistics statistics = new Statistics();

        String[] input = null;
        try {
            input = Files.readAllLines(inputFile,
                    StandardCharsets.UTF_8).toArray(new String[0]);

            long startTime = System.currentTimeMillis();
            Pair<Formula, SolutionCheckerFormula> formulaPair = new ClauseParser().parseInput(input);

            Formula formula = formulaPair.getFirstPairPart();
            SolutionCheckerFormula solutionCheckerFormula = formulaPair.getSecondPairPart();

            String isSatsifiable = solver.solve(formula);
            long endTime = System.currentTimeMillis();

            if (isSatsifiable.equals("SATISFIABLE") || isSatsifiable.equals("UNSATISFIABLE")) {
                statistics.setSolvedCounter(1);
            } else {
                statistics.setTimeoutCounter(1);
            }

            long solvingTime = endTime - startTime;

            statistics.add(solver.getStatistics());
            statistics.setMilliseconds(solvingTime);

            //System.out.print(isSatsifiable + " " + statistics + "   ");

        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (EmptyClauseException e) {
            e.printStackTrace();
        } catch (ClauseContainsZeroException e) {
            e.printStackTrace();
        } catch (UnitLiteralConflictException e) {
            e.printStackTrace();
        } catch (IncorrectFirstLineException e) {
            e.printStackTrace();
        } catch (ClauseNotTerminatedByZeroException e) {
            e.printStackTrace();
        }

        return statistics;

    }

    private static void printTable(Statistics ... stats) {
        String table =
                "\\begin{table}[htb]\n" +
                        "\\centering\n" +
                        "\\caption{Caption}\n" +
                        "\\label{tab:example}\n" +
                        "\\begin{tabular}{|c|c|c|c|c|c|c|}\n" +
                        "\\hline\n" +
                        "Solver & Solving time & Solved Instances & Timeouts & Decisions (A) & Propagations (A) & " +
                        "Conflicts (A)\\\\ " +
                        "\n" +
                        "\\hline\n";

        for (int i = 0; i < stats.length; i++) {
            Statistics stat = stats[i];

            table += String.format(stat.getName() + " & " + stat.getMilliseconds() + " & " + stat.getSolvedCounter() + " & " + stat.getTimeoutCounter() + " & %.2f" +
                    " & %.2f" + " & %.2f " + "\\\\ \n", stat.getDecisionsAverage(),stat.getPropagationsAverage(),
                    stat.getConflictsAverage()) + "\\hline\n";
        }

        table += "\\end{tabular}\n" +
                "\\end{table}";

        System.out.println(table);
    }

}
