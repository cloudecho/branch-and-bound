package com.github.cloudecho.bnb;

public enum Sign {
    /**
     * Equal (=)
     */
    EQ("="),

    /**
     * Greater or equal (>=)
     */
    GE(">="),

    /**
     * Less or equal (<=)
     */
    LE("<=");

    String sign;

    Sign(String sign) {
        this.sign = sign;
    }

    public String getString() {
        return sign;
    }

    public boolean isEquality() {
        return EQ.equals(this);
    }

    public static Sign of(char ch) {
        if ('<' == ch) {
            return LE;
        } else if ('>' == ch) {
            return GE;
        } else if ('=' == ch) {
            return EQ;
        } else {
            throw new IllegalArgumentException("unknown sign '" + ch + "'");
        }
    }
}
