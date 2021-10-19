package com.github.cloudecho.bnb.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class BigDecimalMatrix extends AbstractMatrix<BigDecimal> {
    protected final BigDecimal[][] table;

    public BigDecimalMatrix(int m, int n) {
        super(m, n);
        table = new BigDecimal[m][];

        table[0] = new BigDecimal[n];
        for (int j = 0; j < n; j++) {
            table[0][j] = BigDecimal.ZERO;
        }
        for (int i = 1; i < m; i++) {
            table[i] = Arrays.copyOf(table[0], n);
        }
    }

    public BigDecimalMatrix(double[][] table) {
        super(DoubleMatrix.rows(table), table[0].length);
        this.table = new BigDecimal[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                this.table[i][j] = BigDecimal.valueOf(table[i][j]);
            }
        }
    }

    @Override
    public BigDecimal get(int r, int c) {
        return table[r][c];
    }

    @Override
    public void set(int r, int c, Number num) {
        table[r][c] = BigDecimal.valueOf(num.doubleValue());
    }

    @Override
    public void gaussian(int r, int c) {
        BigDecimal v = table[r][c];
        if (isZero(v)) {
            return;
        }

        normalize(r, c);

        // for each row except r-th
        for (int i = 0; i <= m2; i++) {
            if (r == i) {
                continue;
            }
            v = table[i][c];
            if (isZero(v)) {
                continue;
            }

            // for each element in this row
            for (int j = 0; j <= n2; j++) {
                BigDecimal v2 = v.multiply(table[r][j]).negate().add(table[i][j]);
                table[i][j] = v2;
            }
        }
    }

    @Override
    public void normalize(int r, int c) {
        final BigDecimal v = table[r][c];
        if (isOne(v) || isZero(v)) {
            return;
        }

        for (int j = 0; j <= n2; j++) {
            table[r][j] = table[r][j].divide(v, RoundingMode.HALF_UP);
        }
    }

    static boolean isZero(BigDecimal num) {
        return BigDecimal.ZERO.equals(num);
    }

    static boolean isOne(BigDecimal num) {
        return BigDecimal.ONE.equals(num);
    }

    @Override
    public BigDecimal[] getRow(int r) {
        return table[r];
    }

    @Override
    public void setRow(int r, Object rowData) {
        table[r] = (BigDecimal[]) rowData;
    }

    @Override
    public void negate(int r, int c) {
        table[r][c] = table[r][c].negate();
    }

    @Override
    public BigDecimal divide(int r1, int c1, int r2, int c2) {
        return table[r1][c1].divide(table[r2][c2], RoundingMode.HALF_UP);
    }

    @Override
    public boolean existsPositiveInRow(int r, int endIndex) {
        for (int j = 0; j < endIndex; j++) {
            if (table[r][j].signum() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existsNonZeroInRow(int r, int endIndex) {
        for (int j = 0; j < endIndex; j++) {
            if (table[r][j].signum() != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPositive(int r, int c) {
        return table[r][c].signum() > 0;
    }

    @Override
    public boolean isNegative(int r, int c) {
        return table[r][c].signum() < 0;
    }

    @Override
    public boolean isZero(int r, int c) {
        return table[r][c].signum() == 0;
    }

    @Override
    public int compare(int r1, int c1, int r2, int c2) {
        return table[r1][c1].compareTo(table[r2][c2]);
    }
}
