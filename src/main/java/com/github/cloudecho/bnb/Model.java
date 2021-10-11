package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {
    static final Log LOG = LogFactory.getLog(Model.class);

    public ObjectiveType objectiveType;
    public double c0 = 0;
    public double[] c;
    public double[][] a;
    public Sign[] signs;
    public double[] b;
    public int[] freeVars;
    public int[] intVars;
    public int[] binVars;
    public List<String> variables = new ArrayList<>();
    public List<String> comments = new ArrayList<>();

    public static Model valueOf(String text) {
        return new Parser(text).parse();
    }

    public Solver newSolver() {
        return new BnB(objectiveType, c0, c, a, signs, b, freeVars, intVars, binVars);
    }

    boolean addVariable(String var) {
        if (Strings.isEmpty(var) || variables.contains(var)) {
            return false;
        }
        return variables.add(var); // m.variables.size++
    }

    int sizeOfVariables() {
        return variables.size();
    }

    void addComments(List<String> comments) {
        if (null != comments && !comments.isEmpty()) {
            this.comments.addAll(comments);
        }
    }

    static String sample() {
        StringBuilder b = new StringBuilder();
        b.append("# objective=max");
        b.append("\nmax = -58.8 + 6.7/3*x1 + x2 - 3*x3/2 + x4\n");
        b.append("\n2.1*x1 + 3.2*x2 <= 35");
        b.append("\n5.4*x1 - 4.3*x2 <= 26");
        b.append("\nx2 + x3 >= 50");
        b.append("\nx1 + x2 - x3 + x4 = 100\n");
        b.append("\n# x1={0,1}; x2,x3 integer; x4 unrestricted");
        b.append("\n@bin(x1); @int(x2,x3); @free(x4)");
        return b.toString();
    }

    @Override
    public String toString() {
        return newSolver().toString();
    }

    private static class Node {
        Node parent = null;
        List<Node> children = new ArrayList<>();

        List<String> comments = new ArrayList<>();
        List<String> exprs = new ArrayList<>();
        String target;
        Sign sign;

        void addExpr(String expr) {
            exprs.add(expr);
        }

        void addComment(String comment) {
            comments.add(comment);
        }

        void addChild(Node child) {
            children.add(child);
            child.parent = this;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "exprs=" + exprs +
                    ", sign=" + sign +
                    ", target='" + target + '\'' +
                    ", comments=" + comments +
                    '}';
        }
    }

    static final String FUNC_PREFIX = "@";
    static final String FUNC_BIN = "@bin";
    static final String FUNC_INT = "@int";
    static final String FUNC_FREE = "@free";

    /**
     * e.g. -3*x3/2.0
     */
    private static class Expr {
        final String expr;

        double coef = 0d; // coefficient
        String var = ""; // e.g. x1

        // for parsing
        private boolean letter = false;
        private char sign = '*';

        Expr(String expr) {
            this.expr = expr;
        }

        static Expr valueOf(String expr) {
            return new Expr(expr).parse();
        }

        boolean isConstant() {
            return 0 == var.length();
        }

        boolean isFunc() {
            return var.startsWith(FUNC_PREFIX);
        }

        Expr parse() {
            if (Strings.isEmpty(expr)) {
                return this;
            }

            if (expr.startsWith(FUNC_PREFIX)) {
                this.var = expr;
                return this;
            }

            this.coef = 1d;
            int start = 0;
            for (int k = 0; k < expr.length(); k++) {
                char ch = expr.charAt(k);
                if ('*' == ch || '/' == ch || '-' == ch) {
                    emit(start, k, ch);
                    start = k + 1;
                } else if (Character.isLetter(ch)) {
                    letter = true;
                } else if (Character.isDigit(ch) || '.' == ch) {
                    // NO-OP
                } else {
                    throw new RuntimeException("unknown character '" + ch + "'");
                }
            }
            emit(start, expr.length(), ';');

            return this;
        }

        private void emit(int start, int end, char ch) {
            if ('-' == ch) {
                coef *= -1;
                return;
            }

            if (start == end) {
                return;
            }

            String e = expr.substring(start, end);
            if (letter) {
                this.var = e;
                letter = false;
                sign = ch;
                return;
            }

            if ('*' == sign) {
                coef *= Double.parseDouble(e);
            } else if ('/' == sign) {
                coef /= Double.parseDouble(e);
            } else {
                throw new RuntimeException("unknown sign '" + ch + "'");
            }

            sign = ch;
        }

        @Override
        public String toString() {
            return "Expr{" +
                    "coef=" + coef +
                    ", var='" + var + '\'' +
                    '}';
        }
    }

    private static class Parser {
        final String text;
        Node root;
        Node context;

        Parser(String text) {
            if (Strings.isEmpty(text)) {
                throw new IllegalArgumentException("text could not be empty");
            }
            this.text = text;
        }

        Model parse() {
            this.root = new Node();
            this.context = root;

            int start = 0;
            for (int k = 0; k < text.length(); k++) {
                final char ch = text.charAt(k);
                if (COMMENT == mark && !isNewLine(ch)) {
                    continue;
                }
                if (Character.isWhitespace(ch) || ';' == ch) {
                    emit(start, k);
                    if (isNewLine(ch) || ';' == ch) {
                        emit(';');
                    }
                    start = k + 1;
                } else if ('+' == ch || '-' == ch || '*' == ch || '/' == ch ||
                        '@' == ch || '(' == ch || ')' == ch ||
                        '<' == ch || '>' == ch || '=' == ch ||
                        '#' == ch || ',' == ch) {
                    emit(start, k);
                    emit(ch);
                    start = k + 1;
                } else if (Character.isLetterOrDigit(ch) || '.' == ch) {
                    // NO-OP
                } else {
                    throw new RuntimeException("unknown character '" + ch + "'");
                }
            }
            emit(start, text.length());
            endMark();
            return complete();
        }

        private Model complete() {
            LOG.debug(root);

            Model m = new Model();
            Map<String, Integer> varIndexes = new HashMap<>();
            Map<String, Double> cVector = new HashMap<>();
            List<Map<String, Double>> aMatrix = new ArrayList<>();
            List<Double> bVector = new ArrayList<>();
            List<Sign> signs = new ArrayList<>();
            List<String> binVars = new ArrayList<>();
            List<String> intVars = new ArrayList<>();
            List<String> freeVars = new ArrayList<>();

            // objective
            m.objectiveType = ObjectiveType.valueOf(root.target);
            m.addComments(root.comments);

            // the vector c
            for (String e : root.exprs) {
                Expr expr = Expr.valueOf(e);
                LOG.debug(expr);
                if (expr.isConstant()) {
                    m.c0 += expr.coef;
                    continue;
                }
                meetVar(m, varIndexes, expr);
                cVector.put(expr.var, expr.coef);
            }

            // constraints, the matrix A, the vector b
            for (Node n : root.children) {
                LOG.debug(n);
                m.addComments(n.comments);

                if (n.exprs.isEmpty()) {
                    continue;
                }

                //  @bin/@int etc.
                if (Strings.isEmpty(n.target)) {
                    // should be func
                    processFunc(binVars, intVars, freeVars, n);
                    continue;
                }

                // else: n.target not empty
                bVector.add(Expr.valueOf(n.target).coef);
                signs.add(n.sign);
                // the matrix A
                Map<String, Double> a = new HashMap<>();
                for (String e : n.exprs) {
                    Expr expr = Expr.valueOf(e);
                    if (expr.isConstant() || expr.isFunc()) {
                        LOG.warn("constant or func for matrix A is not supported yet", expr, "ignored");
                        continue;
                    }
                    meetVar(m, varIndexes, expr);
                    a.put(expr.var, expr.coef);
                }
                aMatrix.add(a);
            }

            // assemble
            assemble(m, varIndexes, cVector, aMatrix, bVector, signs, binVars, intVars, freeVars);
            LOG.debug("assemble completed", m);
            return m;
        }

        private void assemble(Model m, Map<String, Integer> varIndexes,
                              Map<String, Double> cVector,
                              List<Map<String, Double>> aMatrix,
                              List<Double> bVector,
                              List<Sign> signs,
                              List<String> binVars,
                              List<String> intVars,
                              List<String> freeVars) {
            final int n = varIndexes.size();
            double[] c = new double[n];
            double[][] a = new double[bVector.size()][n];

            cVector.forEach((var, v) -> {
                Integer j = varIndexes.get(var);
                if (j != null) {
                    c[j] = v;
                }
            });

            for (int k = 0; k < aMatrix.size(); k++) {
                final int i = k;
                Map<String, Double> row = aMatrix.get(i);
                row.forEach((var, v) -> {
                    Integer j = varIndexes.get(var);
                    if (j != null) {
                        a[i][j] = v;
                    }
                });
            }

            m.c = c;
            m.a = a;
            m.b = Maths.toDoubleArray(bVector);
            m.signs = signs.toArray(new Sign[0]);
            m.binVars = toSubscripts(binVars, varIndexes);
            m.intVars = toSubscripts(intVars, varIndexes);
            m.freeVars = toSubscripts(freeVars, varIndexes);
        }

        private int[] toSubscripts(List<String> vars, Map<String, Integer> varIndexs) {
            List<Integer> r = new ArrayList<>();
            vars.forEach(var -> {
                Integer j = varIndexs.get(var); // index
                if (null != j) {
                    Integer k = j + 1; // subscript
                    if (!r.contains(k)) {
                        r.add(k);
                    }
                }
            });
            return Maths.toIntArray(r);
        }

        private void processFunc(List<String> binVars, List<String> intVars, List<String> freeVars, Node n) {
            Expr func = Expr.valueOf(n.exprs.get(0));
            if (!func.isFunc()) {
                LOG.warn("not func expr", func, n, "ignored");
                return;
            }
            for (int i = 1; i < n.exprs.size(); i++) {
                String v = n.exprs.get(i);
                if (FUNC_BIN.equals(func.var)) {
                    binVars.add(v);
                } else if (FUNC_INT.equals(func.var)) {
                    intVars.add(v);
                } else if (FUNC_FREE.equals(func.var)) {
                    freeVars.add(v);
                } else {
                    LOG.warn("unknown func", func, n, "ignored");
                }
            }
        }

        private void meetVar(Model m, Map<String, Integer> varIndexes, Expr expr) {
            // expr.var first met
            if (m.addVariable(expr.var)) {
                varIndexes.put(expr.var, m.sizeOfVariables() - 1);
            }
        }

        static final byte END = 0; // end mark
        static final byte COMMENT = 1;
        static final byte OBJECTIVE = 2;
        static final byte TARGET = 3;

        byte mark = END;
        StringBuilder expr = new StringBuilder();

        private void emit(int start, int end) {
            if (start == end) {
                return;
            }

            String token = text.substring(start, end);
            LOG.debug("parser", "token", token, '[', start, "->", end, ']');

            if (COMMENT == mark) {
                context.addComment(token);
                endMark();
                return;
            }

            if (null == root.target) { // objective
                ObjectiveType.of(token).ifPresent(type -> {
                    root.target = type.name();
                    mark = OBJECTIVE;
                });
            } else {
                expr.append(token);
            }

            LOG.debug("expr", expr);
        }

        private void endMark() {
            endExpr();
            mark = END;
        }

        private void endExpr() {
            if (0 == expr.length()) {
                return;
            }
            if (TARGET == mark) {
                context.target = expr.toString();
                mark = END;
            } else {
                context.addExpr(expr.toString());
            }
            expr.setLength(0);
        }

        private void emit(char ch) {
            LOG.debug("parser", "token", ch);
            if ('#' == ch) {
                mark = COMMENT;
            } else if ('@' == ch || '*' == ch || '/' == ch) {
                expr.append(ch);
            } else if (';' == ch || '+' == ch || '-' == ch ||
                    '<' == ch || '>' == ch || '=' == ch ||
                    ',' == ch || '(' == ch) {
                endExpr();
                if ('-' == ch) {
                    expr.append(ch);
                } else if (';' == ch) { // new line
                    newContext();
                } else if ('<' == ch || '>' == ch || '=' == ch) {
                    if (null == context.sign) {
                        context.sign = Sign.of(ch);
                    }
                    if (null == context.target) {
                        mark = TARGET;
                    }
                }
            }
        }

        private void newContext() {
            // check objective & empty node
            if (null == root.target || context.exprs.isEmpty()) {
                return;
            }

            Node node = new Node();
            if (context == root) {
                // a child
                context.addChild(node);
            } else {
                // a brother
                context.parent.addChild(node);
            }
            context = node;
        }

        private boolean isNewLine(char ch) {
            return '\n' == ch || '\r' == ch;
        }
    }
}
