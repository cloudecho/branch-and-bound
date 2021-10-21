package com.github.cloudecho.bnb.math;

public abstract class AbstractMatrix<T extends Number> implements Matrix<T> {
    public final int max_m;
    public final int max_n;

    /**
     * End row
     */
    protected int m;

    /**
     * End column
     */
    protected int n;

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
    }
}
