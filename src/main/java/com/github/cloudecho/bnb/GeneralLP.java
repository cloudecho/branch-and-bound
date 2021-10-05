package com.github.cloudecho.bnb;

import java.util.Arrays;
import java.util.List;

/**
 * General linear program.
 * <pre>
 * max (or min) z = c0 + CX
 * s.t. Ai*X <= (or >=, =) b_i , i = 1,...,m
 *      x_j >= (or <>) 0 , j = 1,...,n
 *
 * C = (c1 ... c_n)
 * X = (x1 ... x_n)'
 * A = [ a11 ... a1n   // A1
 *       ... ... ...
 *      a_m1 ... a_mn] // Am
 * b = (b1 ... b_m)'
 * </pre>
 *
 * @see <a href="https://math.mit.edu/~goemans/18310S15/lpnotes310.pdf">
 * The lecture notes of Linear Programming by Michel Goemans</a>
 */
public class GeneralLP implements Solver {
    static final Log LOG = LogFactory.getLog(GeneralLP.class);

    protected final int m;
    protected final int n;

    protected final ObjectiveType objectiveType;
    protected final double c0;
    protected final double[] c;
    protected final double[][] a;
    protected final Sign[] signs;
    protected final double[] b;
    protected final List<Integer> freeVars;

    // for standard LP
    private int n2;
    private double[] c2;
    private double[][] a2;
    private double[] x2 = new double[0];

    /**
     * Objective value
     */
    protected double objective = 0d;

    /**
     * The vector X
     */
    protected final double[] x;

    protected int precision = DEFAULT_PRECISION;

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
     */
    public GeneralLP(ObjectiveType objectiveType, double c0, double[] c, double[][] a, Sign[] signs, double[] b, int[] freeVars) {
        this.m = a.length;
        this.n = a[0].length;

        // check length
        if (m != b.length) {
            throw new IllegalArgumentException("m not matched");
        }
        if (n != c.length) {
            throw new IllegalArgumentException("n not matched");
        }
        if (m != signs.length) {
            throw new IllegalArgumentException("wrong length of signs");
        }

        this.objectiveType = objectiveType;
        this.c0 = c0;
        this.c = c;
        this.a = a;
        this.signs = signs;
        this.b = b;
        this.freeVars = Maths.unique(freeVars);
        LOG.debug("free vars", this.freeVars);

        this.x = new double[n];
    }

    protected int iterations = 0;
    protected State state = State.ZERO;

    @Override
    public void solve() {
        LOG.debug(this);
        standardize();

        Simplex simplex = new Simplex(c2, a2, b);
        simplex.setPrecision(this.precision);
        simplex.solve();

        this.iterations = simplex.getIterations();
        this.state = simplex.getState();
        setXnObjective(simplex);
        LOG.debug(this);
    }

    private void setXnObjective(Simplex simplex) {
        if (!simplex.getState().isSolved()) {
            return;
        }

        this.objective = c0 + (objectiveType.isMax() ? simplex.getMax() : -simplex.getMax());
        // X
        this.x2 = simplex.getX();
        System.arraycopy(x2, 0, this.x, 0, n);
        for (int i = 0; i < freeVars.size(); i++) {
            int k = freeVars.get(i); // for x_k
            this.x[k - 1] -= x2[n + i];
        }
    }

    private void standardize() {
        // compute n2 for standard LP
        computeN2();

        // compute c2 & a2
        computeC2A2();
    }

    private void computeN2() {
        this.n2 = n + freeVars.size();
        for (Sign sign : this.signs) {
            if (sign == null) {
                throw new IllegalArgumentException("null sign");
            }
            if (!sign.isEquality()) { // slack/surplus
                this.n2++;
            }
        }
        LOG.debug("n2", n2);
    }

    private void computeC2A2() {
        // init c2 & a2
        this.c2 = Arrays.copyOf(c, n2); // newLength == n2
        this.a2 = new double[m][]; // a2[i] == null
        for (int i = 0; i < m; i++) {
            this.a2[i] = Arrays.copyOf(a[i], n2);
        }

        // if minimize
        if (!this.objectiveType.isMax()) {
            for (int j = 0; j < n; j++) {
                this.c2[j] *= -1;
            }
        }

        // for free vars
        for (int j = 0; j < freeVars.size(); j++) {
            int k = freeVars.get(j); // free var: x_k
            // check range
            if (k < 1 || k > n) {
                throw new IllegalArgumentException("free var out of range: " + k);
            }

            this.c2[n + j] = -c2[k - 1];
            for (int i = 0; i < m; i++) { // for each row
                this.a2[i][n + j] = -a[i][k - 1];
            }
        }

        // for slack/surplus vars
        for (int i = 0, j = n + freeVars.size(); i < m; i++) {
            Sign sign = signs[i];
            if (Sign.LE == sign) { // slack
                this.a2[i][j] = 1;
            } else if (Sign.GE == sign) { // surplus
                this.a2[i][j] = -1;
            } else {
                continue;
            }
            j++;
        }

        LOG.debug("c2", c2);
        LOG.debug("a2", a2);
    }

    @Override
    public ObjectiveType getObjectiveType() {
        return objectiveType;
    }

    @Override
    public double getObjective() {
        return objective;
    }

    @Override
    public double[] getX() {
        return x;
    }

    public double[] getX2() {
        return x2;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public int getIterations() {
        return iterations;
    }

    @Override
    public void setPrecision(int precision) {
        this.precision = precision;
    }

    @Override
    public int getPrecision() {
        return precision;
    }

    protected void toStringExtra(StringBuilder b) {
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(getClass().getSimpleName());
        b.append(" {");
        b.append("m=").append(m).append(' ');
        b.append("n=").append(n).append(' ');
        b.append(objectiveType).append("=").append(objective);
        b.append('\n').append(" iter=").append(iterations);
        b.append(" state=").append(state);
        b.append("\n n2=").append(n2);
        b.append(" x2=").append(Arrays.toString(x2));
        b.append("\n freeVars=").append(freeVars);
        this.toStringExtra(b);
        b.append('\n').append(" x=").append(Arrays.toString(x));
        b.append(" c0=").append(c0);

        // print c
        b.append('\n').append(" [");
        for (int j = 0; j < n; j++) {
            b.append(freeVars.contains(j + 1) ? '*' : ' ');
            b.append(String.format("%-8.3f", c[j])).append(' ');
        }
        b.append(" =  ").append(objectiveType).append("-c0\n  ");
        // hr
        for (int j = 0; j < n; j++) {
            b.append(" -----    ");
        }
        b.append(" +  -----\n  ");

        // print a
        for (int i = 0; i < m; i++) {
            // end line
            if (i > 0) {
                b.append("\n  ");
            }

            // print a[i]
            for (int j = 0; j < n; j++) {
                b.append(String.format(" %-8.3f", a[i][j])).append(' ');
            }
            b.append(String.format(" %-2s  ", signs[i].getString())).append(this.b[i]);
        }
        b.append(']');

        return b.append('\n').append('}').toString();
    }
}