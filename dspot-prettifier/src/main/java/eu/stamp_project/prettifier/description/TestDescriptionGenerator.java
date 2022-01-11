package eu.stamp_project.prettifier.description;

import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.report.output.AmplifierReport;
import eu.stamp_project.dspot.common.report.output.ClassModificationReport;
import eu.stamp_project.dspot.common.report.output.amplifiers.*;
import eu.stamp_project.dspot.common.report.output.assertiongenerator.ExceptionAssertionReport;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * Generates a textual description of the contribution that the passed test cases make.
 * The description is added as a javadoc comment and saved in the report.
 */
public class TestDescriptionGenerator implements Prettifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDescriptionGenerator.class);

    final UserInput configuration;

    /**
     * Maps the variables newly defined by DSpot in the test case to their values.
     * As (most? all?) are inlined by the general minimizer, the names can be generally replaced by the value in the
     * descriptions.
     * Is cleared before describing a new test case.
     */
    private final Map<String, String> variableValues;

    public TestDescriptionGenerator(UserInput configuration) {
        this.configuration = configuration;
        variableValues = new HashMap<>();
    }

    @Override
    public List<CtMethod<?>> prettify(List<CtMethod<?>> amplifiedTestsToBePrettified) {
        List<CtMethod<?>> prettifiedTests = new ArrayList<>();

        boolean coverageReportPresent = Main.report.isExtendedCoverageReportPresent(this.getClass().getSimpleName());
        if (!coverageReportPresent) {
            return amplifiedTestsToBePrettified;
        }
        TestClassJSON amplificationReport = Main.report.extendedCoverageReport;
        Map<String, TestCaseJSON> mapTestNameToResult = amplificationReport.mapTestNameToResult();

        boolean modificationReportPresent = Main.report.isModificationReportPresent(this.getClass().getSimpleName());
        if (!modificationReportPresent) {
            return amplifiedTestsToBePrettified;
        }
        ClassModificationReport modificationReport = Main.report.modificationReport;

        for (CtMethod<?> test : amplifiedTestsToBePrettified) {
            prettifiedTests.add(prettify(test, modificationReport.getModificationsForTest(test),
                    mapTestNameToResult.get(test.getSimpleName())));
        }

        return prettifiedTests;
    }

    private CtMethod<?> prettify(CtMethod<?> amplifiedTest, List<AmplifierReport> modificationsForTest,
                                 TestCaseJSON testCaseResult) {
        variableValues.clear();
        // Map modifications per type
        Map<String, List<AmplifierReport>> modifications =
                modificationsForTest.stream().collect(Collectors.groupingBy(AmplifierReport::getReportType));

        for (AmplifierReport localVariableReport :
                modifications.getOrDefault(AddLocalVariableAmplifierReport.class.getCanonicalName(), emptyList())) {
            rememberLocalVariable((AddLocalVariableAmplifierReport) localVariableReport);
        }

        StringBuilder description = new StringBuilder("Test that ");

        // Test that
        // x is y
        addAssertionText(description, amplifiedTest, modifications);
        // , when a=3 and b=5 .
        addChangeText(description, amplifiedTest, modifications);
        // This tests the methods ...
        addCoverageText(description, amplifiedTest, testCaseResult);
        // The test is based on ...
        addOriginalTestText(description, amplifiedTest, testCaseResult);

        String testDescription = description.toString();
        DSpotUtils.addComment(amplifiedTest, testDescription, CtComment.CommentType.JAVADOC, CommentEnum.All);
        Main.report.extendedCoverageReport.updateTestCase(testCaseResult,
                testCaseResult.copyAndUpdateDescription(testDescription));
        return amplifiedTest;
    }

    /**
     * Adds a text for the assertion in the form of "testedExpression is expectedValue"
     *
     * @param description   the string builder holding the description
     * @param test          the test to be described
     * @param modifications the modifications made to the test during the amplification
     */
    private void addAssertionText(StringBuilder description, CtMethod<?> test, Map<String, List<AmplifierReport>> modifications) {
        List<AmplifierReport> valueAssertionReports =
                modifications.getOrDefault(ValueAssertionReport.class.getCanonicalName(), emptyList());

        if (!valueAssertionReports.isEmpty()) {
            for (AmplifierReport report : valueAssertionReports) {
                ValueAssertionReport valueAssertionReport = (ValueAssertionReport) report;

                description.append(replaceLocalVariableIfPresent(valueAssertionReport.getTestedValue()));
                // TODO adapt to assert method
                description.append(" is ");
                description.append(valueAssertionReport.getExpectedValue());
                description.append(" and ");
            }
            replaceEndIfThere(description, " and ", "");
        } else {
            List<AmplifierReport> exceptionAssertionReports =
                    modifications.getOrDefault(ExceptionAssertionReport.class.getCanonicalName(), emptyList());

            if (!exceptionAssertionReports.isEmpty()) {
                for (AmplifierReport report : exceptionAssertionReports) {
                    ExceptionAssertionReport exceptionAssertionReport = (ExceptionAssertionReport) report;
                    description.append("a ");
                    description.append(exceptionAssertionReport.getExceptionName());
                    description.append(" and ");
                }
                replaceEndIfThere(description, " and ", "");
                description.append(" is thrown");
            } else {
                LOGGER.warn("Tried to generate description for test without any reported assertion modifications, no " +
                        "assertion text added.");
            }
        }
    }

    private void addChangeText(StringBuilder description, CtMethod<?> test, Map<String, List<AmplifierReport>> modifications) {
        description.append(" when ");

        addLiteralChangedText(description, modifications.getOrDefault(LiteralAmplifierReport.class.getCanonicalName(),
                emptyList()));

        List<AmplifierReport> methodAdderReports =
                modifications.getOrDefault(MethodDuplicationAmplifierReport.class.getCanonicalName(), new ArrayList<>());
        methodAdderReports.addAll(modifications.getOrDefault(MethodAdderOnExistingObjectsAmplifierReport.class.getCanonicalName(), emptyList()));
        addMethodCalledOrDuplicationText(description, methodAdderReports);

        addMethodCallRemovedText(description,
                modifications.getOrDefault(MethodRemoveAmplifierReport.class.getCanonicalName(), emptyList()));
        addLocalVariableText(description,
                modifications.getOrDefault(AddLocalVariableAmplifierReport.class.getCanonicalName(), emptyList()));

        replaceEndByPeriodIfThere(description, " when ");
    }

    /**
     * reports for {@link AddLocalVariableAmplifierReport}
     */
    private void addLocalVariableText(StringBuilder description, List<AmplifierReport> modifications) {
        for (AmplifierReport amplifierReport : modifications) {
            if (amplifierReport.isAssertionReport()) {
                // local variable used in assertion, so it is not mentioned when describing the change.
                continue;
            }
            if (amplifierReport.getReportType().equals(AddLocalVariableAmplifierReport.class.getCanonicalName())) {
                AddLocalVariableAmplifierReport localVariableAmplifierReport = (AddLocalVariableAmplifierReport) amplifierReport;
                if (description.indexOf(localVariableAmplifierReport.getVariableName()) != -1) {
                    // variable is used in description until now, so we should report its value!
                    description.append(localVariableAmplifierReport.getVariableName());
                    description.append(" is ");
                    description.append(replaceLocalVariableIfPresent(localVariableAmplifierReport.getVariableValue()));
                    description.append(" and ");
                }
            }
        }
        replaceEndByPeriodIfThere(description, " and ");
    }

    /**
     * reports for {@link LiteralAmplifierReport}
     */
    private void addLiteralChangedText(StringBuilder description, List<AmplifierReport> modifications) {
        for (AmplifierReport amplifierReport : modifications) {
            if (amplifierReport.getReportType().equals(LiteralAmplifierReport.class.getCanonicalName())) {
                LiteralAmplifierReport literalAmplifierReport = (LiteralAmplifierReport) amplifierReport;
                if (literalAmplifierReport.isLocalVariable()) {
                    description.append(literalAmplifierReport.getVariableName());
                    description.append(" is ");
                    description.append(replaceLocalVariableIfPresent(literalAmplifierReport.getNewValue()));
                } else {
                    // new literal is method call parameter
                    description.append("the parameter ");
                    description.append(literalAmplifierReport.getVariableName());
                    description.append(" of the method ");
                    description.append(literalAmplifierReport.getMethodName());
                    description.append(" is set to ");
                    description.append(replaceLocalVariableIfPresent(literalAmplifierReport.getNewValue()));
                }
                description.append(" and ");
            }
        }
        replaceEndByPeriodIfThere(description, " and ");
    }

    /**
     * reports for {@link MethodDuplicationAmplifierReport} and {@link MethodAdderOnExistingObjectsAmplifierReport}
     */
    private void addMethodCalledOrDuplicationText(StringBuilder description, List<AmplifierReport> modifications) {
        for (AmplifierReport amplifierReport : modifications) {
            if (amplifierReport.getReportType().equals(MethodDuplicationAmplifierReport.class.getCanonicalName())) {
                MethodDuplicationAmplifierReport methodDuplicationAmplifierReport = (MethodDuplicationAmplifierReport) amplifierReport;
                description.append(methodDuplicationAmplifierReport.getDuplicatedCall());
                description.append(" is called");
            } else if (amplifierReport.getReportType().equals(MethodAdderOnExistingObjectsAmplifierReport.class.getCanonicalName())) {
                MethodAdderOnExistingObjectsAmplifierReport methodAdderOnExistingObjectsAmplifierReport =
                        (MethodAdderOnExistingObjectsAmplifierReport) amplifierReport;
                description.append(methodAdderOnExistingObjectsAmplifierReport.getInvokedMethod().getName());
                description.append(" is called with the parameters ");
                for (MethodAdderOnExistingObjectsAmplifierReport.MethodParameter parameter : methodAdderOnExistingObjectsAmplifierReport.getInvokedMethod().getParameters()) {
                    description.append(parameter.getName());
                    description.append(" = ");
                    description.append(replaceLocalVariableIfPresent(parameter.getValue()));
                    description.append(" and ");
                }
                replaceEndIfThere(description, " and ", "");
            }
            description.append(" and ");
        }
        replaceEndByPeriodIfThere(description, " and ");
    }

    /**
     * reports for {@link MethodRemoveAmplifierReport}
     */
    private void addMethodCallRemovedText(StringBuilder description, List<AmplifierReport> modifications) {
        for (AmplifierReport amplifierReport : modifications) {
            if (amplifierReport.getReportType().equals(MethodRemoveAmplifierReport.class.getCanonicalName())) {
                MethodRemoveAmplifierReport methodRemoveAmplifierReport = (MethodRemoveAmplifierReport) amplifierReport;
                description.append(methodRemoveAmplifierReport.getRemovedCall());
                description.append(" is not called");
            }
            description.append(" and ");
        }
        replaceEndByPeriodIfThere(description, " and ");
    }


    private void addCoverageText(StringBuilder description, CtMethod<?> test,
                                 TestCaseJSON testCaseResult) {
        description.append(" This tests the methods ");
        Map<CtMethod<?>, List<String>> testToCoveredMethods = new HashMap<>();
        testToCoveredMethods.put(test, Util.getCoveredMethods(testCaseResult.getCoverageImprovement()));
        for (String methodName : testToCoveredMethods.get(test)) {
            description.append(methodName);
            description.append(" and ");
        }
        replaceEndByPeriodIfThere(description, " and ");
    }

    private void addOriginalTestText(StringBuilder description, CtMethod<?> test, TestCaseJSON testCaseResult) {
        description.append(" The test is based on ").append(testCaseResult.getNameOfBaseTestCase()).append(".");
    }

    private void replaceEndIfThere(StringBuilder description, String textToReplace, String replacement) {
        if (description.subSequence(description.length() - textToReplace.length(), description.length()).equals(textToReplace)) {
            description.replace(description.length() - textToReplace.length(), description.length(), replacement);
        }
    }

    /**
     * replace trailing textToReplace by "."
     */
    private void replaceEndByPeriodIfThere(StringBuilder description, String textToReplace) {
        replaceEndIfThere(description, textToReplace, ".");
    }

    /**
     * Remembers name & value of a created local variable to use in the description later.
     *
     * @param report
     */
    private void rememberLocalVariable(AddLocalVariableAmplifierReport report) {
        variableValues.put(report.getVariableName(), report.getVariableValue());
    }

    /**
     * Replaces any remembered local variables with their value.
     * This is a plain string.replace, no actual parsing of code takes place.
     */
    private String replaceLocalVariableIfPresent(String codeSnippet) {
        for (String variableName : variableValues.keySet()) {
            if (codeSnippet.contains(variableName)) {
                codeSnippet = codeSnippet.replace(variableName, variableValues.get(variableName));
            }
        }
        return codeSnippet;
    }
}
