package com.github.cloudecho.bnb;

public enum ObjectiveType {
    MAX, MIN;

    public boolean isMax() {
        return MAX.equals(this);
    }
}
