package com.github.cloudecho.bnb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Maths {
    private Maths() {

    }

    public static double round(double value, int precision) {
        double scale = Math.pow(10, precision);
        return Math.round(value * scale) / scale;
    }

    static final int[] EMPTY_INT_ARRAY = new int[0];
    static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    public static int[] unique(int[] vars) {
        if (0 == length(vars)) {
            return EMPTY_INT_ARRAY;
        }

        // keep the order
        List<Integer> s = new ArrayList<>(vars.length);
        for (int v : vars) {
            if (s.contains(v)) {
                continue;
            }
            s.add(v);
        }

        // to int array
        return toIntArray(s);
    }

    public static int[] union(int[] a1, int[] a2) {
        if (0 == length(a1) && 0 == length(a2)) {
            return EMPTY_INT_ARRAY;
        }

        if (0 == length(a1)) {
            return a2;
        }

        if (0 == length(a2)) {
            return a1;
        }

        int[] r = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, r, a1.length, a2.length);
        return r;
    }

    public static int length(int[] a) {
        if (a == null) {
            return 0;
        }
        return a.length;
    }

    public static boolean contains(int[] arr, final int i) {
        if (arr == null) {
            return false;
        }
        for (int k : arr) {
            if (i == k) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ceiling & floor of a number
     */
    public static class CnF {
        public final int ceil;
        public final int floor;

        public CnF(double d) {
            this.ceil = (int) Math.ceil(d);
            this.floor = (int) Math.floor(d);
        }

        /**
         * Return {@code true} if {@code ceil == floor}
         */
        public boolean eq() {
            return floor == ceil;
        }
    }

    public static double[][] append(double[][] a, double[] row) {
        // shallow copy
        double[][] r = Arrays.copyOf(a, a.length + 1);
        r[a.length] = row;
        return r;
    }

    public static double[] append(double[] b, double d) {
        double[] r = Arrays.copyOf(b, b.length + 1);
        r[b.length] = d;
        return r;
    }

    public static Sign[] append(Sign[] signs, Sign s) {
        Sign[] r = Arrays.copyOf(signs, signs.length + 1);
        r[signs.length] = s;
        return r;
    }

    public static int[] toIntArray(List<Integer> list) {
        if (null == list || list.isEmpty()) {
            return EMPTY_INT_ARRAY;
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    public static double[] toDoubleArray(List<Double> list) {
        if (null == list || list.isEmpty()) {
            return EMPTY_DOUBLE_ARRAY;
        }
        return list.stream().mapToDouble(Double::doubleValue).toArray();
    }
}
