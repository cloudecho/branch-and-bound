package com.github.cloudecho.bnb.math;

public interface Matrix<T extends Number> {
    T get(int r, int c);

    void set(int r, int c, Number num);

    void gaussian(int r, int c);

    void normalize(int r, int c);

    Object getRow(int r);

    void setRow(int r, Object rowData);

    default void setRowTo(int r, int r2) {
        setRow(r, getRow(r2));
    }

    void setRows(int m);

    void setColumns(int n);

    int getRows();

    int getColumns();

    void increaseRows();

    void increaseColumns();

    void decreaseRows();

    void decreaseColumns();

    void negate(int r, int c);

    T divide(int r1, int c1, int r2, int c2);

    boolean existsPositiveInRow(int r, int endIndex);

    boolean existsNonZeroInRow(int r, int endIndex);

    boolean isPositive(int r, int c);

    boolean isNegative(int r, int c);

    boolean isZero(int r, int c);

    default boolean nonPositive(int r, int c) {
        return isNegative(r, c) || isZero(r, c);
    }

    default boolean nonNegative(int r, int c) {
        return isPositive(r, c) || isZero(r, c);
    }

    default boolean nonZero(int r, int c) {
        return !isZero(r, c);
    }

    int compare(int r1, int c1, int r2, int c2);
}
