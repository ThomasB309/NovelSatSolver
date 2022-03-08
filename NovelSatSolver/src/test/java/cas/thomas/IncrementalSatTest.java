package cas.thomas;

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import cas.thomas.Exceptions.SolverTimeoutException;
import cas.thomas.Exceptions.UnitLiteralConflictException;
import cas.thomas.SolverAlgorithms.IncrementalCdclSolver;
import cas.thomas.SolverAlgorithms.IncrementalSatSolver;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class IncrementalSatTest extends TestCase {

	@Test
	public void testIncrementalBasic() throws SolverTimeoutException, UnitLiteralConflictException, TimeoutException {
		IncrementalSatSolver solver = new IncrementalCdclSolver();
		solver.addClause(new int[] {1, 2});
		solver.addClause(new int[] {-1, 2});
		solver.addClause(new int[] {1, -2});
		assumeTrue(solver.solve(new int[] {}));
		assumeTrue(solver.getValue(1));
		assumeTrue(solver.getValue(2));
		solver.addClause(new int[] {-1,-2});
		assumeFalse(solver.solve(new int[] {}));
	}

	@Test
	public void testIncrementalAssumptions() throws SolverTimeoutException, UnitLiteralConflictException, TimeoutException {
		IncrementalSatSolver solver = new IncrementalCdclSolver();
		solver.addClause(new int[] {1, 2});
		solver.addClause(new int[] {-1, 2});
		solver.addClause(new int[] {1, -2});
		assumeFalse(solver.solve(new int[] {-2}));
		assumeTrue(solver.solve(new int[] {}));
	}

	@Test
	public void testIncrementalAMO() throws SolverTimeoutException, UnitLiteralConflictException, TimeoutException {
		IncrementalSatSolver solver = new IncrementalCdclSolver();
		solver.addAtMostOne(new int[] {1, 2});
		assumeTrue(solver.solve(new int[]{}));
		assumeFalse(solver.solve(new int[] {1,2}));
		assumeTrue(solver.solve(new int[]{1}));
		assumeTrue(solver.getValue(1));
		assumeFalse(solver.getValue(2));
	}

	@Test
	public void testIncrementalDNF() throws UnitLiteralConflictException, TimeoutException, SolverTimeoutException {
		IncrementalSatSolver solver = new IncrementalCdclSolver();
		solver.addDnf(new int[][]{{1},{3,4},{-5}});
		assumeTrue(solver.solve(new int[]{}));
		assumeFalse(solver.solve(new int[] {-1,-3,5}));
		assumeTrue(solver.solve(new int[]{-1,5}));
		assumeTrue(solver.getValue(3));
		assumeTrue(solver.getValue(4));
	}


}
