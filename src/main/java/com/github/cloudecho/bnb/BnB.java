package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.util.Log;
import com.github.cloudecho.bnb.util.LogFactory;
import com.github.cloudecho.bnb.util.Maths;
import com.github.cloudecho.bnb.util.Sign;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Branch-and-bound for mixed-integer programming.
 * <pre>
 * max (or min) z = c0 + CX
 * s.t. Ai*X <= (or >=, =) b_i , i = 1,...,m
 *      x_j >= (or <>) 0 , j = 1,...,n
 *      x_j integer (for some or all j = 1,..,n)
 *
 * C = (c1 ... c_n)
 * X = (x1 ... x_n)'
 * A = [ a11 ... a1n   // A1
 *       ... ... ...
 *      a_m1 ... a_mn] // Am
 * b = (b1 ... b_m)'
 * </pre>
 *
 * @see <a href="https://web.mit.edu/15.053/www/AMP-Chapter-09.pdf">Integer Programming</a>
 */
public class BnB extends GeneralLP implements Solver {
    static final Log LOG = LogFactory.getLog(BnB.class);

    private final int[] intVars;
    private final int nBinVars;

    /**
     * Constructor. All variables are default to be non-negative.
     *
     * @param objectiveType The objective type (max or min)
     * @param c0            c0
     * @param c             The coefficient vector C, 1 row, n columns
     * @param a             The matrix A, m rows, n columns
     * @param signs         List of sign (&lt;=, &gt;=, =). <br>
     *                      e.g. [EQ, GE, LE] represents A1*X = b1, A2*X >= b2, A3*X <= b3.
     * @param b             The vector b, m rows, 1 column
     * @param freeVars      unrestricted variables, var starts from 1. <br>
     *                      e.g. {1,2} represents x1,x2 are unrestricted.
     * @param intVars       integer variables, var starts from 1. <br>
     *                      e.g. {3,4} represents x3,x4 are restricted to be non-negative integer.
     * @param binVars       binary variables, var starts from 1. <br>
     *                      e.g. {1,2} represents x1,x2 are restricted to be 0 or 1.
     */
    public BnB(ObjectiveType objectiveType, double c0, double[] c, double[][] a, Sign[] signs, double[] b, int[] freeVars, int[] intVars, int[] binVars) {
        super(objectiveType, c0, c, a, signs, b, freeVars);
        this.intVars = Maths.unique(Maths.union(binVars, intVars));
        this.nBinVars = Maths.length(Maths.unique(binVars));

        LOG.debug("intVars", this.intVars);
        LOG.debug("nBinVars", this.nBinVars);
    }

    /**
     * Constructor. All variables are default to be non-negative.
     *
     * @see #BnB(ObjectiveType, double, double[], double[][], Sign[], double[], int[], int[], int[])
     */
    public BnB(ObjectiveType objectiveType, double c0, double[] c, double[][] a, Sign[] signs, double[] b, int[] freeVars, int[] intVars) {
        this(objectiveType, c0, c, a, signs, b, freeVars, intVars, Maths.EMPTY_INT_ARRAY);
    }

    /**
     * a with binary constraints
     */
    private double[][] a2() {
        double[][] r = a;
        for (int j = 0; j < nBinVars; j++) {
            final int v = intVars[j];
            double[] constraint = new double[n];
            constraint[v - 1] = 1d;
            r = Maths.append(r, constraint);
        }
        return r;
    }

    /**
     * b with binary constraints
     */
    private double[] b2() {
        double[] r = b;
        for (int j = 0; j < nBinVars; j++) {
            r = Maths.append(r, 1d);
        }
        return r;
    }

    /**
     * signs with binary constraints
     */
    private Sign[] signs2() {
        Sign[] r = signs;
        for (int j = 0; j < nBinVars; j++) {
            r = Maths.append(r, Sign.LE);
        }
        return r;
    }

    static class Node {
        static final char ROOT = '0';
        static final char LEFT = 'L';
        static final char RIGHT = 'R';

        final GeneralLP lp;
        final int level;// starts from 0
        final char branch;
        final Node parent;

        public Node(GeneralLP lp, Node parent, char branch) {
            this.lp = lp;
            this.parent = parent;
            this.branch = branch;
            this.level = (parent == null) ? 0 : parent.level + 1;
        }

        @Override
        public String toString() {
            return (parent == null ? "" : parent + "-") + level + branch;
        }
    }

    /**
     * The problem tree
     */
    private final LinkedList<Node> nodes = new LinkedList<>();

