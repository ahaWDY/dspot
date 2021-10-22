package eu.stamp_project.dspot.assertiongenerator.assertiongenerator;

import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.dspot.common.miscellaneous.CloneHelper;
import eu.stamp_project.dspot.common.miscellaneous.Counter;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/05/18
 */
public class TryCatchFailGenerator {

    private int numberOfFail;

    public TryCatchFailGenerator() {
        this.numberOfFail = 0;
    }

    private final static List<String> UNSUPPORTED_EXCEPTION = Arrays.asList(
            "junit.framework.ComparisonFailure",
            "org.junit.runners.model.TestTimedOutException",
            "java.lang.OutOfMemoryError",
            "java.lang.StackOverflowError",
            "java.lang.AssertionError",
            "org.opentest4j.AssertionFailedError"
    );

    /**
     * Adds surrounding try/catch/fail in a failing test.
     *
     * @param test    Failing test method to amplify
     * @param failure Test's failure description
     * @return New amplified test
     */
    @SuppressWarnings("unchecked")
    public CtMethod<?> surroundWithTryCatchFail(CtMethod<?> test, Failure failure) {
        // TODO caro: give on original test class (that ran into failure)
        // TODO caro: check before if the test case given into this method has a defining class
        // TODO caro: maybe pass that information in the failure? or figure out the problematic line here already?
        // TODO caro: and pass on which number of the statement should be eliminated?
        CtMethod cloneMethodTest = CloneHelper.cloneTestMethodForAmp(test, "");
        cloneMethodTest.setSimpleName(test.getSimpleName());
        // TestTimedOutException means infinite loop
        // AssertionError means that some assertion remained in the test: TODO
        if (UNSUPPORTED_EXCEPTION.contains(failure.fullQualifiedNameOfException)) {
            return null;
        }

        final int lineThatThrewExecption = findLineInTestThatThrewException(test, failure);
        int lineToSurround = lineThatThrewExecption;
        if (lineThatThrewExecption != -1) {
            List<CtStatement> throwingStatements =
                    test.getBody().getStatements().stream()
                        .filter(ctStatement -> ctStatement.getPosition().getLine() == lineThatThrewExecption)
                        .collect(Collectors.toList());
            if (throwingStatements.size() == 1) {
                lineToSurround = test.getBody().getStatements().indexOf(throwingStatements.get(0));
            }
        }

        // TODO caro: check if numberOfFail is ever not zero
        cloneMethodTest = TestFramework.get().generateExpectedExceptionsBlock(cloneMethodTest, failure,
                this.numberOfFail, lineToSurround);
        Counter.updateAssertionOf(cloneMethodTest, 1);
        return cloneMethodTest;
    }

    private int findLineInTestThatThrewException(CtMethod<?> test, Failure failure) {
        String[] testNameComponents = failure.testClassName.split("\\.");
        String testClassSimpleName = testNameComponents[testNameComponents.length - 1];
        String regex =
                failure.testCaseName.replace("#",".").replace(".", "\\.")
                + "\\(" + testClassSimpleName + "\\.java:(.*?)\\)\n";
        Pattern find_test_line = Pattern.compile(regex);
        Matcher m = find_test_line.matcher(failure.stackTrace);
        while (m.find()) {
            String s = m.group(1);
            System.out.println(s);
            return Integer.parseInt(s);
        }
        return -1;
    }

}
