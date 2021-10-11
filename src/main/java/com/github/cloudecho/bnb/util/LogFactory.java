package com.github.cloudecho.bnb.util;

public class LogFactory {
    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %3$s %4$s %5$s%6$s%n");
    }

    private LogFactory() {

    }

    public static Log getLog(Class<?> clazz) {
        String pkg = clazz.getPackage().getName();
        int k = pkg.lastIndexOf('.');
        if (k >= 0) {
            pkg = pkg.substring(k + 1);
        }
        return getLog(pkg + '.' + clazz.getSimpleName());
    }

    public static Log getLog(String name) {
        return new Log(name);
    }
}
