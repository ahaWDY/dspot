package eu.stamp_project.dspot.common.report.output;

import eu.stamp_project.dspot.common.report.Report;

public abstract class AmplifierReport {

    protected boolean assertionReport = isAssertionReport();
    protected String reportType = getReportType();

    public abstract boolean isAssertionReport();

    public String getReportType() {
        return this.getClass().getCanonicalName();
    }

}
