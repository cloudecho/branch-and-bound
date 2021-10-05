package com.github.cloudecho.bnb;

import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Level;

public class BnBTest {
    static {
        BnB.LOG.setLevel(Level.ALL);
    }

    @Test
    public void testSolve() {
        double[] c = {5, 7};
        double[][] a = {
                {1, 1},
                {4, 9}
        };
        Sign[] signs = {Sign.LE, Sign.LE};
        double[] b = {11, 61};
        int[] freeVars = null;
        int[] intVars = {1, 2};

        BnB bnb = new BnB(ObjectiveType.max, 4, c, a, signs, b, freeVars, intVars);
        bnb.solve();

        double[] x = bnb.getX();
        Assert.assertEquals("state", State.SOLVED, bnb.getState());
        Assert.assertEquals("objectiveType", ObjectiveType.max, bnb.getObjectiveType());
        Assert.assertEquals("objective", 650, (int) (10 * bnb.getObjective()));
        Assert.assertEquals("x[0]", 80, (int) (10 * x[0]));
        Assert.assertEquals("x[1]", 30, (int) (10 * x[1]));
    }

    @Test
    public void testSolve2() {
        double[] c = {5, 7};
        double[][] a = {
                {1, 1},
                {4, 9}
        };
        Sign[] signs = {Sign.LE, Sign.LE};
        double[] b = {11, 61};
        int[] freeVars = null;
        int[] intVars = {1, 2};

        BnB bnb = new BnB(ObjectiveType.max, 4, c, a, signs, b, freeVars, intVars);
        bnb.solve();

        double[] x = bnb.getX();
        Assert.assertEquals("state", State.SOLVED, bnb.getState());
        Assert.assertEquals("objectiveType", ObjectiveType.max, bnb.getObjectiveType());
        Assert.assertEquals("objective", 650, (int) (10 * bnb.getObjective()));
        Assert.assertEquals("x[0]", 80, (int) (10 * x[0]));
        Assert.assertEquals("x[1]", 30, (int) (10 * x[1]));
    }
}
