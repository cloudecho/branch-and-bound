package com.github.cloudecho.bnb.math;

import java.util.Arrays;

public class DoubleMatrix extends AbstractMatrix<Double> {
    protected final double[][] table;

    public DoubleMatrix(double[][] table, int max_n) {
        super(table, max_n);
        this.table = table;
    }

    @Override
    public Double get(int r, int c) {
        return table[r][c];
    }

    @Override
    public double[] getRow(int r) {
        return table[r];
    }

    @Override
    public void setRow(int r, Object rowData) {
        table[r] = (double[]) rowData;
    }

    @Override
    public void set(int r, int c, Number num) {
        table[r][c] = num.doubleValue();
    }

    @Override
    public void gaussian(int r, int c) {
        double v = table[r][c];
        if (0d == v) {
            return;
        }

        normalize(r, c);

        // for each row except r-th
        for (int i = 0; i < m; i++) {
            if (r == i) {
                continue;
            }
            v = table[i][c];
            if (0d == v) {
                continue;
            }

            // for each element in this row
            for (int j = 0; j < n; j++) {
                double v2 = -v * table[r][j] + table[i][j];
                table[i][j] = Double.isNaN(v2) ? 0 : v2;
            }
        }
    }

    @Override
    public void normalize(int r, int c) {
        final double v = table[r][c];
        if (1d == v || 0d == v) {
            return;
        }

        for (int j = 0; j < n; j++) {
            table[r][j] /= v;
        }
    }

    @Override
    public void negate(int r, int c) {
        table[r][c] *= -1;
    }

    @Override
    public Double divide(int r1, int c1, int r2, int c2) {
        return table[r1][c1] / table[r2][c2];
    }

    @Override
    public boolean isPositive(int r, int c) {
        return table[r][c] > 0d;
    }

    @Override
    public boolean isNegative(int r, int c) {
        return table[r][c] < 0d;
    }

    @Override
    public boolean isZero(int r, int c) {
        return table[r][c] == 0d;
    }

    public boolean existsPositiveInRow(int r, int endIndex) {
        for (int j = 0; j < endIndex; j++) {
            if (table[r][j] > 0d) {
                return true;
            }
        }
        return false;
    }

    public boolean existsNonZeroInRow(int r, int endIndex) {
        for (int j = 0; j < endIndex; j++) {
            if (table[r][j] != 0d) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compare(int r1, int c1, int r2, int c2) {
        return Double.compare(table[r1][c1], table[r2][c2]);
    }

    protected void extendColumn() {
        if (growColumn(n - table[0].length) <= 0) {
            return;
        }

        for (int i = 0; i < m; i++) {
            table[i] = Arrays.copyOf(table[i], n2);
        }
    }
}
