package eu.stamp_project.dspot;

import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.TestTuple;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationException;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.report.GlobalReport;
import eu.stamp_project.dspot.common.report.error.Error;
import eu.stamp_project.dspot.selector.branchcoverageselector.Coverage;
import eu.stamp_project.dspot.selector.branchcoverageselector.clover.CloverExecutor;
import eu.stamp_project.dspot.selector.branchcoverageselector.clover.CloverReader;
import org.slf4j.Logger;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static eu.stamp_project.dspot.common.report.error.ErrorEnum.ERROR_ASSERT_AMPLIFICATION;

//import eu.stamp_project.diff_test_selection.clover.*;

public class DevFriendlyAmplification {

    private final DSpot dSpot;
    private final DSpotState dSpotState;
    private final Logger LOGGER;
    private final GlobalReport GLOBAL_REPORT;

    public DevFriendlyAmplification(DSpot dSpot, DSpotState dSpotState, Logger LOGGER,
                                    GlobalReport GLOBAL_REPORT) {
        this.dSpot = dSpot;
        this.dSpotState = dSpotState;
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
                                                      List<CtMethod<?>> testMethodsToBeAmplified) throws IOException {

        // first we setup the selector so it can compute the complete coverage of the whole existing test suite
        final List<CtMethod<?>> selectedToBeAmplified = dSpot
                .setupSelector(testClassToBeAmplified, testMethodsToBeAmplified);

        // selectedToBeAmplified with all test class methods -> keep only ones matching testMethodsToBeAmplified
        final List<CtMethod<?>> methodsToAmplify =
                selectedToBeAmplified.stream().filter(testMethodsToBeAmplified::contains).collect(Collectors.toList());

        final List<CtMethod<?>> amplifiedTestMethodsToKeep = new ArrayList<>();
        if(dSpotState.getTargetMethod().equals("")) {
            amplifiedTestMethodsToKeep.addAll(ampRemoveAssertionsAddNewOnes(testClassToBeAmplified, methodsToAmplify));
            amplifiedTestMethodsToKeep.addAll(inputAmplification(testClassToBeAmplified, methodsToAmplify));
        }
        else if (!dSpotState.getTargetBranch().equals("")){
            amplifiedTestMethodsToKeep.addAll(targetMethodAmplification(testClassToBeAmplified, methodsToAmplify));
        }
        return amplifiedTestMethodsToKeep;
    }

