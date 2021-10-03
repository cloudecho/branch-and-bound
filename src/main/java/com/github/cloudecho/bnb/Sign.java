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
}
