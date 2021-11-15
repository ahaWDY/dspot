package fr.inria.amplified;

import eu.stamp_project.App;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 26/02/18
 */
public class AmplifiedTest {

    @Test
    public void amplifiedTest() throws Exception {
        int __DSPOT_1 = 5;
        assertEquals(5, __DSPOT_1);
    }

    @Test
    public void amplifiedTest2() throws Exception {
        Integer __DSPOT_1 = 5;
        assertEquals(5, __DSPOT_1.intValue());
        System.out.println(__DSPOT_1.intValue());
        assertEquals(5, __DSPOT_1.intValue());
    }

    @Test
    public void amplifiedTest3() throws Exception {
        Integer __DSPOT_1 = 5;
        assertEquals(5, __DSPOT_1.intValue());
        assertEquals(5, __DSPOT_1.intValue());
    }

    @Test
    public void amplifiedTestWithOldVariable() throws Exception {
        int testingAnInt = 5;
        assertEquals(5, testingAnInt);
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
