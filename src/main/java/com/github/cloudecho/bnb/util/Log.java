package com.github.cloudecho.bnb.util;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    private static final Handler CONSOLE_HANDLER = new ConsoleHandler();
    private final Logger logger;

    static {
        CONSOLE_HANDLER.setLevel(Level.FINER);
    }

    Log(String name) {
        this.logger = Logger.getLogger(name);
        this.logger.setUseParentHandlers(false);
        this.logger.addHandler(CONSOLE_HANDLER);
    }

    public void trace(Object... msg) {
        if (isTraceEnabled()) {
            logger.finer(toString(msg));
        }
    }

    public void debug(Object... msg) {
        if (isDebugEnabled()) {
            logger.fine(toString(msg));
        }
    }

    public void info(Object... msg) {
        if (isInfoEnabled()) {
            logger.info(toString(msg));
        }
    }

    public void warn(Object... msg) {
        if (isWarnEnabled()) {
            logger.warning(toString(msg));
        }
    }

    public void error(Object... msg) {
        if (isErrorEnabled()) {
            logger.severe(toString(msg));
        }
    }

    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINER);
    }

    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    private boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    private boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    private boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    public void setLevel(Level level) {
        logger.setLevel(level);
    }

    public void addHandler(Handler handler) {
        logger.addHandler(handler);
    }

    private String toString(Object... msg) {
        StringBuilder b = new StringBuilder(Thread.currentThread().getName());
        for (Object m : msg) {
            b.append(' ');
            if (m instanceof Object[]) {
                b.append(Arrays.deepToString((Object[]) m));
            } else if (m instanceof int[]) {
                b.append(Arrays.toString((int[]) m));
            } else if (m instanceof double[]) {
                b.append(Arrays.toString((double[]) m));
            } else if (m instanceof Throwable) {
                Throwable ex = ((Throwable) m);
                b.append(ex);
                for (StackTraceElement e : ex.getStackTrace()) {
                    b.append("\n at ").append(e);
                }
            } else {
                b.append(m);
            }
        }
        return b.toString();
    }
}
