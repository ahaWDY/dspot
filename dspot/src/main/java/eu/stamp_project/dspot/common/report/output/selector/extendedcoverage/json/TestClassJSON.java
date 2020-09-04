package eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json;

import java.util.ArrayList;
import java.util.List;

public class TestClassJSON implements eu.stamp_project.dspot.common.report.output.selector.TestClassJSON {

    private List<TestCaseJSON> testCases;

    public TestClassJSON() {
    }

    public boolean addTestCase(TestCaseJSON testCaseJSON) {
        if (this.testCases == null) {
            this.testCases = new ArrayList<>();
        }
        return this.testCases.add(testCaseJSON);
    }

    public List<TestCaseJSON> getTestCases() {
        return this.testCases;
    }
}
