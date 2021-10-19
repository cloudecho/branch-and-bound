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

        Simplex simplex = SimplexFactory.newSimplex(c, a, b);
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

        Simplex simplex = SimplexFactory.newSimplex(c, a, b);
        simplex.solve();

        Assert.assertEquals("state", State.NO_SOLUTION, simplex.getState());
    }

    @Test
    public void testSolveMax0() {
        double[] c = {5, 4, 6, 0, 0, 0, 0};
        double[][] a = {
                {1, 1, 1, 1, 0, 0, 0},
                {1, -0.5, 0, 0, 1, 0, 0},
                {3, 0, -1, 0, 0, 1, 0},
                {0, 2, -3, 0, 0, 0, 1},
        };
        double[] b = {-0, -0, -0, 0};

        Simplex simplex = SimplexFactory.newSimplex(c, a, b);
        simplex.solve();

        Assert.assertEquals("state", State.SOLVED, simplex.getState());
        Assert.assertEquals("max", 0, (int) simplex.getMax());
    }

    @Test
    public void testSolveAnticycling() {
        double[] c = {4, 1.92, -16, -0.96, 0, 0};
        double[][] a = {
                {-12.5, -2, 12.5, 1, 1, 0},
                {1, 0.24, -2, -0.24, 0, 1},
        };
        double[] b = {0, 0};

        Simplex simplex = SimplexFactory.newSimplex(c, a, b);
        simplex.solve();

        Assert.assertEquals("state", State.UNBOUNDED, simplex.getState());
    }

    @Test
    public void testSolveAnticycling2() {
        double[] c = {5, 4, 6, 0, 0, 0, 0};
        double[][] a = {
                {1, 1, 1, 1, 0, 0, 0},
                {1, -0.5, 0, 0, 0, 0, 0},
                {3, 0, -1, 0, 0, 1, 0},
                {0, 2, -3, 0, 0, 0, 1},
        };
        double[] b = {-0, -0, -0, 0};

        Simplex simplex = SimplexFactory.newSimplex(c, a, b);
        simplex.solve();

        Assert.assertEquals("state", State.SOLVED, simplex.getState());
        Assert.assertEquals("max", 0, (int) simplex.getMax());
    }

    @Test
    public void testSolveDrivingAvars() {
        double[] c = {5, 4, 6, 0, 0, 0, 0};
        double[][] a = {
                {1, 1, 1, 1, 0, 0, 0},
                {1, -0.5, 0, 0, 1, 0, 0},
                {3, 0, 1, 0, 0, 1, 0},
                {0, -2, -3, 0, 0, 0, 1},
        };
        double[] b = {60, -32, -43, 86};

        Simplex simplex = SimplexFactory.newSimplex(c, a, b);
        simplex.solve();

        Assert.assertEquals("state", State.NO_SOLUTION, simplex.getState());
    }

    @Test
    public void testSolveDrivingAvars2() {
        double[] c = {1, 1, 0, 0};
        double[][] a = {
                {1, 1, 0, 0},
                {1, 0, 1, 0,},
                {0, 1, 0, 1}
        };
        double[] b = {2, 1, 1};

        Simplex simplex = SimplexFactory.newSimplex(c, a, b);
        simplex.solve();

        double[] x = simplex.getX();
        Assert.assertEquals("state", State.SOLVED, simplex.getState());
        Assert.assertEquals("max", 2, (int) simplex.getMax());
        Assert.assertEquals("x[0]", 1, (int) x[0]);
        Assert.assertEquals("x[1]", 1, (int) x[1]);
        Assert.assertEquals("x[2]", 0, (int) x[2]);
        Assert.assertEquals("x[3]", 0, (int) x[3]);
    }

    @Test
    public void testSolvePivotOnNegative() {
        double[] c = {1, 0, 0, 0, 0, 0};
        double[][] a = {
                {1, 1, 1, 0, 0, 0},
                {1, 0, 0, 1, 0, 0},
                {0, 1, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 1}
        };
        double[] b = {3, 1, 1, 1};

        Simplex simplex = SimplexFactory.newSimplex(c, a, b);
        simplex.solve();

        double[] x = simplex.getX();
        Assert.assertEquals("state", State.SOLVED, simplex.getState());
        Assert.assertEquals("max", 1, (int) simplex.getMax());
        Assert.assertEquals("x[0]", 1, (int) x[0]);
        Assert.assertEquals("x[1]", 1, (int) x[1]);
        Assert.assertEquals("x[2]", 1, (int) x[2]);
        Assert.assertEquals("x[3]", 0, (int) x[3]);
        Assert.assertEquals("x[4]", 0, (int) x[4]);
        Assert.assertEquals("x[5]", 0, (int) x[5]);
    }

    @Test
    public void testSolvePivotOnNegative2() {
        double[] c = {0, -1, -2, 0, 0};
        double[][] a = {
                {1, 1, 0, 0, 0},
                {0, -1, 1, 1, 0},
                {4, 4, -1, 0, 1}
        };
        double[] b = {10, 20, 30};

        Simplex simplex = SimplexFactory.newSimplex(c, a, b);
        simplex.solve();

        double[] x = simplex.getX();
        Assert.assertEquals("state", State.SOLVED, simplex.getState());
        Assert.assertEquals("max", -20, (int) simplex.getMax());
        Assert.assertEquals("x[0]", 10, (int) x[0]);
        Assert.assertEquals("x[1]", 0, (int) x[1]);
        Assert.assertEquals("x[2]", 10, (int) x[2]);
        Assert.assertEquals("x[3]", 10, (int) x[3]);
        Assert.assertEquals("x[4]", 0, (int) x[4]);
    }

    @Test
    public void testSolvePivotOnNegative3() {
        double[] c = {0, -1, -0.5, 0, 0};
        double[][] a = {
                {1, 0, -0.125, 0, 0},
                {0, 1, -1, 1, 0},
                {4, -1, -2, 0, 1}
        };
        double[] b = {10, 20, 30};

        Simplex simplex = SimplexFactory.newSimplex(c, a, b);
        simplex.solve();

        double[] x = simplex.getX();
        Assert.assertEquals("state", State.SOLVED, simplex.getState());
        Assert.assertEquals("iterations", 1, simplex.getIterations());
        Assert.assertEquals("max*10", -33, (int) (10 * simplex.getMax()));
        Assert.assertEquals("x[0]*10", 108, (int) (10 * x[0]));
        Assert.assertEquals("x[1]", 0, (int) x[1]);
        Assert.assertEquals("x[2]*10", 66, (int) (10 * x[2]));
        Assert.assertEquals("x[3]*10", 266, (int) (10 * x[3]));
        Assert.assertEquals("x[4]", 0, (int) x[4]);
    }

    @Test
    public void testSolvePivotOnNegative4() {
        double[] c = {1, 0, 0, 0, 0, 0};
        double[][] a = {
                {1, 1, 1, 0, 0, 0},
                {1, 0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 1},
                {0, 1, 0, 0, 1, 0}
        };
        double[] b = {3, 1, 1, 0.5};

        Simplex simplex = SimplexFactory.newSimplex(c, a, b);
        simplex.solve();

        double[] x = simplex.getX();
        Assert.assertEquals("state", State.NO_SOLUTION, simplex.getState());
    }

    @Test
    public void testSolvePivotOnNegative5() {
        double[] c = {1, 0, 0, 0, 0, 0, 0, 0};
        double[][] a = {
                {1, 1, 1, 1, 0, 0, 0, 0},
                {1, 0, 0, 0, 1, 0, 0, 0},
                // moved to the tail
                {0, 0, 1, 0, 0, 0, 1, 0},
                {0, 0, 0, 1, 0, 0, 0, 1},
                {0, 1, 0, 0, 0, 1, 0, 0},
        };
        double[] b = {4, 1, 1, 1, 1};

        Simplex simplex = SimplexFactory.newSimplex(c, a, b);
        simplex.solve();

        double[] x = simplex.getX();
        Assert.assertEquals("state", State.SOLVED, simplex.getState());
        Assert.assertEquals("max", 1, (int) simplex.getMax());
        Assert.assertEquals("x[0]", 1, (int) x[0]);
        Assert.assertEquals("x[1]", 1, (int) x[1]);
        Assert.assertEquals("x[2]", 1, (int) x[2]);
        Assert.assertEquals("x[3]", 1, (int) x[3]);
        Assert.assertEquals("x[4]", 0, (int) x[4]);
        Assert.assertEquals("x[5]", 0, (int) x[5]);
        Assert.assertEquals("x[6]", 0, (int) x[6]);
        Assert.assertEquals("x[7]", 0, (int) x[7]);
    }
}
