package eu.stamp_project.dspot.common.report.output.assertiongenerator;

public class ValueAssertionReport extends AssertionGeneratorReport {

    private String calledMethod;
    private String expectedValue;

    public ValueAssertionReport(String calledMethod, String expectedValue) {
        this.calledMethod = calledMethod;
        this.expectedValue = expectedValue;
    }

}
