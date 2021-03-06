package com.github.cloudecho.bnb.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

public class BigDecimalMatrix extends AbstractMatrix<BigDecimal> {
    protected final BigDecimal[][] table;
    protected MathContext mathContext = MathContext.DECIMAL128;

    public BigDecimalMatrix(double[][] table, int max_n) {
        super(table, max_n);
        this.table = new BigDecimal[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                this.table[i][j] = BigDecimal.valueOf(table[i][j]);
            }
        }
    }

    public void mathContext(MathContext mathContext) {
        if (mathContext != null) {
            this.mathContext = mathContext;
        }
    }

    @Override
    public BigDecimal get(int r, int c) {
        return table[r][c];
    }

    @Override
    public double getAsDouble(int r, int c) {
        return table[r][c].doubleValue();
    }

    @Override
    public void set(int r, int c, Number num) {
        table[r][c] = BigDecimal.valueOf(num.doubleValue());
    }

    @Override
    public void set(int r, int c, double num) {
        table[r][c] = BigDecimal.valueOf(num);
    }

    @Override
    public void gaussian(int r, int c) {
        BigDecimal v = table[r][c];
        if (isZero(v)) {
            return;
        }

        normalize(r, c);

        // for each row except r-th
        for (int i = 0; i < m; i++) {
            if (r == i) {
                continue;
            }
            v = table[i][c];
            if (isZero(v)) {
                continue;
            }

            // for each element in this row
            for (int j = 0; j < n; j++) {
                if (isZero(table[r][j])) {
                    continue;
                }
                if (j == c) {
                    table[i][j] = BigDecimal.ZERO;
                    continue;
                }

                BigDecimal v2 = v.multiply(table[r][j], mathContext)
                        .negate(mathContext)
                        .add(table[i][j], mathContext);
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

        for (int j = 0; j < n; j++) {
            if (isZero(r, j)) {
                continue;
            }
            if (j == c) {
                table[r][j] = BigDecimal.ONE;
                continue;
            }
            table[r][j] = table[r][j].divide(v, mathContext);
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
    public void removeRow(int r) {
        if (r < 0 || m - 1 - r < 0) {
            return;
        }
        System.arraycopy(table, r + 1, table, r, m - 1 - r);
        table[m - 1] = null;
        decreaseRows();
    }

    @Override
    public void negate(int r, int c) {
        table[r][c] = table[r][c].negate(mathContext);
    }

    @Override
    public BigDecimal divide(int r1, int c1, int r2, int c2) {
        return table[r1][c1].divide(table[r2][c2], mathContext);
    }

    @Override
    public double divideAsDouble(int r1, int c1, int r2, int c2) {
        return divide(r1, c1, r2, c2).doubleValue();
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

    protected void extendColumn() {
        final int fromIndex = n2;
        if (growColumn(n - table[0].length) <= 0) {
            return;
        }
        for (int i = 0; i < m; i++) {
            table[i] = Arrays.copyOf(table[i], n2);
            Arrays.fill(table[i], fromIndex, n2, BigDecimal.ZERO);
        }
    }
}
