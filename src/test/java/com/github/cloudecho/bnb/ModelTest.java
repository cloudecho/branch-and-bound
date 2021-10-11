package com.github.cloudecho.bnb;

import com.github.cloudecho.bnb.util.Maths;
import com.github.cloudecho.bnb.util.Sign;
import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Level;

public class ModelTest {
    static {
        Model.LOG.setLevel(Level.ALL);
    }

    @Test
    public void testValueOf() {
        Model a = Model.valueOf(Model.sample());
        Assert.assertEquals("objectiveType", ObjectiveType.max, a.objectiveType);

        Assert.assertEquals("variables.size", 4, a.variables.size());
        Assert.assertEquals("var1", "x1", a.variables.get(0));
        Assert.assertEquals("var2", "x2", a.variables.get(1));
        Assert.assertEquals("var3", "x3", a.variables.get(2));
        Assert.assertEquals("var4", "x4", a.variables.get(3));

        Assert.assertEquals("c0*10", -588, (int) (10 * a.c0));
        Assert.assertEquals("c1*10", 22, (int) (10 * a.c[0]));
        Assert.assertEquals("c2", 1, (int) a.c[1]);
        Assert.assertEquals("c3*10", -15, (int) (10 * a.c[2]));
        Assert.assertEquals("c4", 1, (int) (a.c[3]));

        Assert.assertEquals("a11", 21, (int) (10 * a.a[0][0]));
        Assert.assertEquals("a12", 32, (int) (10 * a.a[0][1]));
        Assert.assertEquals("a13", 0, (int) a.a[0][2]);
        Assert.assertEquals("a14", 0, (int) a.a[0][3]);

        Assert.assertEquals("a21", 54, (int) (10 * a.a[1][0]));
        Assert.assertEquals("a22", -43, (int) (10 * a.a[1][1]));
        Assert.assertEquals("a23", 0, (int) a.a[1][2]);
        Assert.assertEquals("a24", 0, (int) a.a[1][3]);

        Assert.assertEquals("a31", 0, (int) a.a[2][0]);
        Assert.assertEquals("a32", 1, (int) a.a[2][1]);
        Assert.assertEquals("a33", 1, (int) a.a[2][2]);
        Assert.assertEquals("a34", 0, (int) a.a[2][3]);

        Assert.assertEquals("a41", 1, (int) a.a[3][0]);
        Assert.assertEquals("a42", 1, (int) a.a[3][1]);
        Assert.assertEquals("a43", -1, (int) a.a[3][2]);
        Assert.assertEquals("a44", 1, (int) a.a[3][3]);

        Assert.assertEquals("b1", 35, (int) a.b[0]);
        Assert.assertEquals("b2", 26, (int) a.b[1]);
        Assert.assertEquals("b3", 50, (int) a.b[2]);
        Assert.assertEquals("b4", 100, (int) a.b[3]);

        Assert.assertEquals("signs1", Sign.LE, a.signs[0]);
        Assert.assertEquals("signs2", Sign.LE, a.signs[1]);
        Assert.assertEquals("signs3", Sign.GE, a.signs[2]);
        Assert.assertEquals("signs4", Sign.EQ, a.signs[3]);

        Assert.assertEquals("freeVars.length", 1, Maths.length(a.freeVars));
        Assert.assertEquals("intVars.length", 2, Maths.length(a.intVars));
        Assert.assertEquals("binVars.length", 1, Maths.length(a.binVars));
        Assert.assertEquals("freeVar1", 4, a.freeVars[0]);
        Assert.assertEquals("intVar1", 2, a.intVars[0]);
        Assert.assertEquals("intVar2", 3, a.intVars[1]);
        Assert.assertEquals("binVar1", 1, a.binVars[0]);
    }
}
