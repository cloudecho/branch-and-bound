package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.math.DoubleMatrix;
import com.github.cloudecho.bnb.math.Matrix;
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
    private final Matrix<?> matrix; // double[m+1][n+1+m]
    private final int[] base; // int[m]

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
    protected Simplex(double[] c, double[][] a, double[] b) {
        this.m = a.length;
        this.n = a[0].length;

        this.cycling = new C(n);

        // check length
        if (m != b.length) {
            throw new IllegalArgumentException("m not matched");
        }
        if (n != c.length) {
            throw new IllegalArgumentException("n not matched");
        }

        this.x = new double[n];
        this.base = new int[m];
        final double[][] table = new double[m + 1][];
        final int maxCols = n + 1 + m; // m aVars reserved

        // table[0]
        table[0] = Arrays.copyOf(c, maxCols);

        // table[1..m+1]
        for (int i = 1; i <= m; i++) { // for each row
            table[i] = Arrays.copyOf(a[i - 1], maxCols);
            table[i][n] = b[i - 1];
        }

        this.matrix = createMatrix(table);
        this.matrix.setRows(m + 1);
        this.matrix.setColumns(n + 1);
    }

    protected Matrix<?> createMatrix(double[][] table) {
        return new DoubleMatrix(table);
    }

    private int iterations = 0;
    private State state = State.ZERO;

    @Override
    public void solve() {
        this.state = State.SOLVING;
        this.cycling.reset();
        this.preprocess();
        LOG.trace("preprocess", this);

        this.initBase();
        LOG.debug("success to init base");
        LOG.trace(this);

        while (this.pivot()) ;
        if (State.SOLVING == this.state) {
            this.cycling.reset();
            while (this.pivotOnNegative()) ;
        }
        this.setXnMax();
        if (State.SOLVING == this.state) {
            this.state = State.SOLVED;
        }

        LOG.trace(this);
    }

    /**
     * Return {@code true} if continue
     */
    private boolean pivotOnNegative() {
        boolean goOn = false;
        for (int i = 1; i <= m; i++) {
            if (matrix.nonNegative(i, n)) {
                continue;
            }
            // pivot on negative number
            final int j = indexOfMinRatioColumn(i);
            if (-1 == j) { // not found
                continue;
            }

            if (cycling.inc(i)) {
                LOG.debug("cycling2 detected", '(', i, j, ')');
                this.state = State.NO_SOLUTION;
                return false; // cycling
            }

            LOG.debug("iter", iterations, "pivot (", i, j, ") on negative", matrix.get(i, j), 'b', matrix.get(i, n));
            this.iterations++;
            goOn = true;
            pivot(i, j);
            LOG.trace(this);
        }
        return goOn;
    }

    private int indexOfMinRatioColumn(int r) {
        double minr = 0;
        int w = -1; // not found
        for (int j = 0; j < n; j++) {
            if (matrix.nonNegative(r, j) || Maths.contains(base, j)) {
                continue;
            }
            final double ratio = matrix.divide(0, j, r, j).doubleValue();
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
        this.max = Maths.round(-matrix.get(0, n).doubleValue(), precision);

        for (int i = 0; i < m2(); i++) {
            double b = Maths.round(matrix.get(i + 1, n), precision); // b
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
            // Number b = matrix.get(i, n)
            if (matrix.isPositive(i, n) || matrix.isZero(i, n) && matrix.existsPositiveInRow(i, n)) {
                continue;
            }
            // b < 0d || no positive number in this row
            for (int j = 0; j <= n; j++) {
                matrix.negate(i, j);
            }
        }
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
                matrix.gaussian(w + 1, j);
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
                LOG.trace("base", 'r', r, "var", j);
                base[r - 1] = j;
                // vars[j] = -1; // mark selected
                matrix.gaussian(r, j);
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
        matrix.setColumns(n + 1);
        for (int i = 0; i < m; i++) {
            if (base[i] >= 0) {
                continue;
            }
            matrix.increaseColumns();
            LOG.trace("base", 'i', i, "aVar", n2());
            base[i] = n2();
            matrix.set(i + 1, n2(), 1d);
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
        for (int i = 1; i <= m2() && count < 2; i++) {
            if (matrix.isPositive(i, j)) {
                count++;
                w = i - 1;
            }
        }

        return 1 == count ? w : -1;
    }

    /**
     * Return {@code true} if continue
     */
    private boolean pivot() {
        final int w = indexOfMaxc();
        if (LOG.isDebugEnabled()) {
            LOG.debug("iter=" + iterations, "e=" + w, "maxc=" + Maths.round(matrix.get(0, w), precision));
        }
        if (matrix.nonPositive(0, w)) {
            return this.driveAvars();
        }

        // w enter base
        int r = indexOfMinRatio(w);
        // not found, i.e. each of table[][w] <=0, unbounded
        if (-1 == r) {
            this.state = State.UNBOUNDED;
            return this.driveAvars();
        }

        // detect cycling
        if (w < n && cycling.inc(w)) {
            LOG.debug("cycling detected", '(', r, w, ')');
            this.state = State.NO_SOLUTION;
            return false;
        }

        this.iterations++;
        pivot(r, w);
        LOG.trace(this);

        return true;
    }

    private void pivot(int r, int c) {
        matrix.gaussian(r, c);
        base[r - 1] = c;
    }

    /**
     * Return {@code true} if continue
     */
    private boolean driveAvars() {
        boolean goOn = false;
        for (int r = 1; r <= m2(); r++) {
            if (base[r - 1] < n) { // non-aVar
                continue;
            }
            for (int j = 0; j < n; j++) {
                if (matrix.isZero(r, j) || (matrix.isNegative(r, j) && matrix.isPositive(r, n))) {
                    continue;
                }
                LOG.debug("driving aVar", base[r - 1]);
                goOn = true;
                pivot(r, j);
                LOG.trace(this);
                break;
            }
        }
        if (!goOn) {
            removeZeroRow();
        }
        matrix.setColumns(n + 1); // discard aVars
        return goOn;
    }

    private void removeZeroRow() {
        for (int r = 1; r <= m2(); r++) {
            if (base[r - 1] < n) { // non-aVar
                continue;
            }
            if (!matrix.existsNonZeroInRow(r, n + 1)) {
                // remove r-th row
                for (int i = r; i < m2(); i++) {
                    matrix.setRowTo(i, i + 1);
                    base[i - 1] = base[i];
                }
                base[m2() - 1] = -1;
                matrix.decreaseRows();
                LOG.debug("removeZeroRow", r);
            }
        }
    }

    private static final String CYCLING_THRESHOLD_PROP = "com.github.cloudecho.bnb.CYCLING_THRESHOLD";
    private static final int CYCLING_THRESHOLD = Integer.parseInt(System.getProperty(CYCLING_THRESHOLD_PROP, "5"));

    private final C cycling;

    private static class C {
        final int[] counts;
        final int size;
        int maxCount = 0;
        int reached = 0;

        C(int size) {
            this.size = size;
            this.counts = new int[size];
        }

        boolean inc(final int which) {
            if (++counts[which] == CYCLING_THRESHOLD) {
                reached++;
            }
            if (maxCount < counts[which]) {
                maxCount = counts[which];
            }
            return (reached >= size / 2) || (maxCount >= 10 * CYCLING_THRESHOLD);
        }

        void reset() {
            maxCount = 0;
            reached = 0;
            Arrays.fill(counts, 0);
        }
    }

    private int indexOfMaxc() {
//        Number maxc = matrix.get(0, 0);
        int w = 0;
        for (int j = 1; j < n2(); j++) {
            if (n == j) { // b column
                continue;
            }
            if (matrix.compare(0, w, 0, j) < 0) {
                w = j;
            }
        }
        return w;
    }

    private int indexOfMinRatio(int c) {
        double minv = 0;
        int w = -1;
        for (int i = 1; i <= m2(); i++) {
            // double v = matrix.get(i, c);
            if (matrix.nonPositive(i, c)) {
                continue;
            }
            double v = matrix.divide(i, n, i, c).doubleValue(); // i.e. b/a(i,c)
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
        StringBuilder b = new StringBuilder(getClass().getSimpleName());
        b.append('{');
        b.append("m=").append(m).append(' ');
        b.append("n=").append(n).append(' ');
        b.append("max=").append(max);
        b.append('\n').append(" iter=").append(iterations);
        b.append(" base=").append(Arrays.toString(base));
        b.append(" nAvars=").append(nAvars);
        b.append(" state=").append(state);
        b.append('\n').append(" x=").append(Arrays.toString(x));

        // table
        b.append('\n').append(" [  ");
        for (int j = 0; j <= n2(); j++) {
            b.append(n == j ? " |  " : ' ');
            b.append(String.format("%-9d", j));
        }
        for (int i = 0; i <= m2(); i++) {
            // end line
            b.append('\n');

            // hr
            if (1 == i) {
                b.append("    ");
                for (int j = 0; j < n; j++) {
                    b.append(" -----    ");
                }
                b.append(" +  -----\n");
            }
            // row number
            b.append(String.format("%3d:", i));
            // print table[i]
            for (int j = 0; j <= n2(); j++) {
                if (n == j) {
                    b.append(" | ");
                }
                b.append((i > 0 && j == base[i - 1]) ? '*' : ' '); // base var
                b.append(String.format("%-8.3f", matrix.get(i, j).doubleValue())).append(' ');
            }
        }
        b.setCharAt(b.length() - 1, ']');

        return b.append('\n').append('}').toString();
    }

    protected int m2() {
        return matrix.getRows() - 1;
    }

    protected int n2() {
        return matrix.getColumns() - 1;
    }
}
