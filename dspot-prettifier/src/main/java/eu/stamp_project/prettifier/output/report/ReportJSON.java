package eu.stamp_project.prettifier.output.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.common.report.output.selector.TestClassJSON;
import eu.stamp_project.prettifier.Main;
import eu.stamp_project.prettifier.Util;
import eu.stamp_project.prettifier.configuration.UserInput;
import eu.stamp_project.prettifier.output.report.minimization.general.GeneralMinimizationJSON;
import eu.stamp_project.prettifier.output.report.minimization.pit.PitMinimizationJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

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

    public int nbTestMethods;

    public double medianNbStatementBefore;

    public double medianNbStatementAfter;

    public ReportJSON(UserInput configuration) {
        this.generalMinimizationJSON = new GeneralMinimizationJSON();
        this.pitMinimizationJSON = new PitMinimizationJSON();
        this.amplificationReport = Util.readExtendedCoverageResultJSON(configuration);
    }

    public void output(UserInput configuration, CtType<?> amplifiedTestClass) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String pathname =
                configuration.getOutputDirectory() + File.separator + amplifiedTestClass.getQualifiedName() +
                "_prettifier_report.json";
        LOGGER.info("Output a report in {}", pathname);
        final File file = new File(pathname);
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(Main.report));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