    /**
     * Path 1 of dev-friendly amplification: remove old assertions (if at the end of test case: completely, also
     * invocations inside assertions are removed) and then add single new assertions.
     * @param testClassToBeAmplified original test class
     * @param testMethodsToBeAmplified original test methods
     * @return amplified test methods
     */
    public List<CtMethod<?>> ampRemoveAssertionsAddNewOnes(CtType<?> testClassToBeAmplified,
                                                           List<CtMethod<?>> testMethodsToBeAmplified) {
        final List<CtMethod<?>> amplifiedTests;
        final CtType<?> classWithTestMethods;
        try {
            TestTuple testTuple;
            // Remove old assertions
            testTuple = dSpotState.getAssertionGenerator()
                    .removeAssertions(testClassToBeAmplified, testMethodsToBeAmplified);

            // Add new assertions
            amplifiedTests = dSpotState.getAssertionGenerator()
                    .assertionAmplification(testTuple.testClassToBeAmplified, testTuple.testMethodsToBeAmplified);
            classWithTestMethods = testTuple.testClassToBeAmplified;
        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        return selectPassingAndImprovingTests(amplifiedTests,classWithTestMethods,1);
    }

    /**
     * Path 2 of dev-friendly amplification: remove old assertions, amplify inputs and then add new assertions.
     * The new assertions assert values that changed through the input amplification.
     * @param testClassToBeAmplified original test class
     * @param testMethodsToBeAmplified original test methods
     * @return amplified test methods
     */
    public List<CtMethod<?>> inputAmplification(CtType<?> testClassToBeAmplified,
                                                List<CtMethod<?>> testMethodsToBeAmplified) {
        final List<CtMethod<?>> amplifiedTests;
        final CtType<?> classWithTestMethods;
        try {
            TestTuple testTuple;
            // Remove old assertions
            testTuple = dSpotState.getAssertionGenerator()
                    .removeAssertions(testClassToBeAmplified, testMethodsToBeAmplified);
            classWithTestMethods = testTuple.testClassToBeAmplified;

            // Amplify input
            List<CtMethod<?>> inputAmplifiedTests = dSpotState.getInputAmplDistributor()
                    .inputAmplify(testTuple.testMethodsToBeAmplified, 0);

            // Add new assertions
            amplifiedTests = dSpotState.getAssertionGenerator()
                    .assertionAmplification(classWithTestMethods, inputAmplifiedTests);

        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }
        if (amplifiedTests.size() >= 1000) {
            // executing too many tests over the command line fails because the argument list is too long
            // that is why we split in smaller chunks that we execute separately

            List<CtMethod<?>> accumulateSelectedTests = new ArrayList<>();
            int rounds = amplifiedTests.size() % 1000 + 1;
            LOGGER.info("Too many tests to run at once. Dividing {} tests into {} rounds", amplifiedTests.size(),
                    rounds);
            for (int i = 0; i <= rounds; i++) {
                List<CtMethod<?>> roundTests = amplifiedTests.subList(i, Math.min((i + 1) * 1000,
                        amplifiedTests.size()));
                accumulateSelectedTests.addAll(selectPassingAndImprovingTests(roundTests, classWithTestMethods, 2));
            }
            return accumulateSelectedTests;

        } else {
            return selectPassingAndImprovingTests(amplifiedTests,classWithTestMethods,2);
        }
    }

    public List<CtMethod<?>> targetMethodAmplification(CtType<?> testClassToBeAmplified,
                                                       List<CtMethod<?>> testMethodsToBeAmplified) throws IOException {
        final List<CtMethod<?>> amplifiedTests;
        final CtType<?> classWithTestMethods;

        try {
            TestTuple testTuple;
            // Remove old assertions
            testTuple = dSpotState.getAssertionGenerator()
                    .removeAssertions(testClassToBeAmplified, testMethodsToBeAmplified);
            classWithTestMethods = testTuple.testClassToBeAmplified;
            List<CtMethod<?>> selectedForInputAmplification = testTuple.testMethodsToBeAmplified;

            // amplify
            List<CtMethod<?>> inputAmplifiedTests = dSpotState.getInputAmplDistributor()
                    .inputAmplify(selectedForInputAmplification, 0, dSpotState.getTargetMethod());

            // Add new assertions
            List<CtMethod<?>> amplifiedTestsWithAssertions = dSpotState.getAssertionGenerator()
                    .assertionAmplification(classWithTestMethods, inputAmplifiedTests);

            amplifiedTests = limit(amplifiedTestsWithAssertions, 200);
            if(amplifiedTests.isEmpty()){
                amplifiedTests.addAll(inputAmplifiedTests);
            }

        } catch (Exception | java.lang.Error e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        return selectPassingAndTargetTests(amplifiedTests,classWithTestMethods,3);
    }

    private List<CtMethod<?>> selectPassingAndImprovingTests(List<CtMethod<?>> amplifiedTests,
                                                             CtType<?> classWithTestMethods,
                                                             int path) {
        if (amplifiedTests.isEmpty()) {
            LOGGER.info("Dev friendly amplification, path {}: 0 test method(s) passed to improvement selection.", path);
            return Collections.emptyList();
        }
        final List<CtMethod<?>> amplifiedPassingTests = dSpotState.getTestCompiler()
                .compileRunAndDiscardUncompilableAndFailingTestMethods(classWithTestMethods, amplifiedTests, dSpotState
                        .getCompiler());

        // Keep tests that improve the test suite
        final List<CtMethod<?>> improvingTests = new ArrayList<>();
        try {
            dSpot.selectImprovingTestCases(amplifiedPassingTests, improvingTests);
        } catch (AmplificationException e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        LOGGER.info("Dev friendly amplification, path {}: {} test method(s) have been successfully amplified.",
                path, improvingTests.size());
        return improvingTests;
    }

    private List<CtMethod<?>> selectPassingTests(List<CtMethod<?>> amplifiedTests,
                                                 CtType<?> classWithTestMethods,
                                                 int path){
        if (amplifiedTests.isEmpty()) {
            return Collections.emptyList();
        }
        final List<CtMethod<?>> amplifiedPassingTests = dSpotState.getTestCompiler()
                .compileRunAndDiscardUncompilableAndFailingTestMethods(classWithTestMethods, amplifiedTests, dSpotState
                        .getCompiler());

        LOGGER.info("Dev friendly amplification, path {}: {} test method(s) have been successfully amplified.",
                path, amplifiedPassingTests.size());

        return amplifiedPassingTests;
    }

    private List<CtMethod<?>> selectPassingAndTargetTests(List<CtMethod<?>> amplifiedTests,
                                                          CtType<?> classWithTestMethods,
                                                          int path) throws IOException {
        if (amplifiedTests.isEmpty()) {
            return Collections.emptyList();
        }
        final List<CtMethod<?>> amplifiedPassingTests = dSpotState.getTestCompiler()
                .compileRunAndDiscardUncompilableAndFailingTestMethods(classWithTestMethods, amplifiedTests, dSpotState
                        .getCompiler());

        final List<CtMethod<?>> improvingTests = new ArrayList<>();
        try {
            dSpot.selectImprovingTestCases(amplifiedPassingTests, improvingTests);
        } catch (AmplificationException e) {
            GLOBAL_REPORT.addError(new Error(ERROR_ASSERT_AMPLIFICATION, e));
            return Collections.emptyList();
        }

        LOGGER.info("Dev friendly amplification, path {}: {} test method(s) have been successfully amplified.",
                path, improvingTests.size());
        return improvingTests;
    }

    public List<CtMethod<?>> limit(List<CtMethod<?>> tests, int maxNumTests) {
        final List<CtMethod<?>> reducedTests = new ArrayList<>();

        final int testsSize = tests.size();
        if (testsSize > maxNumTests) {
            Random random = new Random();
            LOGGER.warn("Too many tests have been generated: {}", testsSize);
            for (int i=0;i<maxNumTests; i++) {
                reducedTests.add(tests.get(random.nextInt(testsSize)));
            }
            LOGGER.info("Number of generated test reduced to {}", reducedTests.size());
        }
        if (reducedTests.isEmpty()) {
            reducedTests.addAll(tests);
        }
        return reducedTests;
    }

}
