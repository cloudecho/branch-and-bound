package com.github.cloudecho.bnb;

import java.util.Arrays;

/**
 * Primal Simplex.
 * <pre>
 * max z = CX
 * s.t. AX = b
 *      X >= 0
 *
 * C = (c1 c2 ... c_n)
 * X = (x1 x2 ... x_n)'
 * A = [a11 a12 ... a1n
 *      a21 a22 ... a2n
 *      ... ... ...
 *      a_m1 a_m2 ... a_mn]
 * b = (b1 b2 ... b_m)'
 * </pre>
 * <p>
 * Simplex table:
 * <pre>
 *  x1  x2  ... x_n  b
 *  --  --  --- ---  --
 *  c1  c2  ... c_n  -z
 *  a11 a12 ... a1n  b1
 *  a21 a22 ... a2n  b2
 *  ... ... ...
 *  a_m1 a_m2 ... a_mn b_m
 *
 *  i.e.
 *  [C -z
 *   A b]
 * </pre>
 *
 * @see <a href="https://math.mit.edu/~goemans/18310S15/lpnotes310.pdf">
 * The lecture notes of Linear Programming by Michel Goemans</a>
 */
public class Simplex {
    static final Log LOG = LogFactory.getLog(Simplex.class);
    static final int DEFAULT_PRECISION = 7;

    private final int m;
    private final int n;
    private final double[][] table; // double[m+1][n+1]
    private final int[] base; // int[m]

    /**
     * Objective = max
     */
    private double max = 0d;

    /**
     * The vector X
     */
    private double[] x;

    private int precision = DEFAULT_PRECISION;

    /**
     * Constructor.
     *
     * @param c The coefficient vector C, 1 row, n columns
     * @param a The matrix A, m rows, n columns
     * @param b The vector b, m rows, 1 column
     */
    public Simplex(double[] c, double[][] a, double[] b) {
        this.m = a.length;
        this.n = a[0].length;

        // check length
        if (m != b.length) {
            throw new IllegalArgumentException("m not matched");
        }
        if (n != c.length) {
            throw new IllegalArgumentException("n not matched");
        }

        this.x = new double[n];
        this.base = new int[m];
        this.table = new double[m + 1][n + 1];

        // table[0]
        for (int j = 0; j < n; j++) {
            this.table[0][j] = c[j];
        }
        // table[1..m+1]
        for (int i = 1; i <= m; i++) { // for each row
            for (int j = 0; j < n; j++) {
                this.table[i][j] = a[i - 1][j];
            }
            this.table[i][n] = b[i - 1];
        }
    }

    private int iterations = 0;
    private State state = State.ZERO;

    public void solve() {
        this.state = State.SOLVING;
        this.preprocess();

        if (this.initBase()) {
            LOG.debug("success to init base");
            this.gaussian();
            LOG.debug(this);

            while (!this.pivot()) ;
            this.setXnMax();
            if (State.SOLVING == this.state) {
                this.state = State.SOLVED;
            }
        } else {
            LOG.debug("fail to init base");
            this.state = State.NO_SOLUTION;
        }

        LOG.debug(this);
    }

    public State getState() {
        return state;
    }

    private void setXnMax() {
        this.max = Maths.round(-table[0][n], precision);

        for (int i = 0; i < m; i++) {
            int j = base[i];
            this.x[j] = Maths.round(table[i + 1][n], precision); // b
        }
    }

    private void preprocess() {
        // for each row, except 0-th
        for (int i = 1; i <= m; i++) {
            double b = table[i][n];
            if (b >= 0d) {
                continue;
            }
            for (int j = 0; j <= n; j++) {
                table[i][j] *= -1;
            }
        }
    }

    private boolean initBase() {
        for (int i = 0; i < m; i++) {
            this.base[i] = -1;
        }

        int[] vars = arr0to(n);
        int count = 0;
        for (int j = n - 1; j >= 0 && count < m; j--) {
            int w = baseVar(j);
            if (w > -1 && base[w] < 0) {
                base[w] = j; // j is selected
                vars[j] = -1; // mark selected
                count++;
            }
        }

        if (count < m) {
            for (int i = 0; i < m; i++) {
                if (base[i] < 0) {
                    int k = nextVar(vars);
                    if (k < 0) { // -1
                        return false;
                    }
                    base[i] = k;
                    count++;
                }
            }
        }

        return count == m;
    }

