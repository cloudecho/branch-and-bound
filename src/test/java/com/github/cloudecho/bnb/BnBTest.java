package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.util.Sign;
import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Level;

public class BnBTest {
    static {
        BnB.LOG.setLevel(Level.ALL);
        //GeneralLP.LOG.setLevel(Level.ALL);
        //Simplex.LOG.setLevel(Level.ALL);
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
        Assert.assertEquals("objective", 65, (int) bnb.getObjective());
        Assert.assertEquals("x[0]", 8, (int) x[0]);
        Assert.assertEquals("x[1]", 3, (int) x[1]);
    }

    @Test
    public void testSolve01() {
        double[] c = {-8, -2, -4, -7, -5};
        double[][] a = {
                {-3, -3, 1, 2, 3},
                {-5, -3, -2, -1, 1}
        };
        Sign[] signs = {Sign.LE, Sign.LE};
        double[] b = {-2, -4};
        int[] freeVars = null;
        int[] intVars = null;
        int[] binVars = new int[]{1, 2, 3, 4, 5};

        BnB bnb = new BnB(ObjectiveType.max, 10, c, a, signs, b, freeVars, intVars, binVars);
        bnb.solve();

        double[] x = bnb.getX();
        Assert.assertEquals("state", State.SOLVED, bnb.getState());
        Assert.assertEquals("objectiveType", ObjectiveType.max, bnb.getObjectiveType());
        Assert.assertEquals("objective", 4, (int) bnb.getObjective());
        Assert.assertEquals("x[0]", 0, (int) x[0]);
        Assert.assertEquals("x[1]", 1, (int) x[1]);
        Assert.assertEquals("x[2]", 1, (int) x[2]);
        Assert.assertEquals("x[3]", 0, (int) x[3]);
        Assert.assertEquals("x[4]", 0, (int) x[4]);
    }

    @Test
    public void testSolveMixed() {
        double[] c = {3, 2, 0, 0};
        double[][] a = {
                {1, -2, 1, 0},
                {2, 1, 0, 1}
        };
        Sign[] signs = {Sign.EQ, Sign.EQ};
        double[] b = {2.5, 1.5};
        int[] freeVars = null;
        int[] intVars = {2, 3};

        BnB bnb = new BnB(ObjectiveType.min, 4, c, a, signs, b, freeVars, intVars);
        bnb.solve();

        double[] x = bnb.getX();
        Assert.assertEquals("state", State.SOLVED, bnb.getState());
        Assert.assertEquals("objectiveType", ObjectiveType.min, bnb.getObjectiveType());
        Assert.assertEquals("objective", 55, (int) (10 * bnb.getObjective()));
        Assert.assertEquals("x[0]", 5, (int) (10 * x[0]));
        Assert.assertEquals("x[1]", 0, (int) (10 * x[1]));
        Assert.assertEquals("x[2]", 20, (int) (10 * x[2]));
        Assert.assertEquals("x[3]", 5, (int) (10 * x[3]));
    }

    @Test
    public void testSolveMixedNoSolution() {
        double[] c = {3, 2, 0, 0};
        double[][] a = {
                {1, -2, 1, 0},
                {2, 1, 0, 1}
        };
        Sign[] signs = {Sign.EQ, Sign.EQ};
        double[] b = {2.5, 1.5};
        int[] freeVars = null;
        int[] intVars = {2, 3};
        int[] binVars = {3};

        BnB bnb = new BnB(ObjectiveType.min, 4, c, a, signs, b, freeVars, intVars, binVars);
        bnb.solve();

        Assert.assertEquals("state", State.NO_SOLUTION, bnb.getState());
    }

    @Test
    public void testSolveTsp3() {
        double inf = Integer.MAX_VALUE;
        double[] c = {inf, 20.000, 24.083, 20.000, inf, 18.439, 24.083, 18.439, inf, 0.000, 0.000};
        double[][] a = {
                {1.000, 0.000, 0.000, 1.000, 0.000, 0.000, 1.000, 0.000, 0.000, 0.000, 0.000},
                {0.000, 1.000, 0.000, 0.000, 1.000, 0.000, 0.000, 1.000, 0.000, 0.000, 0.000},
                {0.000, 0.000, 1.000, 0.000, 0.000, 1.000, 0.000, 0.000, 1.000, 0.000, 0.000},
                {1.000, 1.000, 1.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000},
                {0.000, 0.000, 0.000, 1.000, 1.000, 1.000, 0.000, 0.000, 0.000, 0.000, 0.000},
                {0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 1.000, 1.000, 1.000, 0.000, 0.000},
                {0.000, 0.000, 0.000, 0.000, 0.000, 3.000, 0.000, 0.000, 0.000, 1.000, -1.000},
                {0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 3.000, 0.000, -1.000, 1.000},
                {0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 1.000, 0.000},
                {0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 1.000},
                {0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 1.000, 0.000},
                {0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 1.000}
        };

        Sign[] signs = {Sign.EQ, Sign.EQ, Sign.EQ, Sign.EQ, Sign.EQ, Sign.EQ, Sign.LE, Sign.LE, Sign.GE, Sign.GE, Sign.LE, Sign.LE};
        double[] b = {1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2};
        int[] freeVars = null;
        int[] intVars = {10, 11};
        int[] binVars = {1, 2, 3, 4, 5, 6, 7, 8, 9};

        BnB bnb = new BnB(ObjectiveType.min, 0, c, a, signs, b, freeVars, intVars, binVars);
        bnb.solve();

        Assert.assertEquals("state", State.SOLVED, bnb.getState());
        Assert.assertEquals("min", 62.522, bnb.getObjective(), 0.0001);
    }
}
