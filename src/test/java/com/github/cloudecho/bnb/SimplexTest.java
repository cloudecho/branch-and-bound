package com.github.cloudecho.bnb;

import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Level;

public class SimplexTest {
    static {
        Simplex.LOG.setLevel(Level.ALL);
    }

    @Test
    public void testSolve() {
        double[] c = new double[]{5, 4, 6, 0, 0, 0, 0};
        double[][] a = new double[][]{
                new double[]{1, 1, 1, 1, 0, 0, 0},
                new double[]{1, 0.5, 0, 0, 1, 0, 0},
                new double[]{3, 0, 1, 0, 0, 1, 0},
                new double[]{0, -2, -3, 0, 0, 0, -1},
        };
        double[] b = new double[]{60, 32, 43, -86};

        Simplex simplex = new Simplex(c, a, b);
        simplex.solve();

        double[] x = simplex.getX();
        Assert.assertEquals("state", State.SOLVED, simplex.getState());
        Assert.assertEquals("iterations", 3, simplex.getIterations());
        Assert.assertEquals("max", 237, (int) simplex.getMax());
        Assert.assertEquals("x[0]", 13, (int) x[0]);
        Assert.assertEquals("x[1]", 37, (int) x[1]);
        Assert.assertEquals("x[2]", 3, (int) x[2]);
    }

    @Test
    public void testSolveNoSolution() {
        double[] c = new double[]{5, 4, 6, 0, 0, 0, 0};
        double[][] a = new double[][]{
                new double[]{1, 1, 1, 1, 0, 0, 0},
                new double[]{1, 0.5, 0, 0, 1, 0, 0},
                new double[]{3, 0, 1, 0, 0, 1, 0},
                new double[]{0, -2, -3, 0, 0, 0, -1},
        };
        double[] b = new double[]{-60, -32, -43, 86};

        Simplex simplex = new Simplex(c, a, b);
        simplex.solve();

        Assert.assertEquals("state", State.NO_SOLUTION, simplex.getState());
    }
}
