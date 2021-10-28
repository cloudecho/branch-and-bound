package com.github.cloudecho.bnb.math;

public abstract class AbstractMatrix<T extends Number> implements Matrix<T> {
    public final int max_m;
    public final int max_n;

    /**
     * The number of rows
     */
    protected int m;

    /**
     * The number of columns
     */
    protected int n;

    /**
     * The number of physical columns. <br>
     * n2 = n + nGrow;  n &le; n2 &le; max_n
     */
    protected int n2;

    protected final int extend_n;

    static final int DEFAULT_EXTEND_TIMES = 10;

    public AbstractMatrix(int max_m, int max_n) {
        if (max_m < 1) {
            throw new IllegalArgumentException("zero or negative m: " + max_m);
        }
        if (max_n < 1) {
            throw new IllegalArgumentException("zero or negative n: " + max_n);
        }
        this.max_m = max_m;
        this.max_n = max_n;
        this.m = max_m;
        this.n = max_n;
        this.n2 = n;
        this.extend_n = 0;
    }

    public AbstractMatrix(double[][] table, int max_n) {
        this.m = rows(table);
        this.n = table[0].length;
        this.max_m = m;
        this.max_n = max_n;
        this.n2 = n;
        this.extend_n = (max_n > n) ? (max_n - n) : 0;
    }

    static int rows(double[][] table) {
        if (null == table || table.length < 1) {
            throw new IllegalArgumentException("number of rows < 1");
        }
        return table.length;
    }

    @Override
    public void setRows(int m) {
        if (m < 0 || m > max_m) {
            throw new IllegalArgumentException("row index out of range: " + m);
        }
        this.m = m;
    }

    @Override
    public void setColumns(int n) {
        if (n < 0 || n > max_n) {
            throw new IllegalArgumentException("column index out of range: " + n);
        }
        this.n = n;
        this.extendColumn();
    }

    protected abstract void extendColumn();

    protected int extendTimes() {
        return DEFAULT_EXTEND_TIMES;
    }

    /**
     * Return how many columns grows
     */
    protected int growColumn(int nDelta) {
        if (nDelta <= 0) {
            return nDelta;
        }
        if (extend_n == 0) {
            return 0;
        }

        final int times = extendTimes();
        nDelta = (int) Math.floor(Math.ceil(1d * nDelta / extend_n * times) / times * extend_n);
        this.n2 += nDelta;
        return nDelta;
    }

    @Override
    public int getRows() {
        return m;
    }

    @Override
    public int getColumns() {
        return n;
    }

    @Override
    public void increaseRows() {
        if (++m > max_m) {
            throw new IllegalStateException("exceed max rows: " + max_m);
        }
    }

    @Override
    public void increaseColumns() {
        if (++n > max_n) {
            throw new IllegalStateException("exceed max columns: " + max_n);
        }
        extendColumn();
    }

    @Override
    public void decreaseRows() {
        if (--m < 0) {
            throw new IllegalStateException("negative row index");
        }
    }

    @Override
    public void decreaseColumns() {
        if (--n < 0) {
            throw new IllegalStateException("negative column index");
        }
        extendColumn();
    }
}
