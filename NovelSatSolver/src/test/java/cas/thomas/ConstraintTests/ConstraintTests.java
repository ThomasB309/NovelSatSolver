package cas.thomas.ConstraintTests;

import cas.thomas.Formulas.AMOConstraint;
import cas.thomas.Formulas.Constraint;
import cas.thomas.Formulas.DisjunctiveConstraint;
import cas.thomas.utils.IntegerArrayQueue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ConstraintTests {

    /*
    Need to be rewritten
     */

    /*private AMOConstraint amoConstraint;
    private DisjunctiveConstraint disjunctiveConstraint;
    private List<Constraint>[] positivelyWatchedDisjunctiveConstraints;
    private List<Constraint>[] negativelyWatchedDisjunctiveConstraints;
    private List<Constraint>[] positivelyWatchedAMOConstraints;
    private List<Constraint>[] negativelyWatchedAMOConstraints;
    private int numberOfVariables;
    private int[] variableAssignments;
    private IntegerArrayQueue unitLiterals;
    private Constraint[] reasonClauses;

    @Before
    public void setup() {
        numberOfVariables = 6;
        variableAssignments = new int[numberOfVariables];
        unitLiterals = new LinkedList<>();
        reasonClauses = new Constraint[numberOfVariables];

        positivelyWatchedDisjunctiveConstraints = (ArrayList<Constraint>[]) new ArrayList[numberOfVariables];
        negativelyWatchedDisjunctiveConstraints = (ArrayList<Constraint>[]) new ArrayList[numberOfVariables];

        positivelyWatchedAMOConstraints = (ArrayList<Constraint>[]) new ArrayList[numberOfVariables];
        negativelyWatchedAMOConstraints = (ArrayList<Constraint>[]) new ArrayList[numberOfVariables];

        for (int i = 0; i < numberOfVariables; i++) {
            positivelyWatchedDisjunctiveConstraints[i] = new ArrayList<>();
            negativelyWatchedDisjunctiveConstraints[i] = new ArrayList<>();
            positivelyWatchedAMOConstraints[i] = new ArrayList<>();
            negativelyWatchedAMOConstraints[i] = new ArrayList<>();
        }


        amoConstraint = new AMOConstraint(new int[]{1, 2, 3}, positivelyWatchedAMOConstraints, negativelyWatchedAMOConstraints);
        disjunctiveConstraint = new DisjunctiveConstraint(new int[]{2, -3, -4}, positivelyWatchedDisjunctiveConstraints, negativelyWatchedDisjunctiveConstraints);

    }

    @Test
    public void testDisjunctiveConstraintsSetup() {
        Assert.assertEquals(positivelyWatchedDisjunctiveConstraints[2].size(), 1);
        Assert.assertEquals(negativelyWatchedDisjunctiveConstraints[2].size(), 0);

        Assert.assertEquals(positivelyWatchedDisjunctiveConstraints[3].size(), 0);
        Assert.assertEquals(negativelyWatchedDisjunctiveConstraints[3].size(), 1);

        Assert.assertEquals(positivelyWatchedDisjunctiveConstraints[4].size(), 0);
        Assert.assertEquals(negativelyWatchedDisjunctiveConstraints[4].size(), 0);
    }

    @Test
    public void testAMOConstraintsSetup() {
        Assert.assertEquals(positivelyWatchedAMOConstraints[1].size(), 1);
        Assert.assertEquals(negativelyWatchedAMOConstraints[1].size(), 0);

        Assert.assertEquals(positivelyWatchedAMOConstraints[2].size(), 1);
        Assert.assertEquals(negativelyWatchedAMOConstraints[2].size(), 0);

        Assert.assertEquals(positivelyWatchedAMOConstraints[3].size(), 1);
        Assert.assertEquals(negativelyWatchedAMOConstraints[3].size(), 0);
    }


    @Test
    public void testPropagateInDisjunctiveConstraints() {

        variableAssignments[2] = -1;

        Assert.assertEquals(unitLiterals.size(), 0);

        Assert.assertEquals(positivelyWatchedDisjunctiveConstraints[2].size(), 1);
        Assert.assertEquals(negativelyWatchedDisjunctiveConstraints[4].size(), 0);


        Assert.assertFalse(disjunctiveConstraint.propagate(-2, variableAssignments, unitLiterals,
                positivelyWatchedDisjunctiveConstraints, negativelyWatchedDisjunctiveConstraints, reasonClauses));

        Assert.assertEquals(negativelyWatchedDisjunctiveConstraints[4].size(), 1);
        Assert.assertEquals(unitLiterals.size(), 0);

        variableAssignments[3] = 1;

        Assert.assertTrue(disjunctiveConstraint.propagate(3, variableAssignments, unitLiterals,
                positivelyWatchedDisjunctiveConstraints, negativelyWatchedDisjunctiveConstraints,
                reasonClauses));

        Assert.assertEquals(unitLiterals.size(), 1);
        Assert.assertEquals(unitLiterals.get(0).intValue(), -4);

    }

    @Test
    public void testPropagateInDisjunctiveConstraintsWithConflict() {
        variableAssignments[2] = -1;

        Assert.assertEquals(unitLiterals.size(), 0);

        Assert.assertFalse(disjunctiveConstraint.propagate(-2, variableAssignments, unitLiterals,
                positivelyWatchedDisjunctiveConstraints, negativelyWatchedDisjunctiveConstraints,
                reasonClauses));

        variableAssignments[3] = 1;
        variableAssignments[4] = 1;

        Assert.assertTrue(disjunctiveConstraint.propagate(3, variableAssignments, unitLiterals,
                positivelyWatchedDisjunctiveConstraints, negativelyWatchedDisjunctiveConstraints,
                reasonClauses));

        Assert.assertEquals(unitLiterals.size(), 0);

        Assert.assertTrue(disjunctiveConstraint.resetConflictState());
        Assert.assertFalse(disjunctiveConstraint.resetConflictState());
    }

    @Test
    public void testPropagateInAMOConstraints() {
        variableAssignments[1] = -1;

        Assert.assertEquals(unitLiterals.size(), 0);

        Assert.assertFalse(amoConstraint.propagate(-1, variableAssignments, unitLiterals,
                positivelyWatchedDisjunctiveConstraints, negativelyWatchedDisjunctiveConstraints,
                reasonClauses));

        Assert.assertEquals(unitLiterals.size(), 0);

        variableAssignments[2] = 1;

        Assert.assertTrue(amoConstraint.propagate(2, variableAssignments, unitLiterals,
                positivelyWatchedDisjunctiveConstraints, negativelyWatchedDisjunctiveConstraints,
                reasonClauses));

        Assert.assertEquals(unitLiterals.size(), 1);
        Assert.assertEquals(unitLiterals.get(0).intValue(), -3);

    }*/

}
