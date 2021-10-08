package com.github.cloudecho.bnb;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class ByteArrayLogHandler extends StreamHandler {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public ByteArrayLogHandler() {
        setOutputStream(out);
        setLevel(Level.FINER);
    }

    public String getString() {
        String r = out.toString();
        out.reset();
        return r;
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    /**
     * Override <tt>StreamHandler.close</tt> to do a flush but not
     * to close the output stream.
     */
    @Override
    public void close() {
        flush();
    }
}
