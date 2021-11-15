package eu.stamp_project;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppTest {

    @Test
    public void test1() {
        App app = new App(4);
        assertEquals(4, app.getInt());
        app.compute();
        assertEquals(8, app.getInt());
        app.compute(10);
        assertEquals(40, app.getInt());
        app.compute(3);
        assertEquals(60, app.getInt());
    }

    @Test
    public void test2_failAssert0() throws Exception {
        // AssertionGenerator generate try/catch block with fail statement
        try {
            App app = new App(4);
            app.throwException();
            Assert.fail("test2 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals("Illegal Arg", expected.getMessage());
        }
    }

    @Test(timeout = 10000)
    public void test1_mg12_assSep41() throws Exception {
        int __DSPOT_j_0 = -1150482841;
        App app = new App(4);
        app.compute(__DSPOT_j_0);
        Assert.assertEquals(-153482034, ((int) (((App) (app)).getInt())));
    }

    @Test(timeout = 10000)
    public void test1_mg13_failAssert0() throws Exception {
        try {
            App app = new App(4);
            app.throwException();
            Assert.fail("test1_mg13 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals("Illegal Arg", expected.getMessage());
        }
    }
}