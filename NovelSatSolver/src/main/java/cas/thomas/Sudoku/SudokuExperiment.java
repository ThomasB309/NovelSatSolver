package cas.thomas.Sudoku;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cas.thomas.ConflictHandling.CDCLConflictHandler;
import cas.thomas.ConflictHandling.DPLLConflictHandler;
import cas.thomas.Evaluation.ConstraintStatistics;
import cas.thomas.Evaluation.Evaluation;
import cas.thomas.Evaluation.Statistics;
import cas.thomas.RestartHandling.NoRestartsSchedulingStrategy;
import cas.thomas.RestartHandling.ReluctantDoublingRestartStrategy;
import cas.thomas.SolutionChecker.SolutionCheckerAMOConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerConjunctiveFormula;
import cas.thomas.SolutionChecker.SolutionCheckerConstraint;
import cas.thomas.SolutionChecker.SolutionCheckerDisjunctiveConstraint;
import cas.thomas.SolverAlgorithms.SolverAlgorithm;
import cas.thomas.SolverAlgorithms.mDPLL;
import cas.thomas.VariableSelection.VSIDS;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import cas.thomas.Exceptions.SolverTimeoutException;
import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.SolverAlgorithms.IncrementalCdclSolver;
import cas.thomas.SolverAlgorithms.IncrementalSatSolver;

public class SudokuExperiment {

	/*public static class Statistics {
		public long milliseconds;
		public long decisions;
		public long propagations;
		public long conflicts;

		@Override
		public String toString() {
			return "Statistics [milliseconds=" + milliseconds + ", decisions=" + decisions + ", propagations="
					+ propagations + ", conflicts=" + conflicts + "]";
		}
	}*/

	public static class SudokuProblem {
		public int size;
		public int[][] values;

		public SudokuProblem(int size) {
			this.size = size;
			int rows = size * size;
			values = new int[rows][];
			for (int i = 0; i < rows; i++) {
				values[i] = new int[rows];
			}

		}

		public void print() {
			for (int[] row : values) {
				System.out.println(Arrays.toString(row));
			}
		}
	}

	public static class CnfWithAMO {
		public List<int[]> clauses;
		public List<int[]> amos;

		public CnfWithAMO() {
			clauses = new ArrayList<>();
			amos = new ArrayList<>();
		}
	}

	public static void main(String[] args) throws IOException {
		Statistics dpllNoRestartsFalse = new Statistics();
		Statistics dpllRestartsFalse = new Statistics();
		Statistics cdclRestartsFalse = new Statistics();
		Statistics sat4j = new Statistics();

		if (args.length != 2) {
			System.err.println("You need to specify the input path and the timeout!");
			System.exit(-1);
		}


		dpllNoRestartsFalse.setName("DPLL\\_NR\\_F");
		dpllRestartsFalse.setName("DPLL\\_R\\_F");
		cdclRestartsFalse.setName("CDCL\\_R\\_F");
		sat4j.setName("SAT4J");

		int timeout = Integer.parseInt(args[1]);

		for (String file : listFilesUsingFileWalk(args[0], 1)) {
			SudokuProblem problem = readSudokuProblem(file);
			dpllNoRestartsFalse.add(solveWithDinoSat(problem, true, false, false, timeout));
			dpllRestartsFalse.add(solveWithDinoSat(problem, true, true, false, timeout));
			cdclRestartsFalse.add(solveWithDinoSat(problem, false, true, false, timeout));
			sat4j.add(solveWithSat4j(problem, timeout));
			System.out.println(file);
		}

		Evaluation.printTable(dpllNoRestartsFalse, dpllRestartsFalse, cdclRestartsFalse, sat4j);

	}

