package com.github.cloudecho.bnb;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

class Log {
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

    void debug(Object... msg) {
        logger.fine(toString(msg));
    }

    void info(Object... msg) {
        logger.info(toString(msg));
    }

    void warn(Object... msg) {
        logger.warning(toString(msg));
    }

    void error(Object... msg) {
        logger.severe(toString(msg));
    }

    void setLevel(Level level) {
        logger.setLevel(level);
    }

    private String toString(Object... msg) {
        StringBuilder b = new StringBuilder(Thread.currentThread().getName());
        for (Object m : msg) {
            b.append(' ').append(m);
        }
        return b.toString();
    }
}
