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
        return amplifiedTestMethodsToKeep;
    }

    public List<CtMethod<?>> ampRemoveAssertionsAddNewOnes(CtType<?> testClassToBeAmplified,
                                                      List<CtMethod<?>> testMethodsToBeAmplified) {
        final List<CtMethod<?>> amplifiedTests;
        final CtType<?> classWithTestMethods;
        try {
            TestTuple testTuple;
            // 1. Remove old assertions
            testTuple = dSpotState.getAssertionGenerator().removeAssertions(testClassToBeAmplified, testMethodsToBeAmplified);
            // 2. Add new assertions
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

    /**
     * TODO full process replicating the old one
     *
     * @param testClassToBeAmplified   Test class to be amplified
     * @param testMethodsToBeAmplified Test methods to be amplified
     * @return Amplified test methods
     */
    public List<CtMethod<?>> oldFullDevFriendlyAmp(CtType<?> testClassToBeAmplified,
                                                      List<CtMethod<?>> testMethodsToBeAmplified) {

        List<CtMethod<?>> amplifiedTestMethodsToKeep = dSpot.setupSelector(testClassToBeAmplified,
                testMethodsToBeAmplified);


        final List<CtMethod<?>> testsWithoutAssertions;
        final CtType<?> classWithTestMethods;
        // 1. Remove old assertions
        try {
            TestTuple testTuple;
            testTuple = dSpotState.getAssertionGenerator().removeAssertions(testClassToBeAmplified, amplifiedTestMethodsToKeep);
            classWithTestMethods = testTuple.testClassToBeAmplified;
            testsWithoutAssertions = testTuple.testMethodsToBeAmplified;
        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        // 2. Amplify input
        final List<CtMethod<?>> selectedToBeAmplified;
        List<CtMethod<?>> inputAmplifiedTests;
        try {
            selectedToBeAmplified = setup.fullSelectorSetup(classWithTestMethods, testsWithoutAssertions);

            // amplify tests and shrink amplified set with inputAmplDistributor
            inputAmplifiedTests = dSpotState.getInputAmplDistributor().inputAmplify(selectedToBeAmplified, 0);

        } catch (AmplificationException e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_INPUT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        // 3. Add new assertions
        final List<CtMethod<?>> testsWithAssertions = dSpotState.getAssertionGenerator().assertionAmplification(classWithTestMethods, inputAmplifiedTests);
        if (testsWithAssertions.isEmpty()) {
            return testsWithAssertions;
        }

        final List<CtMethod<?>> amplifiedPassingTests =
                dSpotState.getTestCompiler().compileRunAndDiscardUncompilableAndFailingTestMethods(
                        classWithTestMethods,
                        testsWithAssertions,
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

        LOGGER.info("Dev friendly amplification: {} test method(s) have been successfully amplified.", improvingTests.size());
        return improvingTests;
    }
}
