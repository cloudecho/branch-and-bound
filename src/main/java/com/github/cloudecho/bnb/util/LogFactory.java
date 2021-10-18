package com.github.cloudecho.bnb.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

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

    public static final String LOG_LEVEL_PROP = "com.github.cloudecho.bnb.LOG_LEVEL";

    public static Log getLog(String name) {
        Log log = new Log(name);
        getConfiguredLogLevel().ifPresent(log::setLevel);
        return log;
    }

    static final List<Level> LOG_LEVELS = Arrays.asList(
            Level.ALL,
            Level.FINEST,
            Level.FINER,
            Level.FINE,
            Level.INFO,
            Level.SEVERE,
            Level.WARNING,
            Level.CONFIG,
            Level.OFF);

    static Optional<Level> getConfiguredLogLevel() {
        return getConfiguredLogLevel(LOG_LEVEL_PROP);
    }

    public static Optional<Level> getConfiguredLogLevel(String prop) {
        String level = System.getProperty(prop, "");
        if (Strings.isEmpty(level)) {
            return Optional.empty();
        }
        for (Level l : LOG_LEVELS) {
            if (level.equalsIgnoreCase(l.getName())) {
                return Optional.of(l);
            }
        }
        return Optional.empty();// no match
    }
}
