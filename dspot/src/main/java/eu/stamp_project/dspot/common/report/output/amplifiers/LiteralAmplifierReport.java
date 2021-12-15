package eu.stamp_project.dspot.common.report.output.amplifiers;

import eu.stamp_project.dspot.common.report.output.AmplifierReport;

/**
 * Reports any changes where a literal was changed to another value.
 * E.g. by the {@link eu.stamp_project.dspot.amplifier.amplifiers.FastLiteralAmplifier},
 * or the {@link eu.stamp_project.dspot.amplifier.amplifiers.NullifierAmplifier}
 */
public class LiteralAmplifierReport extends AmplifierReport {

    private String variableName;
    private String originalValue;
    private String newValue;

    public LiteralAmplifierReport(String variableName, String originalValue, String newValue) {
        this.variableName = variableName;
        this.originalValue = originalValue;
        this.newValue = newValue;
    }

    @Override
    public boolean isAssertionReport() {
        return false;
    }

}
