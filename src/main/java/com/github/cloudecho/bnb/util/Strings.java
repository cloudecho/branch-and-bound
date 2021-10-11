package com.github.cloudecho.bnb.util;

public class Strings {
    private Strings() {

    }

    public static boolean hasLength(String str) {
        return null != str && str.length() > 0;
    }

    public static boolean isEmpty(String str) {
        return null == str || str.length() == 0;
    }
}
