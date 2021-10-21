package com.github.cloudecho.bnb.math;

import java.util.Arrays;

public class RevisedDoubleMatrix extends AbstractMatrix<Double> {
    /**
     * table[j] represents column j of the matrix,
     * table[j][i] represents the element (i,j) of the matrix
     */
    protected double[][] table;

    /**
     * suppose table[j] = [0 ... 1 ... 0]' ,
     * identityColumn[j] is the index of value 1
     */
    protected final int[] identityColumn;

    public RevisedDoubleMatrix(double[][] table, int max_n) {
        super(table, max_n);
        this.table = transpose(table);
        identityColumn = new int[max_n];
        Arrays.fill(identityColumn, -1);
    }

    double[][] transpose(double[][] table) {
        double[][] t = new double[n][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                t[j][i] = table[i][j];
            }
        }
        return t;
    }

    @Override
    public Double get(int r, int c) {
        final int i = identityColumn[c];
        if (i > 0) {
            return i == r ? 1d : 0d;
        }
        return column(c)[r];
    }

    @Override
    public double[] getRow(int r) {
        double[] row = new double[m];
        for (int j = 0; j < n; j++) {
            row[j] = this.get(r, j);
        }
        return row;
    }

    @Override
    public void setRow(int r, Object rowData) {
        double[] row = (double[]) rowData;
        for (int j = 0; j < n; j++) {
            this.set(r, j, row[j]);
        }
    }

    @Override
    public void setRowTo(int r, int r2) {
        for (int j = 0; j < n; j++) {
            set(r, j, get(r2, j));
        }
    }

    @Override
    public void set(int r, int c, Number num) {
        final double v = num.doubleValue();
        final int i = identityColumn[c];
        if (i > 0) {
            if (1d == v && i == r || 0d == v && i != r) {
                return;
            }
            identityColumn[c] = -1;
            table[c] = new double[m];
        }
        column(c)[r] = v;
    }

    @Override
    public void gaussian(int r, int c) {
        double v = get(r, c);
        if (0d == v) {
            return;
        }

        normalize(r, c);

        // for each row except r-th
        for (int i = 0; i < m; i++) {
            if (r == i) {
                continue;
            }
            v = get(i, c);
            if (0d == v) {
                continue;
            }

            // for each element in this row
            for (int j = 0; j < n; j++) {
                double v2 = -v * get(r, j) + get(i, j);
                set(i, j, Double.isNaN(v2) ? 0 : v2);
            }
        }

        identityColumn(r, c);
    }

    @Override
    public void normalize(int r, int c) {
        final double v = get(r, c);
        if (1d == v || 0d == v) {
            return;
        }

        for (int j = 0; j < n; j++) {
            set(r, j, get(r, j) / v);
        }
    }

    private void identityColumn(int r, int c) {
        identityColumn[c] = r;
        table[c] = null;
    }

    @Override
    public void negate(int r, int c) {
        final int i = identityColumn[c];
        if (i > 0) {
            table[c] = new double[m];
            if (i == r) {
                table[c][r] = -1d;
                return;
            }
        }
        column(c)[r] *= -1d;
    }

    @Override
    public Double divide(int r1, int c1, int r2, int c2) {
        return get(r1, c1) / get(r2, c2);
    }

    @Override
    public boolean isPositive(int r, int c) {
        final int i = identityColumn[c];
        if (i > 0) {
            return i == r;
        }
        return column(c)[r] > 0d;
    }

    @Override
    public boolean isNegative(int r, int c) {
        final int i = identityColumn[c];
        if (i > 0) {
            return false;
        }
        return column(c)[r] < 0d;
    }

    @Override
    public boolean isZero(int r, int c) {
        final int i = identityColumn[c];
        if (i > 0) {
            return i != r;
        }
        return column(c)[r] == 0d;
    }

    public boolean existsPositiveInRow(int r, int endIndex) {
        for (int j = 0; j < endIndex; j++) {
            if (get(r, j) > 0d) {
                return true;
            }
        }
        return false;
    }

    public boolean existsNonZeroInRow(int r, int endIndex) {
        for (int j = 0; j < endIndex; j++) {
            if (!isZero(r, j)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compare(int r1, int c1, int r2, int c2) {
        return Double.compare(get(r1, c1), get(r2, c2));
    }

    @Override
    protected void extendColumn() {
        int nGrow = growColumn(n - table.length);
        if (nGrow > 0) {
            table = Arrays.copyOf(table, n2);
        } else if (nGrow < 0) {
            for (int j = n; j < n2; j++) {
                table[j] = null;
            }
        }
    }

    private double[] column(int c) {
        // lazy initialization
        if (table[c] == null) {
            table[c] = new double[m];
        }
        return table[c];
    }
}
