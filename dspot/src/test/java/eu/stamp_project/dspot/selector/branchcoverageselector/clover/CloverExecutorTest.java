package eu.stamp_project.dspot.selector.branchcoverageselector.clover;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CloverExecutorTest {

    @Test
    void instrumentAndRunGivenTestClass() {
        new CloverExecutor().instrumentAndRunGivenTestClass("F:\\tavern","CalculatorTest");
//        new CloverExecutor().instrumentAndRunGivenTestClass("F:\\jsoup","AttributeTest");
    }
}