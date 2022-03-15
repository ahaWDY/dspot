package eu.stamp_project.dspot.selector.branchcoverageselector.clover;

<<<<<<< HEAD
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CloverExecutorTest {

    @Test
    public void instrumentAndRunGivenTest() {
        List<String> testBody = Arrays.asList("html", "hasValue");
        Map<String, List<String>> test = new HashMap<String, List<String>>();
        test.put("AttributeTest", testBody);
//        new CloverExecutor().instrumentAndRunGivenTest("F:\\jsoup", test);
        new CloverExecutor().instrumentAndRunGivenTestClass("F:\\jsoup","AttributeTest");
=======
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CloverExecutorTest {

    @Test
    void instrumentAndRunGivenTestClass() {
        new CloverExecutor().instrumentAndRunGivenTestClass("F:\\tavern","CalculatorTest");
//        new CloverExecutor().instrumentAndRunGivenTestClass("F:\\jsoup","AttributeTest");
>>>>>>> 83a95628a0d0628be0e156673f0269e213b08878
    }
}