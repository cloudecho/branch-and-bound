package com.github.cloudecho.bnb;

import java.util.List;

/**
 * Branch and bound for mixed-integer programming.
 * <pre>
 * max (or min) z = c0 + CX
 * s.t. Ai*X <= (or >=, =) b_i , i = 1,...,m
 *      x_j >= (or <>) 0 , j = 1,...,n
 *      x_j integer (for some or all j = 1,..,n)
 *
 * C = (c1 ... c_n)
 * X = (x1 ... x_n)'
 * A = [ a11 ... a1n   // A1
 *       ... ... ...
 *      a_m1 ... a_mn] // Am
 * b = (b1 ... b_m)'
 * </pre>
 *
 * @see <a href="https://web.mit.edu/15.053/www/AMP-Chapter-09.pdf">
 * Integer Programming</a>
 */
public class BnB extends GeneralLP implements Solver {
    static final Log LOG = LogFactory.getLog(BnB.class);

    private final List<Integer> intVars;

    /**
     * Constructor. All variables are default to be non-negative.
     *
     * @param objectiveType The objective type (max or min)
     * @param c0            c0
     * @param c             The coefficient vector C, 1 row, n columns
     * @param a             The matrix A, m rows, n columns
     * @param signs         List of sign (&lt;=, &gt;=, =). <br>
     *                      e.g. [EQ, GE, LE] represents A1*X = b1, A2*X >= b2, A3*X <= b3.
     * @param b             The vector b, m rows, 1 column
     * @param freeVars      unrestricted variables, var starts from 1. <br>
     *                      e.g. {1,2} represents x1,x2 are unrestricted.
     * @param intVars       integer variables, var starts from 1. <br>
     *                      e.g. {3,4} represents x3,x4 are restricted to be non-negative integer.
     */
    public BnB(ObjectiveType objectiveType, double c0, double[] c, double[][] a, Sign[] signs, double[] b, int[] freeVars, int[] intVars) {
        super(objectiveType, c0, c, a, signs, b, freeVars);
        this.intVars = Maths.unique(intVars);
        LOG.debug("intVars", intVars);
    }

    @Override
    protected void toStringExtra(StringBuilder b) {
        b.append("\n intVars=").append(intVars);
    }
}
