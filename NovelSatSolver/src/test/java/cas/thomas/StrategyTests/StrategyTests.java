package cas.thomas.StrategyTests;

public class StrategyTests {

    // These tests need to be rewritten

    /*@Test
    public void FirstOpenVariableSelectiontest() {
        FirstOpenVariableSelection firstOpenVariableSelection = new FirstOpenVariableSelection();

        int[] variables = new int[]{0,1,-1,1,1,-1,-1,0};

        Assert.assertEquals(firstOpenVariableSelection.getNextVariable(variables, null, false), 7);
    }

    @Test
    public void VSIDSTest() {
        VSIDS vsids = new VSIDS();

        int[] variables = new int[]{0,1,-1,1,1,-1,-1,0,0,0,1,1};
        int[] occurences = new int[]{0,2,5,1,2,7,8,2,3,1,4,1};

        Assert.assertEquals(vsids.getNextVariable(variables, occurences, false), 8);

        variables[8] = 1;

        Assert.assertEquals(vsids.getNextVariable(variables, occurences, false), 7);

        variables[7] = -1;

        Assert.assertEquals(vsids.getNextVariable(variables, occurences, false), 9);

        for (int i = 0; i < 256; i++) {
            vsids.getNextVariable(variables, occurences, true);
        }

        Assert.assertArrayEquals(new int[]{0,1,2,0,1,3,4,1,1,0,2,0}, occurences);
    }*/
}
