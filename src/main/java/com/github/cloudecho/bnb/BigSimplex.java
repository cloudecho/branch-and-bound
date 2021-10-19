package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.math.Matrix;
import com.github.cloudecho.bnb.util.Log;
import com.github.cloudecho.bnb.util.LogFactory;

/**
 * BigDecimal version of Simplex
 */
public class BigSimplex extends Simplex {
    static final Log LOG = LogFactory.getLog(BigSimplex.class);

    /**
     * Constructor.
     *
     * @param c The coefficient vector C, 1 row, n columns
     * @param a The matrix A, m rows, n columns
     * @param b The vector b, m rows, 1 column
     */
    public BigSimplex(double[] c, double[][] a, double[] b) {
        super(c, a, b);
    }

    @Override
    protected Matrix createMatrix(double[][] table) {
        // TODO
        return super.createMatrix(table);
    }
}
