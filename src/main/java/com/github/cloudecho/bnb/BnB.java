package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.util.*;

import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Branch-and-bound for mixed-integer programming.
 * <pre>
 * max (or min) z = c<sub>0</sub> + CX
 * s.t. A<sub>i</sub>*X &le; (or &ge;, =) b<sub>i</sub> , i = 1,...,m
 *      x<sub>j</sub> &ge; (or &ne;) 0 , j = 1,...,n
 *      x<sub>j</sub> integer (for some or all j = 1,..,n)
 *
 * C = [c<sub>1</sub> ... c<sub>n</sub>]
 * X = [x<sub>1</sub> ... x<sub>n</sub>]<sup>T</sup>
 * A = [a<sub>11</sub> ... a<sub>1n</sub>    // A<sub>1</sub>
 *      ... ... ...
 *      a<sub>m1</sub> ... a<sub>mn</sub>]   // A<sub>m</sub>
 * b = [b<sub>1</sub> ... b<sub>m</sub>]<sup>T</sup>
 * </pre>
 *
 * @see <a href="https://web.mit.edu/15.053/www/AMP-Chapter-09.pdf">Integer Programming</a>
 */
public class BnB extends GeneralLP implements Solver {
    static final Log LOG = LogFactory.getLog(BnB.class);

    public static final String LOG_LEVEL_PROP = "com.github.cloudecho.bnb.BnB.LOG_LEVEL";

    static {
        LogFactory.getConfiguredLogLevel(LOG_LEVEL_PROP)
                .ifPresent(LOG::setLevel);
    }

    private final int[] intVars;
    private final int nBinVars;
    private int nBinVars2;

