package com.github.cloudecho.bnb;

public interface Solver {
    int DEFAULT_PRECISION = 7;

    void solve();

    State getState();

    ObjectiveType getObjectiveType();

    double getObjective();

    double[] getX();

    int getIterations();

    void setPrecision(int precision);

    int getPrecision();

    String toString();
}
