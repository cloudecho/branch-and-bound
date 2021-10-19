package com.github.cloudecho.bnb.math;

public abstract class AbstractMatrix implements Matrix {
    public final int m;
    public final int n;

    /**
     * End row
     */
    protected int m2;

    /**
     * End column
     */
    protected int n2;

    public AbstractMatrix(int m, int n) {
        if (m < 0) {
            throw new IllegalArgumentException("negative m: " + m);
        }
        if (n < 0) {
            throw new IllegalArgumentException("negative n: " + n);
        }
        this.m = m;
        this.n = n;
        this.m2 = m;
        this.n2 = n;
    }

    @Override
    public void endRow(int m2) {
        if (m2 < 0 || m2 > m) {
            throw new IllegalArgumentException("row index out of range: " + m2);
        }
        this.m2 = m2;
    }

    @Override
    public void endColumn(int n2) {
        if (n2 < 0 || n2 > n) {
            throw new IllegalArgumentException("column index out of range: " + n2);
        }
        this.n2 = n2;
    }

    @Override
    public int endRow() {
        return m2;
    }

    @Override
    public int endColumn() {
        return n2;
    }

    @Override
    public void incEndRow() {
        if (++m2 > m) {
            throw new IllegalStateException("exceed max rows: " + m);
        }
    }

    @Override
    public void incEndColumn() {
        if (++n2 > n) {
            throw new IllegalStateException("exceed max columns: " + n);
        }
    }

    @Override
    public void decEndRow() {
        if (--m2 < 0) {
            throw new IllegalStateException("negative row index");
        }
    }

    @Override
    public void decEndColumn() {
        if (--n2 < 0) {
            throw new IllegalStateException("negative column index");
        }
    }
}
