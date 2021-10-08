package com.github.cloudecho.bnb;

import java.util.Optional;

public enum ObjectiveType {
    max, min;

    public boolean isMax() {
        return max.equals(this);
    }

    public static Optional<ObjectiveType> of(String name) {
        for (ObjectiveType t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }
}
