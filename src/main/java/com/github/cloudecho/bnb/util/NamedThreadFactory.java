package com.github.cloudecho.bnb.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// Modified from DefaultThreadFactory
public class NamedThreadFactory implements ThreadFactory {
    private ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private String namePrefix = "noname-thread";

    public NamedThreadFactory() {
        group = Thread.currentThread().getThreadGroup();
    }

    public NamedThreadFactory namePrefix(String namePrefix) {
        if (Strings.hasLength(namePrefix)) {
            this.namePrefix = namePrefix;
        }
        return this;
    }

    public NamedThreadFactory group(ThreadGroup group) {
        if (group != null) {
            this.group = group;
        }
        return this;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + '-' + threadNumber.getAndIncrement(),
                0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
