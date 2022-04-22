package cas.thomas.Sudoku;

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

	public static class Statistics {
		public long milliseconds;
		public long decisions;
		public long propagations;
		public long conflicts;

		@Override
		public String toString() {
			return "Statistics [milliseconds=" + milliseconds + ", decisions=" + decisions + ", propagations="
					+ propagations + ", conflicts=" + conflicts + "]";
		}
	}

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
		for (String file : listFilesUsingFileWalk("C:\\Users\\tomas.balyo\\Desktop\\sudokus", 1)) {
			SudokuProblem problem = readSudokuProblem(file);
			Statistics s4jstats = solveWithSat4j(problem);
			Statistics dinostats = solveWithDinoSat(problem);
			System.out.println(file + " " + s4jstats + " " + dinostats);
		}

	}
	
	public static Statistics solveWithDinoSat(SudokuProblem problem) {
		Statistics stats = new Statistics();
		CnfWithAMO formula = convertSudokuProblemToCnfWithAMO(problem);
		IncrementalSatSolver solver = new IncrementalCdclSolver();
		for (int[] clause : formula.clauses) {
			solver.addClause(clause);
		}
		for (int[] amo : formula.amos) {
			solver.addAtMostOne(amo);
		}
		solver.setTimeLimit(5000);
		try {
			long startTime = System.currentTimeMillis();
			solver.solve(new int[] {});
			stats.milliseconds = System.currentTimeMillis() - startTime;
		} catch (SolverTimeoutException | UnitLiteralConflictException | java.util.concurrent.TimeoutException e) {
			System.out.println(e.getMessage());
		}
		return stats;
	}

	public static Statistics solveWithSat4j(SudokuProblem problem) {
		Statistics stats = new Statistics();
		ISolver solver = SolverFactory.newDefault();
		solver.setTimeout(5);
		CnfWithAMO formula = convertSudokuProblemToCnfWithAMO(problem);
		try {
			for (int[] clause : formula.clauses) {
				solver.addClause(new VecInt(clause));
			}
			for (int[] amo : formula.amos) {
				solver.addAtMost(new VecInt(amo), 1);
			}

			long startTime = System.currentTimeMillis();
			solver.isSatisfiable();
			Map<String, Number> statistics = solver.getStat();
			stats.milliseconds = System.currentTimeMillis() - startTime;
			stats.decisions = statistics.get("decisions").longValue();
			stats.conflicts = statistics.get("conflicts").longValue();
			stats.propagations = statistics.get("propagations").longValue();
		} catch (ContradictionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			System.out.println("TIMEOUT");
		}

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
