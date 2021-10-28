package com.github.cloudecho.bnb.math;

import java.util.Arrays;

public class RevisedDoubleMatrix extends AbstractMatrix<Double> {
    /**
     * For <tt>identifyColumn[j] &ge; 0</tt>, suppose table[j] = [0 ... 1 ... 0]<sup>T</sup> ,
     * <tt>identifyColumn[j]</tt> is the index of value 1
     */
    protected final int[] identifyColumn;

    /**
     * <tt>table[j][i]</tt> represents the element (i,j) of matrix
     */
    protected double[][] table;

    public RevisedDoubleMatrix(double[][] table, int max_n) {
        super(table, max_n);
        this.table = transpose(table);
        identifyColumn = new int[max_n];
        Arrays.fill(identifyColumn, -1);
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
    public double getAsDouble(int r, int c) {
        final int i = identifyColumn[c];
        if (i >= 0) {
            return i == r ? 1d : 0d;
        }
        return table[c][r];
    }

    public Double get(int r, int c) {
        return getAsDouble(r, c);
    }

    @Override
    public double[] getRow(int r) {
        double[] row = new double[m];
        for (int j = 0; j < n; j++) {
            row[j] = this.getAsDouble(r, j);
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
    public void removeRow(int r) {
        if (r < 0 || m - 1 - r < 0) {
            return;
        }
        for (int j = 0; j < n; j++) {
            final int i = identifyColumn[j];
            if (i >= 0) {
                if (i == r) {
                    throw new UnsupportedOperationException("remove element of identify column: " + j + " (r=" + r + ')');
                } else if (i > r) {
                    identifyColumn[j] = i - 1;
                } // else i < r, NO-OP
                continue;
            }
            System.arraycopy(table[j], r + 1, table[j], r, m - 1 - r);
            // table[j][m - 1] ignored
        }
        decreaseRows();
    }

    @Override
    public void set(int r, int c, final double num) {
        final int i = identifyColumn[c];
        if (i >= 0) {
            if (1d == num && i == r || 0d == num && i != r) {
                return;
            }
            throw new UnsupportedOperationException("setting value on identify column: " + c + " (r=" + r + ')');
        }
        table[c][r] = num;
    }

    @Override
    public void set(int r, int c, Number num) {
        set(r, c, num.doubleValue());
    }

    @Override
    public void gaussian(int r, int c) {
        double v = getAsDouble(r, c);
        if (0d == v) {
            return;
        }

        normalize(r, c);

        // for each row except r-th
        for (int i = 0; i < m; i++) {
            if (r == i) {
                continue;
            }
            v = getAsDouble(i, c);
            if (0d == v) {
                continue;
            }

            // for each element in this row
            for (int j = 0; j < n; j++) {
                // skip while j==c as matrix(i,c) will be 0
                if (isZero(r, j) || j == c) {
                    continue;
                }
                double v2 = -v * getAsDouble(r, j) + getAsDouble(i, j);
                set(i, j, Double.isNaN(v2) ? 0 : v2);
            }
        }

        identifyColumn(r, c);
    }

    @Override
    public void normalize(int r, int c) {
        final double v = getAsDouble(r, c);
        if (1d == v || 0d == v) {
            for (int j = 0; j < n; j++) {
                if (isZero(r, j) || identifyColumn[j] < 0) {
                    continue;
                }
                unidentifyColumn(r, j);
                break;
            }
            return;
        }

        for (int j = 0; j < n; j++) {
            if (isZero(r, j)) {
                continue;
            }
            if (j == c) {
                set(r, j, 1d);
                continue;
            }
            if (identifyColumn[j] >= 0) {
                unidentifyColumn(r, j);
            }
            set(r, j, getAsDouble(r, j) / v);
        }
    }

    // To avoid frequently gc
    private double[] reusedColumn;

    private void unidentifyColumn(int r, int j) {
        Arrays.fill(reusedColumn, 0);
        reusedColumn[r] = 1d;
        table[j] = reusedColumn;
        identifyColumn[j] = -1;
    }

    private void identifyColumn(int r, int c) {
        identifyColumn[c] = r;
        reusedColumn = table[c];
        table[c] = null;
    }

    @Override
    public void negate(int r, int c) {
        final int i = identifyColumn[c];
        if (i >= 0) {
            if (i == r) { // 1
                throw new UnsupportedOperationException("negating on identify column: " + c + " (r=" + r + ')');
            } // else 0
            return;
        }
        table[c][r] *= -1d;
    }

    @Override
    public Double divide(int r1, int c1, int r2, int c2) {
        return getAsDouble(r1, c1) / getAsDouble(r2, c2);
    }

    @Override
    public double divideAsDouble(int r1, int c1, int r2, int c2) {
        return getAsDouble(r1, c1) / getAsDouble(r2, c2);
    }

    @Override
    public boolean isPositive(int r, int c) {
        final int i = identifyColumn[c];
        if (i >= 0) {
            return i == r;
        }
        return table[c][r] > 0d;
    }

    @Override
    public boolean isNegative(int r, int c) {
        final int i = identifyColumn[c];
        if (i >= 0) {
            return false;
        }
        return table[c][r] < 0d;
    }

    @Override
    public boolean isZero(int r, int c) {
        final int i = identifyColumn[c];
        if (i >= 0) {
            return i != r;
        }
        return table[c][r] == 0d;
    }

    public boolean existsPositiveInRow(int r, int endIndex) {
        for (int j = 0; j < endIndex; j++) {
            if (getAsDouble(r, j) > 0d) {
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
        return Double.compare(getAsDouble(r1, c1), getAsDouble(r2, c2));
    }

    @Override
    protected void extendColumn() {
        int nGrow = growColumn(n - table.length);
        if (nGrow > 0) {
            table = Arrays.copyOf(table, n2);
            // init table[j]
            for (int j = n2 - nGrow; j < n2; j++) {
                table[j] = new double[m];
            }
        }
    }
}
