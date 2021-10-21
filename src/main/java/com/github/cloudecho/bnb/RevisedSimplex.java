package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.math.Matrix;
import com.github.cloudecho.bnb.math.RevisedDoubleMatrix;

/**
 * Revised version of Simplex
 */
public class RevisedSimplex extends Simplex {
    /**
     * Constructor.
     *
     * @param c The coefficient vector C, 1 row, n columns
     * @param a The matrix A, m rows, n columns
     * @param b The vector b, m rows, 1 column
     */
    protected RevisedSimplex(double[] c, double[][] a, double[] b) {
        super(c, a, b);
    }

    @Override
    protected Matrix<?> createMatrix(double[][] table, int max_n) {
        return new RevisedDoubleMatrix(table, max_n);
    }
}