	private static void convertSudoToDIMACSFiles(String inputFile, String outputPath) throws IOException {
		SudokuProblem problem = readSudokuProblem(inputFile);

		CnfWithAMO formula = convertSudokuProblemToCnfWithAMO(problem);

		int maxVariable = 0;

		if (!Files.exists(Paths.get(outputPath, "rcnf"))) {
			Files.createDirectory(Paths.get(outputPath,"rcnf"));
		}

		if (!Files.exists(Paths.get(outputPath, "cnf"))) {
			Files.createDirectory(Paths.get(outputPath,"cnf"));
		}

		SolutionCheckerConstraint[] constraints =
				new SolutionCheckerConstraint[formula.clauses.size() + formula.amos.size()];

		int counter = 0;
		for (int[] clause : formula.clauses) {
			for (int i = 0; i < clause.length; i++) {
				maxVariable = Math.max(maxVariable, Math.abs(clause[i]));
			}

			constraints[counter] = new SolutionCheckerDisjunctiveConstraint(clause);
			counter++;
		}

		for (int[] amo : formula.amos) {
			for (int i = 0; i < amo.length; i++) {
				maxVariable = Math.max(maxVariable, Math.abs(amo[i]));
			}

			constraints[counter] = new SolutionCheckerAMOConstraint(amo);
			counter++;
		}

		SolutionCheckerConjunctiveFormula cnf = new SolutionCheckerConjunctiveFormula(constraints, maxVariable);

		cnf.toDimacsFile(Paths.get(outputPath, "rcnf", new File(inputFile).getName()));
		cnf.toDimacsCNFFile(Paths.get(outputPath, "cnf", new File(inputFile).getName()));



	}

	private static IncrementalCdclSolver getSolver(boolean dpll, boolean restarts, boolean firstBranchingDecision,
											 long timeout) {
		return new IncrementalCdclSolver(new VSIDS(), dpll ? new DPLLConflictHandler() : new CDCLConflictHandler(), restarts ?
				new ReluctantDoublingRestartStrategy(512) : new NoRestartsSchedulingStrategy(0), true,
				firstBranchingDecision, timeout);
	}
	
	public static Statistics solveWithDinoSat(SudokuProblem problem, boolean dpll, boolean restarts, boolean firstBranchingDecision,
											  long timeout) {
		Statistics stats = new Statistics();
		CnfWithAMO formula = convertSudokuProblemToCnfWithAMO(problem);
		IncrementalCdclSolver solver = getSolver(dpll, restarts, firstBranchingDecision, timeout);
		for (int[] clause : formula.clauses) {
			solver.addClause(clause);
		}
		for (int[] amo : formula.amos) {
			solver.addAtMostOne(amo);
		}
		solver.setTimeLimit(timeout);
		long startTime = System.currentTimeMillis();
		try {
			solver.solve(new int[] {});
			stats.setSolvedCounter(1);
		} catch (SolverTimeoutException e) {
			stats.setTimeoutCounter(1);
		}

		stats.add(solver.getStatistics());
		stats.setMilliseconds(System.currentTimeMillis() - startTime);
		return stats;
	}

	public static Statistics solveWithSat4j(SudokuProblem problem, int timeout) {
		Statistics stats = new Statistics();
		ISolver solver = SolverFactory.newDefault();
		solver.setTimeout(timeout);
		CnfWithAMO formula = convertSudokuProblemToCnfWithAMO(problem);
		long startTime = 0;
		try {
			for (int[] clause : formula.clauses) {
				solver.addClause(new VecInt(clause));
			}
			for (int[] amo : formula.amos) {
				solver.addAtMost(new VecInt(amo), 1);
			}

			startTime = System.currentTimeMillis();
			solver.isSatisfiable();
		} catch (ContradictionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			stats.setTimeoutCounter(1);
		}

		Map<String, Number> statistics = solver.getStat();
		stats.setMilliseconds(System.currentTimeMillis() - startTime);
		stats.setDecisions(statistics.get("decisions").longValue());
		stats.setConflicts(statistics.get("conflicts").longValue());
		stats.setPropagations(statistics.get("propagations").longValue());

		stats.setSolvedCounter(1);

		return stats;
	}

