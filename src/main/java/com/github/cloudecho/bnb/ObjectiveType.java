package com.github.cloudecho.bnb;

public enum ObjectiveType {
    max, min;

    public boolean isMax() {
        return max.equals(this);
    }
}
