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
        double[] c = {5, 4, 6, 0, 0, 0, 0};
        double[][] a = {
                {1, 1, 1, 1, 0, 0, 0},
                {1, 0.5, 0, 0, 1, 0, 0},
                {3, 0, 1, 0, 0, 1, 0},
                {0, -2, -3, 0, 0, 0, -1},
        };
        double[] b = {60, 32, 43, -86};

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
        double[] c = {5, 4, 6, 0, 0, 0, 0};
        double[][] a = {
                {1, 1, 1, 1, 0, 0, 0},
                {1, 0.5, 0, 0, 1, 0, 0},
                {3, 0, 1, 0, 0, 1, 0},
                {0, -2, -3, 0, 0, 0, -1},
        };
        double[] b = {-60, -32, -43, 86};

        Simplex simplex = new Simplex(c, a, b);
        simplex.solve();

        Assert.assertEquals("state", State.NO_SOLUTION, simplex.getState());
    }

    @Test
    public void testSolveAnticycling() {
        double[] c = {4, 1.92, -16, -0.96, 0, 0};
        double[][] a = {
                {-12.5, -2, 12.5, 1, 1, 0},
                {1, 0.24, -2, -0.24, 0, 1},
        };
        double[] b = {0, 0};

        Simplex simplex = new Simplex(c, a, b);
        simplex.solve();

        Assert.assertEquals("state", State.UNBOUNDED, simplex.getState());
    }
}
