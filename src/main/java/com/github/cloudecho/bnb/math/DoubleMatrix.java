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
    public double getAsDouble(int r, int c) {
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
    public void removeRow(int r) {
        if (r < 0 || m - 1 - r < 0) {
            return;
        }
        System.arraycopy(table, r + 1, table, r, m - 1 - r);
        table[m - 1] = null;
        decreaseRows();
    }

    @Override
    public void set(int r, int c, Number num) {
        table[r][c] = num.doubleValue();
    }

    @Override
    public void set(int r, int c, double num) {
        table[r][c] = num;
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
                if (0d == table[r][j]) {
                    continue;
                }
                if (j == c) {
                    table[i][j] = 0d;
                    continue;
                }
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
            if (0d == table[r][j]) {
                continue;
            }
            if (j == c) {
                table[r][j] = 1d;
                continue;
            }
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
    public double divideAsDouble(int r1, int c1, int r2, int c2) {
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
