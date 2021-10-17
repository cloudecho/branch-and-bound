package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.util.Log;
import com.github.cloudecho.bnb.util.LogFactory;
import com.github.cloudecho.bnb.util.Maths;

import java.util.Arrays;

/**
 * Primal Simplex.
 * <pre>
 * max z = CX
 * s.t. AX = b
 *      X >= 0
 *
 * C = (c1 ... c_n)
 * X = (x1 ... x_n)'
 * A = [ a11 ... a1n
 *       ... ... ...
 *      a_m1 ... a_mn]
 * b = (b1 ... b_m)'
 * </pre>
 * <p>
 * Simplex table:
 * <pre>
 *   x1  ... x_n  | b
 *   c1  ... c_n  | -z
 *  ---  --- ---  + ---
 *  a11  ... a1n  | b1
 *  ...      ...  | ...
 *  a_m1 ... a_mn | b_m
 *
 *  i.e.
 *  [C -z
 *   A b]
 * </pre>
 *
 * @see <a href="https://math.mit.edu/~goemans/18310S15/lpnotes310.pdf">
 * The lecture notes of Linear Programming by Michel Goemans</a>
 */
public class Simplex implements Solver {
    static final Log LOG = LogFactory.getLog(Simplex.class);

    private final int m;
    private final int n;
    private final double[][] table; // double[m+1][n+1]
    private final int[] base; // int[m]

    private int m2;
    private int n2;

    /**
     * Objective = max
     */
    private double max = 0d;

    /**
     * The vector X
     */
    private final double[] x;

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
        this.m2 = m;
        this.n2 = n;

        // check length
        if (m != b.length) {
            throw new IllegalArgumentException("m not matched");
        }
        if (n != c.length) {
            throw new IllegalArgumentException("n not matched");
        }

        this.x = new double[n];
        this.base = new int[m];
        this.table = new double[m + 1][];
        final int maxCols = n + 1 + m; // m aVars reserved

        // table[0]
        this.table[0] = Arrays.copyOf(c, maxCols);

