package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.util.Sign;
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

        GeneralLP generalLP = new GeneralLP(ObjectiveType.min, 3, c, a, signs, b, freeVars);
        generalLP.solve();

        double[] x = generalLP.getX();
        Assert.assertEquals("state", State.SOLVED, generalLP.getState());
        //Assert.assertEquals("iterations", 1, generalLP.getIterations());
        Assert.assertEquals("objectiveType", ObjectiveType.min, generalLP.getObjectiveType());
        Assert.assertEquals("objective", -76, generalLP.getObjective(), 0.1);
        Assert.assertEquals("x[0]", -27, x[0], 0.1);
        Assert.assertEquals("x[1]", 25, x[1], 0.1);

        double[] y = generalLP.getShadowPrice();
        Assert.assertEquals("y[0]", -5, y[0], 0.1);
        Assert.assertEquals("y[1]", 0, y[1], 0.1);
        Assert.assertEquals("y[2]", 3, y[2], 0.1);
    }

    @Test
    public void testSolve2() {
        double[] c = {5, 4.5, 6};
        double[][] a = {
                {6, 5, 8},
                {10, 20, 10},
                {1, 0, 0},
        };
        Sign[] signs = {Sign.LE, Sign.LE, Sign.LE};
        double[] b = {60, 150, 8};
        GeneralLP generalLP = new GeneralLP(ObjectiveType.max, 0, c, a, signs, b, null);
        generalLP.solve();

        double[] x = generalLP.getX();
        Assert.assertEquals("state", State.SOLVED, generalLP.getState());
        Assert.assertEquals("objectiveType", ObjectiveType.max, generalLP.getObjectiveType());
        Assert.assertEquals("objective", 51.428571, generalLP.getObjective(), 0.000001);
        Assert.assertEquals("x[0]", 6.428571, x[0], 0.000001);
        Assert.assertEquals("x[1]", 4.285714, x[1], 0.000001);
        Assert.assertEquals("x[2]", 0, x[2], 0.000001);

        double[] y = generalLP.getShadowPrice();
        Assert.assertEquals("y[0]", 0.785714, y[0], 0.000001);
        Assert.assertEquals("y[1]", 0.028571, y[1], 0.000001);
        Assert.assertEquals("y[2]", 0, y[2], 0.000001);
    }
}