    @Override
    public void solve() {
        this.objective = objectiveType.isMax() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        this.iterations = 0;
        LOG.debug(this);
        this.state = State.SOLVING;

        // create root node
        GeneralLP lp0 = new GeneralLP(objectiveType, c0, c, a2(), signs2(), b2(), freeVars);
        nodes.add(new Node(lp0, null, Node.ROOT));

        while (!nodes.isEmpty()) {
            this.iterations++;
            solve(nodes.removeLast());
        }

        if (State.SOLVING == this.state) {
            this.state = State.NO_SOLUTION;
        }

        LOG.debug(this);
    }

    private void solve(Node node) {
        node.lp.setPrecision(this.precision);
        node.lp.solve();
        LOG.debug(node, node.lp.state);

        // if the LP relaxation unbounded
        if (State.UNBOUNDED == node.lp.state) {
            this.state = State.UNBOUNDED;
            LOG.debug(node, "prune", node.lp.state);
            return;
        }

        // if the LP relaxation not solved
        if (!node.lp.state.isSolved()) {
            LOG.debug(node, "prune", node.lp.state);
            return;
        }

        // case 1
        if (this.betterOrEq(node.lp.objective)) {
            LOG.debug(node, "prune", "worse", node.lp.objective);
            return;
        }

        // case 2
        if (isFeasible(node.lp.x)) {
            this.state = State.SOLVED;
            this.objective = node.lp.objective; // incumbent
            System.arraycopy(node.lp.x, 0, this.x, 0, n);
            LOG.debug(node, "prune", "incumbent", this.objective);
            return;
        }

        // case 3: node.lp.x not feasible
        if (!isLeaf(node)) {
            if (node.level < nBinVars) {
                branch01(node);
            } else {
                branch(node);
            }
        }
    }

    private boolean betterOrEq(double z) {
        if (objectiveType.isMax()) {
            return objective >= z;
        } else {
            return objective <= z;
        }
    }

    private void branch(Node parent) {
        final GeneralLP lp0 = parent.lp;
        final int v = intVars[parent.level];
        Maths.CnF cf = new Maths.CnF(lp0.x[v - 1]);

        // add a constraint to parent.A
        final double[] constraint = new double[n];
        constraint[v - 1] = 1;
        final double[][] a2 = Maths.append(lp0.a, constraint);

        // LP1: left branch (<= floor)
        LOG.debug(parent, "left branch x(", v, ") <=", cf.floor);
        Sign[] signs1 = Maths.append(lp0.signs, Sign.LE);
        double[] b1 = Maths.append(lp0.b, cf.floor);
        GeneralLP lp1 = new GeneralLP(lp0.objectiveType, lp0.c0, lp0.c, a2, signs1, b1, lp0.freeVars);

        // LP2: right branch (>= ceil)
        LOG.debug(parent, "right branch x(", v, ") >=", cf.ceil);
        Sign[] signs2 = Maths.append(lp0.signs, Sign.GE);
        double[] b2 = Maths.append(lp0.b, cf.ceil);
        GeneralLP lp2 = new GeneralLP(lp0.objectiveType, lp0.c0, lp0.c, a2, signs2, b2, lp0.freeVars);

        nodes.addLast(new Node(lp1, parent, Node.LEFT));
        nodes.addLast(new Node(lp2, parent, Node.RIGHT));
    }

    /**
     * 0-1 branch for binary vars
     */
    private void branch01(Node parent) {
        final GeneralLP lp0 = parent.lp;
        final int v = intVars[parent.level];

        final Sign[] signs2 = Arrays.copyOf(lp0.signs, lp0.signs.length);
        signs2[m + parent.level] = Sign.EQ;

        // LP1: left branch (=0)
        LOG.debug(parent, "left branch x(", v, ") =", 0);
        final double[] b1 = Arrays.copyOf(lp0.b, lp0.b.length);
        b1[m + parent.level] = 0d;
        GeneralLP lp1 = new GeneralLP(lp0.objectiveType, lp0.c0, lp0.c, lp0.a, signs2, b1, lp0.freeVars);

        // LP2: right branch (=1)
        LOG.debug(parent, "right branch x(", v, ") =", 1);
        final double[] b2 = Arrays.copyOf(lp0.b, lp0.b.length);
        b2[m + parent.level] = 1d;
        GeneralLP lp2 = new GeneralLP(lp0.objectiveType, lp0.c0, lp0.c, lp0.a, signs2, b2, lp0.freeVars);

        nodes.addLast(new Node(lp1, parent, Node.LEFT));
        nodes.addLast(new Node(lp2, parent, Node.RIGHT));
    }

    private boolean isLeaf(Node node) {
        return node.level == intVars.length;
    }

    private boolean isFeasible(double[] x) {
        for (int v : intVars) {
            if (!new Maths.CnF(x[v - 1]).eq()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void toStringExtra(StringBuilder b) {
        b.append("\n intVars=").append(Arrays.toString(intVars));
        b.append(" nBinVars=").append(nBinVars);
    }
}
