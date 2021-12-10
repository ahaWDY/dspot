package eu.stamp_project.dspot.common.report.output;

import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ClassModificationReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassModificationReport.class);

    // maps the simple name of a method to its modifications
    private Map<String, List<AmplifierReport>> reportsPerMethod;

    public ClassModificationReport() {
        this.reportsPerMethod = new HashMap<>();
    }

    public void output(String testClass, String outputDirectory) {
        final String allReports = new GsonBuilder().setPrettyPrinting().create().toJson(this).toString();
        LOGGER.info("{}{}", AmplificationHelper.LINE_SEPARATOR, allReports);
        final String reportPathName =
                DSpotUtils.shouldAddSeparator.apply(outputDirectory) + testClass + "_modification_report.json";
        try (FileWriter writer = new FileWriter(reportPathName, false)) {
            writer.write(allReports);
            LOGGER.info("Writing report in {}", reportPathName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reportModification(String testNameBeforeModification, String testNameAfterModification,
                                    AmplifierReport report) {

        List<AmplifierReport> newModificationReports = reportsPerMethod.computeIfAbsent(testNameAfterModification
                , s -> new ArrayList<>(1));

        // For each modification we also change the name.
        // We take over the previously reported modifications to the new method name
        // (but don't delete it, there could be more new test cases based on the old one)
        if (!testNameAfterModification.equals(testNameBeforeModification)) {
            List<AmplifierReport> oldModificationReports = reportsPerMethod.get(testNameBeforeModification);
            if (oldModificationReports != null) {
                newModificationReports.addAll(oldModificationReports);
            }
        }

        newModificationReports.add(report);
    }

    /**
     * Filter the report to only keep modifications made to the test cases given in
     * @param selectedTests
     */
    public void filterModifications(List<CtMethod<?>> selectedTests) {
        Set<String> methodNames = selectedTests.stream().map(CtMethod::getSimpleName).collect(Collectors.toSet());
        reportsPerMethod = reportsPerMethod.entrySet().stream()
                .filter(entry -> methodNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<AmplifierReport> getModificationsForTest(CtMethod<?> testMethod) {
        List<AmplifierReport> amplifierReports = reportsPerMethod.get(testMethod.getSimpleName());
        if (amplifierReports == null) {
            LOGGER.error("No modification report found for test " + testMethod.getSimpleName());
            return Collections.emptyList();
        }
        return amplifierReports;
    }
}