	public static CnfWithAMO convertSudokuProblemToCnfWithAMO(SudokuProblem sudoku) {
		CnfWithAMO formula = new CnfWithAMO();
		int numbers = sudoku.size * sudoku.size;
		eachCellExactlyOneNumber(formula, numbers);
		eachNumberInAtMostOneRowColumnBlock(formula, numbers);
		solutionSeedHolds(sudoku, formula, numbers);
		return formula;
	}

	private static void solutionSeedHolds(SudokuProblem sudoku, CnfWithAMO formula, int numbers) {
		for (int x = 0; x < numbers; x++) {
			for (int y = 0; y < numbers; y++) {
				int value = sudoku.values[x][y];
				if (value != 0) {
					formula.clauses.add(new int[] { getVarCode(numbers, x, y, value) });
				}
			}
		}
	}

	private static void eachCellExactlyOneNumber(CnfWithAMO formula, int numbers) {
		for (int x = 0; x < numbers; x++) {
			for (int y = 0; y < numbers; y++) {
				int[] codes = new int[numbers];
				for (int i = 1; i <= numbers; i++) {
					codes[i - 1] = getVarCode(numbers, x, y, i);
				}
				formula.amos.add(codes);
				formula.clauses.add(codes);
			}
		}
	}

	private static void eachNumberInAtMostOneRowColumnBlock(CnfWithAMO formula, int numbers) {
		for (int i = 1; i <= numbers; i++) {
			atMostOnceInEachRow(formula, numbers, i);
			atMostOnceInEachColumn(formula, numbers, i);
			atMostOnceInEachBlock(formula, numbers, i);
		}
	}

	private static void atMostOnceInEachRow(CnfWithAMO formula, int numbers, int i) {
		for (int x = 0; x < numbers; x++) {
			int[] codes = new int[numbers];
			for (int y = 0; y < numbers; y++) {
				codes[y] = getVarCode(numbers, x, y, i);
			}
			formula.amos.add(codes);
		}
	}

	private static void atMostOnceInEachColumn(CnfWithAMO formula, int numbers, int i) {
		for (int x = 0; x < numbers; x++) {
			int[] codes = new int[numbers];
			for (int y = 0; y < numbers; y++) {
				codes[y] = getVarCode(numbers, y, x, i);
			}
			formula.amos.add(codes);
		}
	}

	private static void atMostOnceInEachBlock(CnfWithAMO formula, int numbers, int i) {
		int size = (int) Math.round(Math.sqrt(numbers));
		for (int x = 0; x < numbers; x += size) {
			for (int y = 0; y < numbers; y += size) {
				int[] codes = new int[numbers];
				int next = 0;
				for (int xx = x; xx < x + size; xx++) {
					for (int yy = y; yy < y + size; yy++) {
						codes[next] = getVarCode(numbers, xx, yy, i);
						next++;
					}
				}
				formula.amos.add(codes);
			}
		}
	}

	public static int getVarCode(int size, int row, int column, int value) {
		return row * size * size + column * size + value;
	}

	public static SudokuProblem readSudokuProblem(String path) throws NumberFormatException, IOException {
		SudokuProblem problem = null;
		int lineNumber = 0;
		int rows = 0;
		for (String line : Files.readAllLines(Path.of(path))) {
			if (lineNumber == 0) {
				problem = new SudokuProblem(Integer.parseInt(line));
				rows = problem.size * problem.size;
			} else {
				String[] numbers = line.split(" ");
				for (int i = 0; i < rows; i++) {
					problem.values[lineNumber - 1][i] = Integer.parseInt(numbers[i]);
				}

			}
			lineNumber++;
		}
		return problem;
	}

	public static List<String> listFilesUsingFileWalk(String dir, int depth) throws IOException {
		try (Stream<Path> stream = Files.walk(Paths.get(dir), depth)) {
			return stream.filter(file -> !Files.isDirectory(file)).map(Path::toString).collect(Collectors.toList());
		}
	}

}