        // table[1..m+1]
        for (int i = 1; i <= m; i++) { // for each row
            this.table[i] = Arrays.copyOf(a[i - 1], maxCols);
            this.table[i][n] = b[i - 1];
        }
    }

    private int iterations = 0;
    private State state = State.ZERO;

    @Override
    public void solve() {
        this.state = State.SOLVING;
        this.preprocess();
        LOG.debug("preprocess", this);

        this.initBase();
        LOG.debug("success to init base");
        LOG.debug(this);

        while (!this.pivot()) ;
        if (State.SOLVING == this.state) {
            while (!this.pivotOnNegative()) ;
        }
        this.setXnMax();
        if (State.SOLVING == this.state) {
            this.state = State.SOLVED;
        }

        LOG.debug(this);
    }

    /**
     * Return {@code true} if STOP
     */
    private boolean pivotOnNegative() {
        boolean b = true;
        for (int i = 1; i <= m; i++) {
            if (table[i][n] >= 0) {
                continue;
            }
            // pivot on negative number
            final int j = indexOfMinRatioColumn(i);
            if (-1 == j) { // not found
                continue;
            }
            LOG.debug("pivot (", i, j, ") on negative", table[i][j]);
            this.iterations++;
            b = false;
            pivot(i, j);
            LOG.debug(this);
        }
        return b;
    }

    private int indexOfMinRatioColumn(int r) {
        double minr = 0;
        int w = -1; // not found
        for (int j = 0; j < n; j++) {
            if (table[r][j] >= 0 || Maths.contains(base, j)) {
                continue;
            }
            final double ratio = table[0][j] / table[r][j];
            if (minr > ratio || -1 == w) {
                minr = ratio;
                w = j;
            }
        }
        return w;
    }

    @Override
    public State getState() {
        return state;
    }

    private void setXnMax() {
        this.max = Maths.round(-table[0][n], precision);

        for (int i = 0; i < m2; i++) {
            double b = Maths.round(table[i + 1][n], precision); // b
            int j = base[i];
            if (j > n || b < 0) { // aVar || not feasible
                this.state = State.NO_SOLUTION;
            }
            if (j < n) {
                this.x[j] = b;
            }
        }
    }

    private void preprocess() {
        for (int i = 0; i < m; i++) {
            this.base[i] = -1;
        }

        // for each row, except 0-th
        for (int i = 1; i <= m; i++) {
            double b = table[i][n];
            if (b > 0d || b == 0d && existsPositiveNum(table[i], n)) {
                continue;
            }
            // b < 0d || no positive number in this row
            for (int j = 0; j <= n; j++) {
                table[i][j] *= -1;
            }
        }
    }

    private boolean existsPositiveNum(double[] data, int endIndex) {
        for (int i = 0; i < endIndex; i++) {
            if (data[i] > 0) {
                return true;
            }
        }
        return false;
    }

    private int nonZeroNum(double[] data, int endIndex) {
        int count = 0;
        for (int i = 0; i < endIndex; i++) {
            if (data[i] != 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * The number of artificial variables
     */
    private int nAvars = 0;

    private void initBase() {
        int[] vars = arr0to(n);
        int count = 0;
        for (int j = n - 1; j >= 0 && count < m; j--) {
            int w = baseVar(j);
            if (w > -1 && base[w] < 0) {
                base[w] = j; // j is selected
                vars[j] = -1; // mark selected
                gaussian(w + 1, j);
                count++;
            }
        }

        if (count < m) {
            for (int j = 0; j < n; j++) {
                if (vars[j] < 0) { // j selected
                    continue;
                }
                int r = indexOfMinRatio(j);
                if (r < 0 || base[r - 1] >= 0) {
                    continue;
                }
                LOG.debug("base", 'r', r, "var", j);
                base[r - 1] = j;
                // vars[j] = -1; // mark selected
                gaussian(r, j);
                count++;
            }
        }

        nAvars = m - count;
        addAvars();
    }

    private void addAvars() {
        if (nAvars == 0) {
            return;
        }
        n2 = n;
        for (int i = 0; i < m; i++) {
            if (base[i] >= 0) {
                continue;
            }
            ++n2;
            LOG.debug("base", 'i', i, "aVar", n2);
            base[i] = n2;
            table[i + 1][n2] = 1;
        }
    }

    private void gaussian(final int r, final int c) {
        double v = table[r][c];
        if (0d == v) {
            return;
        }

        normalize(r, c);

        // for each row except r-th
        for (int i = 0; i <= m2; i++) {
            if (r == i) {
                continue;
            }
            v = table[i][c];
            if (0d == v) {
                continue;
            }

            // for each element in this row
            for (int j = 0; j <= n2; j++) {
                double v2 = -v * table[r][j] + table[i][j];
                table[i][j] = Double.isNaN(v2) ? 0 : v2;
            }
        }
    }

    private void normalize(final int r, final int c) {
        final double v = table[r][c];
        if (1d == v || 0d == v) {
            return;
        }

        for (int j = 0; j <= n2; j++) {
            table[r][j] /= v;
        }
    }

    private static int[] arr0to(int k) {
        int[] arr = new int[k];
        for (int j = 0; j < k; j++) {
            arr[j] = j;
        }
        return arr;
    }

    private int baseVar(int j) {
        int w = -1;
        int count = 0;
        for (int i = 1; i <= m2 && count < 2; i++) {
            if (table[i][j] > 0d) {
                count++;
                w = i - 1;
            }
        }

        return 1 == count ? w : -1;
    }

    /**
     * Return {@code true} if STOP
     */
    private boolean pivot() {
        final int w = indexOfMaxc();
        final double maxc = table[0][w];
        if (LOG.isDebugEnabled()) {
            LOG.debug("iter=" + iterations, "e=" + w, "maxc=" + Maths.round(maxc, precision));
        }
        if (maxc <= 0) {
            return this.driveAvars();
        }

        this.iterations++;

        // w enter base
        int r = indexOfMinRatio(w);
        // not found, i.e. each of table[][w] <=0, unbounded
        if (-1 == r) {
            this.state = State.UNBOUNDED;
            return this.driveAvars();
        }
        pivot(r, w);
        LOG.debug(this);

        return false;
    }

    private void pivot(int r, int c) {
        gaussian(r, c);
        base[r - 1] = c;
    }

    /**
     * Return {@code true} if STOP
     */
    private boolean driveAvars() {
        boolean b = true;
        for (int r = 1; r <= m2; r++) {
            if (base[r - 1] < n) { // non-aVar
                continue;
            }
            for (int j = 0; j < n; j++) {
                if (0d == table[r][j] || (table[r][j] < 0d && table[r][n] > 0d)) {
                    continue;
                }
                LOG.debug("driving aVar", base[r - 1]);
                b = false;
                pivot(r, j);
                LOG.debug(this);
                break;
            }
        }
        if (b) {
            b = !removeZeroRow();
        }
        this.n2 = n; // discard aVars
        return b;
    }

    private boolean removeZeroRow() {
        boolean removed = false;
        for (int r = 1; r <= m2; r++) {
            if (base[r - 1] < n) { // non-aVar
                continue;
            }
            if (0 == nonZeroNum(table[r], n + 1)) {
                // remove r-th row
                for (int i = r; i < m2; i++) {
                    table[i] = table[i + 1];
                    base[i - 1] = base[i];
                }
                base[m2 - 1] = -1;
                this.m2--;
                removed = true;
                LOG.debug("removeZeroRow", r);
            }
        }
        return removed;
    }

    private int indexOfMaxc() {
        double maxc = table[0][0];
        int w = 0;
        for (int j = 1; j < n2; j++) {
            if (n == j) { // b column
                continue;
            }
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
        for (int i = 1; i <= m2; i++) {
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

    @Override
    public double[] getX() {
        return this.x;
    }

    public double getMax() {
        return this.max;
    }

    @Override
    public double getObjective() {
        return this.getMax();
    }

    @Override
    public ObjectiveType getObjectiveType() {
        return ObjectiveType.max;
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

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("Simplex {");
        b.append("m=").append(m).append(' ');
        b.append("n=").append(n).append(' ');
        b.append("max=").append(max);
        b.append('\n').append(" iter=").append(iterations);
        b.append(" base=").append(Arrays.toString(base));
        b.append(" nAvars=").append(nAvars);
        b.append(" state=").append(state);
        b.append('\n').append(" x=").append(Arrays.toString(x));

        // table
        b.append('\n').append(" [ ");
        for (int j = 0; j <= n2; j++) {
            b.append(n == j ? " |  " : ' ');
            b.append(String.format("%-9d", j));
        }
        for (int i = 0; i <= m2; i++) {
            // end line
            b.append('\n');

            // hr
            if (1 == i) {
                b.append("   ");
                for (int j = 0; j < n; j++) {
                    b.append(" -----    ");
                }
                b.append(" +  -----\n");
            }
            // row number
            b.append(String.format("%2d:", i));
            // print table[i]
            for (int j = 0; j <= n2; j++) {
                if (n == j) {
                    b.append(" | ");
                }
                b.append((i > 0 && j == base[i - 1]) ? '*' : ' '); // base var
                b.append(String.format("%-8.3f", table[i][j])).append(' ');
            }
        }
        b.setCharAt(b.length() - 1, ']');

        return b.append('\n').append('}').toString();
    }
}
