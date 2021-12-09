package eu.stamp_project.dspot.common.report.output.assertiongenerator;

import eu.stamp_project.dspot.common.report.output.AmplifierReport;

public class ValueAssertionReport extends AmplifierReport {

    private String calledMethod;
    private String expectedValue;

    public ValueAssertionReport(String calledMethod, String expectedValue) {
        this.calledMethod = calledMethod;
        this.expectedValue = expectedValue;
    }

    @Override
    public boolean isAssertionReport() {
        return true;
    }

}
