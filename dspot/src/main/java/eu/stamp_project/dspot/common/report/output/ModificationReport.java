package eu.stamp_project.dspot.common.report.output;

import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.report.Report;
import eu.stamp_project.dspot.common.report.output.amplifiers.AmplifierReport;
import eu.stamp_project.dspot.common.report.output.assertiongenerator.AssertionGeneratorReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ModificationReport implements Report {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModificationReport.class);

    private Map<CtType<?>, ClassModificationReport> reportsPerClass;

    public ModificationReport() {
        this.reportsPerClass = new HashMap<>();
    }

    @Override
    public void output(String outputDirectory) {
        final String allReports = new GsonBuilder().setPrettyPrinting().create().toJson(this).toString();
        LOGGER.info("{}{}", AmplificationHelper.LINE_SEPARATOR, allReports);
        final String reportPathName = DSpotUtils.shouldAddSeparator.apply(outputDirectory) + "_modification_report.txt";
        try (FileWriter writer = new FileWriter(reportPathName, false)) {
            writer.write(allReports);
            LOGGER.info("Writing report in {}", reportPathName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void reset() {
        reportsPerClass.clear();
    }

    public void reportModification(CtType<?> testClass, String testName, AssertionGeneratorReport report) {
        ClassModificationReport classModificationReport =
                reportsPerClass.computeIfAbsent(testClass, s -> new ClassModificationReport());
        List<AssertionGeneratorReport> assertionReports =
                classModificationReport.assertionGeneratorReportsPerMethod.computeIfAbsent(testName,
                        s -> new ArrayList<>(1));
        assertionReports.add(report);
    }

    private class ClassModificationReport {

        private Map<String, List<AmplifierReport>> inputAmplifierReportsPerMethod;
        private Map<String, List<AssertionGeneratorReport>> assertionGeneratorReportsPerMethod;

        public ClassModificationReport() {
            this.inputAmplifierReportsPerMethod = new HashMap<>();
            this.assertionGeneratorReportsPerMethod = new HashMap<>();
        }

        public String output() {
            return "not implemented yet";
        }

        public void reset() {
            this.inputAmplifierReportsPerMethod.clear();
            this.assertionGeneratorReportsPerMethod.clear();
        }
    }
}
