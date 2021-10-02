package com.github.cloudecho.bnb;

public class Maths {
    private Maths() {

    }

    public static double round(double value, int precision) {
        double scale = Math.pow(10, precision);
        return Math.round(value * scale) / scale;
    }
}
