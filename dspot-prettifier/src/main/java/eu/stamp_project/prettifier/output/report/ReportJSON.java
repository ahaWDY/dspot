package eu.stamp_project.prettifier.output.report;

import eu.stamp_project.dspot.common.report.output.selector.TestClassJSON;
import eu.stamp_project.prettifier.Util;
import eu.stamp_project.prettifier.configuration.UserInput;
import eu.stamp_project.prettifier.output.report.minimization.general.GeneralMinimizationJSON;
import eu.stamp_project.prettifier.output.report.minimization.pit.PitMinimizationJSON;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/02/19
 */
public class ReportJSON {

    public GeneralMinimizationJSON generalMinimizationJSON;

    public PitMinimizationJSON pitMinimizationJSON;

    public TestClassJSON amplificationReport;

    public int nbTestMethods;

    public double medianNbStatementBefore;

    public double medianNbStatementAfter;

    public ReportJSON(UserInput configuration) {
        this.generalMinimizationJSON = new GeneralMinimizationJSON();
        this.pitMinimizationJSON = new PitMinimizationJSON();
        this.amplificationReport = Util.readExtendedCoverageResultJSON(configuration);
    }

}