    private void gaussian() {
        for (int i = 1; i <= m; i++) {
            gaussian(i, base[i - 1]);
        }
    }

    private void gaussian(final int r, final int c) {
        double v = table[r][c];
        if (0d == v) {
            return;
        }

        normalize(r, c);

        // for each row except r-th
        for (int i = 0; i <= m; i++) {
            if (r == i) {
                continue;
            }
            v = table[i][c];
            if (0d == v) {
                continue;
            }

            // for each element in this row
            for (int j = 0; j <= n; j++) {
                table[i][j] += -v * table[r][j];
            }
        }
    }

    private void normalize(final int r, final int c) {
        final double v = table[r][c];
        if (1d == v || 0d == v) {
            return;
        }

        for (int j = 0; j <= n; j++) {
            table[r][j] /= v;
        }
    }

    private int nextVar(int[] vars) {
        for (int j = 0; j < vars.length; j++) {
            if (vars[j] >= 0) {
                vars[j] = -1; // mark selected
                if (hasPositive(j)) {
                    return j; // as j == vars[j]
                }
            }
        }
        // not found
        return -1;
    }

    private boolean hasPositive(int j) {
        for (int i = 1; i <= m; i++) {
            if (table[i][j] > 0d) {
                return true;
            }
        }
        return false;
    }

    private static int[] arr0to(int k) {
        int[] arr = new int[k];
        for (int i = 0; i < k; i++) {
            arr[i] = i;
        }
        return arr;
    }

    private int baseVar(int j) {
        int w = -1;
        int count = 0;
        for (int i = 1; i <= m && count < 2; i++) {
            if (table[i][j] > 0d) {
                count++;
                w = i - 1;
            }
        }

        return 1 == count ? w : -1;
    }

    /**
     * Return {@true} if STOP
     */
    private boolean pivot() {
        final int w = indexOfMaxc();
        final double maxc = table[0][w];
        LOG.debug("iter=" + iterations, "w=" + w, "maxc=" + Maths.round(maxc, precision));
        if (maxc <= 0) {
            return true;
        }

        this.iterations++;

        // w enter base
        int r = indexOfMinRatio(w);
        // not found, i.e. each of table[][w] <=0, unbounded
        if (-1 == r) {
            this.state = State.UNBOUNDED;
            return true;
        }
        base[r - 1] = w;

        gaussian(r, w);
        LOG.debug(this);

        return false;
    }


    private int indexOfMaxc() {
        double maxc = table[0][0];
        int w = 0;
        for (int j = 1; j < n; j++) {
            if (maxc < table[0][j]) {
                maxc = table[0][j];
                w = j;
            }
        }
        return w;
    }

    private int indexOfMinRatio(int c) {
        double minv = 0;
        int w = -1;
        for (int i = 1; i <= m; i++) {
            double v = table[i][c];
            if (v <= 0d) {
                continue;
            }
            v = table[i][n] / v; // i.e. b/v
            if (minv > v || -1 == w) {
                minv = v;
                w = i;
            } else if (minv == v) {
                // Bland’s anticycling pivoting rule
                if (base[w - 1] > base[i - 1]) {
                    // base[i-1] leave
                    w = i;
                }
            }
        }
        return w;
    }

    public double[] getX() {
        return this.x;
    }

    public double getMax() {
        return this.max;
    }

    public int getIterations() {
        return iterations;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("Simplex {");
        b.append("m=").append(m).append(' ');
        b.append("n=").append(n).append(' ');
        b.append("max=").append(max);
        b.append('\n').append(" iter=").append(iterations);
        b.append(" base=").append(Arrays.toString(base));
        b.append(" state=").append(state);
        b.append('\n').append(" x=").append(Arrays.toString(x));

        // table
        b.append('\n').append(" [");
        for (int i = 0; i < table.length; i++) {
            if (i > 0) {
                b.append("\n  ");
            }
            for (double d : table[i]) {
                b.append(String.format("%-8.3f", d)).append(' ');
            }
        }
        b.setCharAt(b.length() - 1, ']');

        return b.append('\n').append('}').toString();
    }
}
