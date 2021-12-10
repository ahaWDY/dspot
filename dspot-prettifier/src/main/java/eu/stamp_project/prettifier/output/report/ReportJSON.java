package eu.stamp_project.prettifier.output.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.common.report.output.ClassModificationReport;
import eu.stamp_project.dspot.common.report.output.ModificationReport;
import eu.stamp_project.dspot.common.report.output.selector.TestClassJSON;
import eu.stamp_project.prettifier.Main;
import eu.stamp_project.prettifier.Util;
import eu.stamp_project.prettifier.configuration.UserInput;
import eu.stamp_project.prettifier.output.report.minimization.general.GeneralMinimizationJSON;
import eu.stamp_project.prettifier.output.report.minimization.pit.PitMinimizationJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;
import sun.java2d.pipe.OutlineTextRenderer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 25/02/19
 */
public class ReportJSON {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportJSON.class);

    public GeneralMinimizationJSON generalMinimizationJSON;

    public PitMinimizationJSON pitMinimizationJSON;

    public TestClassJSON amplificationReport;

    public ClassModificationReport modificationReport;

    public int nbTestMethods;

    public double medianNbStatementBefore;

    public double medianNbStatementAfter;

    public ReportJSON(UserInput configuration) {
        this.generalMinimizationJSON = new GeneralMinimizationJSON();
        this.pitMinimizationJSON = new PitMinimizationJSON();
        this.amplificationReport = Util.readExtendedCoverageResultJSON(configuration);
        this.modificationReport = Util.readModificationReport(configuration);
    }

    public void output(UserInput configuration, CtType<?> amplifiedTestClass) {
        Util.writeReportJSON(configuration, this, "_prettifier");
    }

    public boolean isExtendedCoverageReportPresent(String prettifierToApply) {
        TestClassJSON report;
        try {
            report =
                    (eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON) amplificationReport;
        } catch (ClassCastException e) {
            LOGGER.error("No DSpot output is not from ExtendedCoverageSelector! " + prettifierToApply
                    + " not applied");
            return false;
        }
        if (report == null) {
            LOGGER.error("No json found under configured DSpot output path! TestDescriptionGenerator not " +
                    "applied");
            return false;
        }
        return true;
    }

    public boolean isModificationReportPresent(String prettifierToApply) {
        ClassModificationReport report = Main.report.modificationReport;
        if (report == null) {
            LOGGER.error("No modification report found under configured DSpot output path! " + prettifierToApply
                    + " not applied");
            return false;
        }
        return true;
    }
}
