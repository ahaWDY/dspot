package eu.stamp_project.dspot.common.report.output.amplifiers;

import eu.stamp_project.dspot.common.report.output.AmplifierReport;

public class AddLocalVariableAmplifierReport extends AmplifierReport {

    private String variableName;
    private String variableValue;

    public AddLocalVariableAmplifierReport(String variableName, String variableValue) {
        this.variableName = variableName;
        this.variableValue = variableValue;
    }

    @Override
    public boolean isAssertionReport() {
        return false;
    }
}