    /**
     * Constructor. All variables are default to be non-negative.
     *
     * @param objectiveType The objective type (max or min)
     * @param c0            c0
     * @param c             The coefficient vector C, 1 row, n columns
     * @param a             The matrix A, m rows, n columns
     * @param signs         List of sign (&le;, &ge;, =). <br>
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
        this.nBinVars2 = nBinVars;

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
            if (existsBinConstraint(v)) {
                LOG.debug("existsBinConstraint", v);
                this.nBinVars2--;
                continue;
            }
            double[] constraint = new double[n];
            constraint[v - 1] = 1d;
            r = Maths.append(r, constraint);
        }
        return r;
    }

    /**
     * Return {@code true} if binary constraint for x(v) already exists.
     */
    private boolean existsBinConstraint(int v) {
        final int c = v - 1; // column
        // for each row
        for (int i = 0; i < m; i++) {
            if (b[i] != 1d || a[i][c] != 1d) {
                continue;
            }
            if (Sign.GE == signs[i]) {
                continue;
            }
            if (existsNegativeNumOrFreeVar(a[i], n)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean existsNegativeNumOrFreeVar(double[] row, int endIndex) {
        for (int j = 0; j < endIndex; j++) {
            if (row[j] < 0) {
                return true;
            }
            if (row[j] != 0 && Maths.contains(freeVars, j)) {
                return true;
            }
        }
        return false;
    }

    /**
     * b with binary constraints
     */
    private double[] b2() {
        double[] r = b;
        for (int j = 0; j < nBinVars2; j++) {
            r = Maths.append(r, 1d);
        }
        return r;
    }

    /**
     * signs with binary constraints
     */
    private Sign[] signs2() {
        Sign[] r = signs;
        for (int j = 0; j < nBinVars2; j++) {
            r = Maths.append(r, Sign.LE);
        }
        return r;
    }

    static class Node {
        static final char ROOT = '0';
        static final char LEFT = 'L';
        static final char RIGHT = 'R';

        GeneralLP lp;
        final int level;// starts from 0
        final char branch;
        final Node parent;

        int binVar = -1;

        public Node(GeneralLP lp, Node parent, char branch) {
            this.lp = lp;
            this.parent = parent;
            this.branch = branch;
            this.level = (parent == null) ? 0 : parent.level + 1;
        }

        void solve() {
            lp.solve();

            // for binary var
            for (Node n = this; n != null; n = n.parent) {
                if (-1 == n.binVar) {
                    continue;
                }
                this.lp.x[n.binVar - 1] = (LEFT == n.branch) ? 0d : 1d;
            }
        }

        Node binary(int binVar) {
            this.binVar = binVar;
            return this;
        }

        @Override
        public String toString() {
            return (parent == null ? "" : parent + "-") + level + branch;
        }

    }

    /**
     * The problem tree
     */
    private final Deque<Node> nodes = new LinkedBlockingDeque<>();

    private final AtomicInteger taskCounter = new AtomicInteger(0);

    public static final String SOLVING_THREADS_PROP = "com.github.cloudecho.bnb.SOLVING_THREADS";

    static final int nThreads = Integer.parseInt(System.getProperty(SOLVING_THREADS_PROP, "2"));

    private final ExecutorService executor = Executors.newFixedThreadPool(
            nThreads, new NamedThreadFactory().namePrefix("bnb-solver"));

    @Override
    public void solve() {
        this.objective = objectiveType.isMax() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        this.iterations = 0;
        LOG.trace(this);
        this.state = State.SOLVING;

        // create root node
        final double[][] a2 = a2();
        GeneralLP lp0 = new GeneralLP(objectiveType, c0, c, a2, signs2(), b2(), freeVars);
        nodes.add(new Node(lp0, null, Node.ROOT));
        this.submitTasks(1);

        // wait to complete
        synchronized (this) {
            while (taskCounter.get() > 0) {
                try {
                    LOG.debug("wait to complete");
                    this.wait();
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }
        }

        if (State.SOLVING == this.state) {
            this.state = State.NO_SOLUTION;
        }

        LOG.trace(this);
    }

    private void submitTasks(int nTasks) {
        for (int i = 0; i < nTasks; i++) {
            taskCounter.incrementAndGet();
            executor.submit(this::doWork);
        }
    }

    private void doWork() {
        final Node node = nodes.pollLast();
        try {
            if (null != node) {
                solve(node);
            }
        } catch (Throwable ex) {
            LOG.error(ex);
        } finally {
            taskCounter.decrementAndGet();
            synchronized (this) {
                this.notify();
            }
        }
    }

    private void solve(Node node) {
        synchronized (this) {
            this.iterations++;
        }
        node.lp.setPrecision(this.precision);
        node.solve();

        LOG.debug(node, node.lp.state);

        // if the LP relaxation unbounded
        if (State.UNBOUNDED == node.lp.state) {
            synchronized (this) {
                this.state = State.UNBOUNDED;
            }
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
            synchronized (this) {
                this.state = State.SOLVED;
                this.objective = node.lp.objective; // incumbent
                this.x = node.lp.x;
                this.reducedCost = node.lp.reducedCost;
                this.slack = node.lp.slack;
                this.shadowPrice = node.lp.shadowPrice;
            }
            LOG.debug(node, "prune", "incumbent", this.objective);
            return;
        }

        // case 3: node.lp.x not feasible
        createBranch(node);
    }

    private void createBranch(Node node) {
        if (isLeaf(node)) {
            return;
        }
        if (node.level < nBinVars) {
            branch01(node);
        } else {
            branch(node);
        }
        this.submitTasks(2);
    }

    private synchronized boolean betterOrEq(double z) {
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
        Node child1 = new Node(lp1, parent, Node.LEFT);
        nodes.addLast(child1);

        // LP2: right branch (>= ceil)
        LOG.debug(parent, "right branch x(", v, ") >=", cf.ceil);
        Sign[] signs2 = Maths.append(lp0.signs, Sign.GE);
        double[] b2 = Maths.append(lp0.b, cf.ceil);
        GeneralLP lp2 = new GeneralLP(lp0.objectiveType, lp0.c0, lp0.c, a2, signs2, b2, lp0.freeVars);
        Node child2 = new Node(lp2, parent, Node.RIGHT);
        nodes.addLast(child2);

        parent.lp = null; // release memory
    }

    /**
     * 0-1 branch for binary vars
     */
    private void branch01(Node parent) {
        final GeneralLP lp0 = parent.lp;
        final int v = intVars[parent.level];
        Maths.CnF cf = new Maths.CnF(lp0.x[v - 1]);

        double[][] a2 = Arrays.copyOf(lp0.a, lp0.m);
        double[] bLeft = lp0.b; // left branch
        double[] bRight = Arrays.copyOf(lp0.b, lp0.m); // right branch
        double[] c2 = Arrays.copyOf(lp0.c, lp0.n);
        double c0Right = lp0.c0 + lp0.c[v - 1]; // c0Left unchanged
        branch01Arguments(c2, a2, bRight, lp0, v);

        // LP1: left branch (=0)
        LOG.debug(parent, "left branch x(", v, ") =", 0);
        GeneralLP lp1 = new GeneralLP(lp0.objectiveType, lp0.c0, c2, a2, lp0.signs, bLeft, lp0.freeVars);
        Node child1 = new Node(lp1, parent, Node.LEFT).binary(v);
        nodes.addLast(child1);

        // LP2: right branch (=1)
        LOG.debug(parent, "right branch x(", v, ") =", 1);
        GeneralLP lp2 = new GeneralLP(lp0.objectiveType, c0Right, c2, a2, lp0.signs, bRight, lp0.freeVars);
        Node child2 = new Node(lp2, parent, Node.RIGHT).binary(v);
        nodes.addLast(child2);

        parent.lp = null; // release memory
    }

    private void branch01Arguments(final double[] c2, final double[][] a2, final double[] bRight, GeneralLP lp0, final int binVar) {
        final int j = binVar - 1;
        c2[j] = 0;
        for (int i = 0; i < lp0.m; i++) {
            if (0d == lp0.a[i][j]) {
                continue;
            }
            a2[i] = Arrays.copyOf(lp0.a[i], lp0.n);
            a2[i][j] = 0;
            bRight[i] -= lp0.a[i][j];
        }
    }

    private boolean isLeaf(Node node) {
        return node.level >= intVars.length;
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
