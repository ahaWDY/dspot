package eu.stamp_project.dspot.selector.extendedcoverageselector;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.impl.CoverageImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedCoverageTest {

    String EXECUTION_PATH_1 = "package/package/class:1,0,3,0,0,4";
    String EXECUTION_PATH_2 = "package/package/class:1,0,3,5,0,1";
    String EXECUTION_PATH_3 = "package/package/class:1,0,3,5,0,4";

    private ExtendedCoverage genExtendedCoverageWithPath(String executionPath) {
        Coverage coverage = new CoverageImpl(8, 10);
        coverage.setExecutionPath(executionPath);

        return new ExtendedCoverage(coverage);
    }

    @Test
    public void constructExtendedCoverageFromCoverage() {
        ExtendedCoverage extendedCoverage = genExtendedCoverageWithPath(EXECUTION_PATH_1);

        Map<String, List<Integer>> instructionsCoveredPerClass = new HashMap<>();
        instructionsCoveredPerClass.put("package/package/class", Arrays.asList(1, 0, 3, 0, 0, 4));

        Assert.assertEquals(extendedCoverage.getInstructionsCoveredPerClass(),instructionsCoveredPerClass);
    }

    @Test
    public void testAccumulate() {
        ExtendedCoverage extendedCoverage = genExtendedCoverageWithPath(EXECUTION_PATH_1);
        ExtendedCoverage extendedCoverage2 = genExtendedCoverageWithPath(EXECUTION_PATH_2);

        extendedCoverage.accumulate(extendedCoverage2);

        Assert.assertEquals(extendedCoverage,genExtendedCoverageWithPath(EXECUTION_PATH_3));
    }

}
