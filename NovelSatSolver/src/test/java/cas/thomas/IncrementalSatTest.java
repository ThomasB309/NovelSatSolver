package cas.thomas;

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import cas.thomas.Exceptions.SolverTimeoutException;
import cas.thomas.SolverAlgorithms.IncrementalCdclSolver;
import cas.thomas.SolverAlgorithms.IncrementalSatSolver;
import junit.framework.TestCase;

public class IncrementalSatTest extends TestCase {
	
	public void testIncrementalBasic() throws SolverTimeoutException {
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

	public void testIncrementalAssumptions() throws SolverTimeoutException {
		IncrementalSatSolver solver = new IncrementalCdclSolver();
		solver.addClause(new int[] {1, 2});
		solver.addClause(new int[] {-1, 2});
		solver.addClause(new int[] {1, -2});
		assumeFalse(solver.solve(new int[] {-2}));
		assumeTrue(solver.solve(new int[] {}));
	}

}
