package com.github.cloudecho.bnb;

import java.util.*;

public class Maths {
    private Maths() {

    }

    public static double round(double value, int precision) {
        double scale = Math.pow(10, precision);
        return Math.round(value * scale) / scale;
    }

    public static List<Integer> unique(int[] vars) {
        if (vars == null || 0 == vars.length) {
            return Collections.EMPTY_LIST;
        }

        Set<Integer> s = new HashSet<>();
        for (int v : vars) {
            s.add(v);
        }

        return new ArrayList<>(s);
    }
}
