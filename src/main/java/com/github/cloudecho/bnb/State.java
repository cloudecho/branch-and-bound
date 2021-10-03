package com.github.cloudecho.bnb;

public enum State {
    ZERO, SOLVING, SOLVED, UNBOUNDED, NO_SOLUTION;

    public boolean isSolved() {
        return SOLVED.equals(this);
    }
}
