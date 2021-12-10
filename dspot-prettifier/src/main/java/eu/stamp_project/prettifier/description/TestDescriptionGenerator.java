package eu.stamp_project.prettifier.description;

import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.report.output.AmplifierReport;
import eu.stamp_project.dspot.common.report.output.ClassModificationReport;
import eu.stamp_project.dspot.common.report.output.ModificationReport;
import eu.stamp_project.dspot.common.report.output.amplifiers.AddLocalVariableAmplifierReport;
import eu.stamp_project.dspot.common.report.output.assertiongenerator.ValueAssertionReport;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestCaseJSON;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;
import eu.stamp_project.prettifier.Main;
import eu.stamp_project.prettifier.Prettifier;
import eu.stamp_project.prettifier.Util;
import eu.stamp_project.prettifier.configuration.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtMethod;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generates a textual description of the contribution that the passed test cases make.
 * The description is added as a javadoc comment and saved in the report.
 */
public class TestDescriptionGenerator implements Prettifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDescriptionGenerator.class);

    UserInput configuration;

    public TestDescriptionGenerator(UserInput configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<CtMethod<?>> prettify(List<CtMethod<?>> amplifiedTestsToBePrettified) {
        List<CtMethod<?>> prettifiedTests = new ArrayList<>();

        boolean coverageReportPresent = Main.report.isExtendedCoverageReportPresent(this.getClass().getSimpleName());
        if (!coverageReportPresent) {
            return amplifiedTestsToBePrettified;
        }
        TestClassJSON amplificationReport = (TestClassJSON) Main.report.amplificationReport;
        Map<String, TestCaseJSON> mapTestNameToResult = amplificationReport.mapTestNameToResult();

        boolean modificationReportPresent = Main.report.isModificationReportPresent(this.getClass().getSimpleName());
        if (!modificationReportPresent) {
            return amplifiedTestsToBePrettified;
        }
        ClassModificationReport modificationReport = Main.report.modificationReport;

        for (CtMethod<?> test : amplifiedTestsToBePrettified) {
            StringBuilder description = new StringBuilder("Test that ");

            addAssertionText(description, test, modificationReport);
            addChangeText(description, test, modificationReport);
            addCoverageText(description, test, mapTestNameToResult);
            addOriginalTestText(description, test, mapTestNameToResult);

            String testDescription = description.toString();
            DSpotUtils.addComment(test, testDescription, CtComment.CommentType.JAVADOC, CommentEnum.All);
            amplificationReport.updateTestCase(mapTestNameToResult.get(test.getSimpleName()),
                    mapTestNameToResult.get(test.getSimpleName()).copyAndUpdateDescription(testDescription));
            prettifiedTests.add(test);
        }

        return prettifiedTests;
    }

    private void addAssertionText(StringBuilder description, CtMethod<?> test, ClassModificationReport modificationReport) {
        ValueAssertionReport assertionReport = null;
        AddLocalVariableAmplifierReport localVariableReport = null;
        for (AmplifierReport amplifierReport : modificationReport.getModificationsForTest(test)) {
            if (!amplifierReport.isAssertionReport()) {
                continue;
            }
            if (amplifierReport.getReportType().equals(ValueAssertionReport.class.getCanonicalName())) {
                assertionReport = (ValueAssertionReport) amplifierReport;
            }
            if (amplifierReport.getReportType().equals(AddLocalVariableAmplifierReport.class.getCanonicalName())) {
                localVariableReport = (AddLocalVariableAmplifierReport) amplifierReport;
            }
        }
        if (assertionReport == null || localVariableReport == null) {
            return;
        }
        if (!assertionReport.getTestedValue().equals(localVariableReport.getVariableName())) {
            // TODO does this happen with casts?
            LOGGER.error("Asserted value and corresponding local variable do not match!!!");
        }

        // TODO if no local variable, use tested value of assertion report directly
        description.append(localVariableReport.getVariableValue());

        // TODO adapt to assert method
        description.append(" is ");

        description.append(assertionReport.getExpectedValue());
    }

    private void addChangeText(StringBuilder description, CtMethod<?> test, ClassModificationReport modificationReport) {
        description.append(" when ");

        for (AmplifierReport amplifierReport : modificationReport.getModificationsForTest(test)) {
            if (amplifierReport.isAssertionReport()) {
                continue;
            }
            if (amplifierReport.getReportType().equals(AddLocalVariableAmplifierReport.class.getCanonicalName())) {
                AddLocalVariableAmplifierReport localVariableAmplifierReport = (AddLocalVariableAmplifierReport) amplifierReport;
                description.append(localVariableAmplifierReport.getVariableName())
                        .append(" is ")
                        .append(localVariableAmplifierReport.getVariableValue());
                description.append(" and ");
            }
        }
        replaceLastAnd(description);
    }

    private void addCoverageText(StringBuilder description, CtMethod<?> test,
                                 Map<String, TestCaseJSON> mapTestNameToResult) {
        description.append(" This test tests the methods ");
        Map<CtMethod<?>, List<String>> testToCoveredMethods = new HashMap<>();
        TestCaseJSON testCaseJSON = mapTestNameToResult.get(test.getSimpleName());
        testToCoveredMethods.put(test, Util.getCoveredMethods(testCaseJSON.getCoverageImprovement()));
        for (String methodName : testToCoveredMethods.get(test)) {
            description.append(methodName);
            description.append(" and ");
        }
        replaceLastAnd(description);
    }

    private void addOriginalTestText(StringBuilder description, CtMethod<?> test, Map<String, TestCaseJSON> mapTestNameToResult) {
        description.append(" It is based on ").append("<TODO pass original test name>").append(".");
    }

    /**
     * replace last " and " by "."
     *
     * @param description
     */
    private void replaceLastAnd(StringBuilder description) {
        description.replace(description.length() - 5, description.length(), ".");
    }
}
