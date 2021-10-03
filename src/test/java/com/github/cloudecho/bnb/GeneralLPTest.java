package com.github.cloudecho.bnb;

import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Level;

public class GeneralLPTest {
    static {
        GeneralLP.LOG.setLevel(Level.ALL);
        Simplex.LOG.setLevel(Level.ALL);
    }

    @Test
    public void testSolve() {
        double[] c = {2, -1};
        double[][] a = {
                {1, 1},
                {3, 2},
                {1, 2},
        };
        Sign[] signs = {Sign.GE, Sign.LE, Sign.EQ};
        double[] b = {-2, 4, 23};
        int[] freeVars = {1};

        GeneralLP generalLP = new GeneralLP(ObjectiveType.MIN, 3, c, a, signs, b, freeVars);
        generalLP.solve();

        double[] x = generalLP.getX();
        Assert.assertEquals("state", State.SOLVED, generalLP.getState());
        Assert.assertEquals("iterations", 1, generalLP.getIterations());
        Assert.assertEquals("objectiveType", ObjectiveType.MIN, generalLP.getObjectiveType());
        Assert.assertEquals("objective", -76, (int) generalLP.getObjective());
        Assert.assertEquals("x[0]", -27, (int) x[0]);
        Assert.assertEquals("x[1]", 25, (int) x[1]);
    }
}
