package com.github.cloudecho.bnb.math;

public class DoubleMatrix extends AbstractMatrix<Double> {
    protected final double[][] table;

    public DoubleMatrix(int m, int n) {
        super(m, n);
        this.table = new double[m][n];
    }

    public DoubleMatrix(double[][] table) {
        super(rows(table), table[0].length);
        this.table = table;
    }

    static int rows(double[][] table) {
        if (null == table || table.length < 1) {
            throw new IllegalArgumentException("the rows of table < 1");
        }
        return table.length;
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

    @Override
    public void normalize(int r, int c) {
        final double v = table[r][c];
        if (1d == v || 0d == v) {
            return;
        }

        for (int j = 0; j <= n2; j++) {
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
}
