package com.github.cloudecho.bnb;

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

    public void addHandler(Handler handler){
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
            } else {
                b.append(m);
            }
        }
        return b.toString();
    }
}
