package eu.stamp_project.dspot;

import eu.stamp_project.dspot.common.configuration.AmplificationSetup;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.TestTuple;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationException;
import eu.stamp_project.dspot.common.report.GlobalReport;
import eu.stamp_project.dspot.common.report.error.Error;
import org.slf4j.Logger;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static eu.stamp_project.dspot.common.report.error.ErrorEnum.ERROR_ASSERT_AMPLIFICATION;
import static eu.stamp_project.dspot.common.report.error.ErrorEnum.ERROR_INPUT_AMPLIFICATION;

public class DevFriendlyAmplification {

    private final DSpot dSpot;
    private final DSpotState dSpotState;
    private final AmplificationSetup setup;
    private final Logger LOGGER;
    private final GlobalReport GLOBAL_REPORT;

    public DevFriendlyAmplification(DSpot dSpot, DSpotState dSpotState, AmplificationSetup setup, Logger LOGGER,
                                    GlobalReport GLOBAL_REPORT) {
        this.dSpot = dSpot;
        this.dSpotState = dSpotState;
        this.setup = setup;
        this.LOGGER = LOGGER;
        this.GLOBAL_REPORT = GLOBAL_REPORT;
    }

    /**
     * Amplifies the test cases in a way suitable to present the results to developers.
     *
     * @param testClassToBeAmplified   Test class to be amplified
     * @param testMethodsToBeAmplified Test methods to be amplified
     * @return Amplified test methods
     */
    public List<CtMethod<?>> devFriendlyAmplification(CtType<?> testClassToBeAmplified,
                                                 List<CtMethod<?>> testMethodsToBeAmplified) {

        final List<CtMethod<?>> selectedToBeAmplified = dSpot.setupSelector(testClassToBeAmplified,
                testMethodsToBeAmplified);

        final List<CtMethod<?>> amplifiedTestMethodsToKeep = new ArrayList<>();
        amplifiedTestMethodsToKeep.addAll(ampRemoveAssertionsAddNewOnes(testClassToBeAmplified,
                selectedToBeAmplified));
        amplifiedTestMethodsToKeep.addAll(inputAmplification(testClassToBeAmplified,selectedToBeAmplified));
        return amplifiedTestMethodsToKeep;
    }

    public List<CtMethod<?>> ampRemoveAssertionsAddNewOnes(CtType<?> testClassToBeAmplified,
                                                           List<CtMethod<?>> testMethodsToBeAmplified) {
        final List<CtMethod<?>> amplifiedTests;
        final CtType<?> classWithTestMethods;
        try {
            TestTuple testTuple;
            // Remove old assertions
            testTuple = dSpotState.getAssertionGenerator().removeAssertions(testClassToBeAmplified, testMethodsToBeAmplified);

            // Add new assertions
            amplifiedTests = dSpotState.getAssertionGenerator().assertionAmplification(testTuple.testClassToBeAmplified, testTuple.testMethodsToBeAmplified);
            classWithTestMethods = testTuple.testClassToBeAmplified;
        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        final List<CtMethod<?>> amplifiedPassingTests =
                dSpotState.getTestCompiler().compileRunAndDiscardUncompilableAndFailingTestMethods(
                        classWithTestMethods,
                        amplifiedTests,
                        dSpotState.getCompiler()
                );

        // 4. Keep tests that improve the test suite
        final List<CtMethod<?>> improvingTests = new ArrayList<>();
        try {
            dSpot.selectImprovingTestCases(amplifiedPassingTests, improvingTests);
        } catch (AmplificationException e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        LOGGER.info("Dev friendly amplification, path 1: {} test method(s) have been successfully amplified.",
                improvingTests.size());
        return improvingTests;
    }

    public List<CtMethod<?>> inputAmplification(CtType<?> testClassToBeAmplified,
                                                List<CtMethod<?>> testMethodsToBeAmplified) {
        final List<CtMethod<?>> amplifiedTests;
        final CtType<?> classWithTestMethods;
        try {
            TestTuple testTuple;
            // Remove old assertions
            testTuple = dSpotState.getAssertionGenerator().removeAssertions(testClassToBeAmplified, testMethodsToBeAmplified);
            classWithTestMethods = testTuple.testClassToBeAmplified;

            // Amplify input
            List<CtMethod<?>> selectedForInputAmplification = setup.fullSelectorSetup(classWithTestMethods,
                    testTuple.testMethodsToBeAmplified);

            // amplify tests and shrink amplified set with inputAmplDistributor
            List<CtMethod<?>> inputAmplifiedTests =
                    dSpotState.getInputAmplDistributor().inputAmplify(selectedForInputAmplification, 0);

            // Add new assertions
            // TODO how can these assertions be related to the amplified input???
            // TODO: keep 'parent' test case and also observe it's values: assert values that are different in parent
            amplifiedTests = dSpotState.getAssertionGenerator().assertionAmplification(classWithTestMethods, inputAmplifiedTests);

        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        if (amplifiedTests.isEmpty()) {
            return Collections.emptyList();
        }
        final List<CtMethod<?>> amplifiedPassingTests =
                dSpotState.getTestCompiler().compileRunAndDiscardUncompilableAndFailingTestMethods(
                        classWithTestMethods,
                        amplifiedTests,
                        dSpotState.getCompiler()
                );

        // 4. Keep tests that improve the test suite
        final List<CtMethod<?>> improvingTests = new ArrayList<>();
        try {
            dSpot.selectImprovingTestCases(amplifiedPassingTests, improvingTests);
        } catch (AmplificationException e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        LOGGER.info("Dev friendly amplification, path 1: {} test method(s) have been successfully amplified.",
                improvingTests.size());
        return improvingTests;
    }

}
