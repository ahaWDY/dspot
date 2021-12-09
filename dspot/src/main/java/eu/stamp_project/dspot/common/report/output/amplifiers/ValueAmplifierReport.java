package eu.stamp_project.dspot.common.report.output.amplifiers;

import eu.stamp_project.dspot.common.report.output.AmplifierReport;

public class ValueAmplifierReport extends AmplifierReport {

    private String variableName;
    private String originalValue;
    private String newValue;

    public ValueAmplifierReport(String variableName, String originalValue, String newValue) {
        this.variableName = variableName;
        this.originalValue = originalValue;
        this.newValue = newValue;
    }

    @Override
    public boolean isAssertionReport() {
        return false;
    }

}
