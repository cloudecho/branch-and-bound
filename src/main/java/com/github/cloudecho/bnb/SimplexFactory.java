package com.github.cloudecho.bnb;

public class SimplexFactory {
    private SimplexFactory() {

    }

    public enum SimplexType {
        REVISED, NORMAL, BIG;

        static SimplexType of(String name) {
            for (SimplexType t : values()) {
                if (t.name().equalsIgnoreCase(name)) {
                    return t;
                }
            }
            return NORMAL;
        }
    }

    public static final String SIMPLEX_TYPE_PROP = "com.github.cloudecho.bnb.SIMPLEX_TYPE";
    static final SimplexType SIMPLEX_TYPE = SimplexType.of(System.getProperty(SIMPLEX_TYPE_PROP, "REVISED"));

    public static Simplex newSimplex(double[] c, double[][] a, double[] b) {
        return newSimplex(SIMPLEX_TYPE, c, a, b);
    }

    public static Simplex newSimplex(SimplexType t, double[] c, double[][] a, double[] b) {
        switch (t) {
            case BIG:
                return new BigSimplex(c, a, b);
            case REVISED:
            default:
                return new RevisedSimplex(c, a, b);
            case NORMAL:
                return new Simplex(c, a, b);
        }
    }
}
