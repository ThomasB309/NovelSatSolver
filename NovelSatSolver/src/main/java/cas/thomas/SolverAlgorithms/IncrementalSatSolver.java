package cas.thomas.SolverAlgorithms;

import cas.thomas.Exceptions.SolverTimeoutException;
import cas.thomas.Exceptions.UnitLiteralConflictException;

import java.util.concurrent.TimeoutException;

public interface IncrementalSatSolver {
	
	public void addClause(int[] clause);
	
	public void addAtMostOne(int[] literals);
	
	public void addDnf(int[][] dnf);
	
	public void setTimeLimit(long milliseconds);
	
	/**
	 * Assumption are temporary unit literals, they are valid only this solve call
	 * @param assumptions
	 * @return
	 * @throws SolverTimeoutException
	 */
	public boolean solve(int[] assumptions) throws SolverTimeoutException, UnitLiteralConflictException, TimeoutException;
	
	// if solve returns true, then get the truth values with this method
	public boolean getValue(int variable) throws IllegalStateException;

}

