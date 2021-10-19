package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.math.BigDecimalMatrix;
import com.github.cloudecho.bnb.math.Matrix;

/**
 * BigDecimal version of Simplex
 */
public class BigSimplex extends Simplex {
    /**
     * Constructor.
     *
     * @param c The coefficient vector C, 1 row, n columns
     * @param a The matrix A, m rows, n columns
     * @param b The vector b, m rows, 1 column
     */
    protected BigSimplex(double[] c, double[][] a, double[] b) {
        super(c, a, b);
    }

    @Override
    protected Matrix<?> createMatrix(double[][] table) {
        return new BigDecimalMatrix(table);
    }
}
