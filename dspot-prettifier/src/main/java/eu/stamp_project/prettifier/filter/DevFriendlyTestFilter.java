package eu.stamp_project.prettifier.filter;

import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestCaseJSON;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;
import eu.stamp_project.prettifier.Main;
import eu.stamp_project.prettifier.Prettifier;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Filters test cases with the aim to only keep test cases that are valued by developers.
 * Requires test cases that were selected by the ExtendedCoverageSelector.
 *
 * Keeps:
 * - tests for an exception
 *
 * Discards:
 * - tests of simple getters or setters (name starts with get/set + only instruction improvement in first line)
 * - tests of hashCode (as they are simple value comparisons until now)
 */
public class DevFriendlyTestFilter implements Prettifier {

    @Override
    public List<CtMethod<?>> prettify(List<CtMethod<?>> amplifiedTestsToBePrettified) {

        boolean coverageReportPresent = Main.report.isExtendedCoverageReportPresent(this.getClass().getSimpleName());
        if (!coverageReportPresent) {
            return amplifiedTestsToBePrettified;
        }
        TestClassJSON amplificationReport = Main.report.extendedCoverageReport;
        Map<String, TestCaseJSON> mapTestNameToResult = amplificationReport.mapTestNameToResult();

        List<CtMethod<?>> prettifiedTests = new ArrayList<>(filterExceptionTests(amplifiedTestsToBePrettified));
        List<CtMethod<?>> excludedTestsForGettersSetters = filterSimpleGetterSetterTests(amplifiedTestsToBePrettified);
        List<CtMethod<?>> excludedTestsForHashCode = filterHashCodeTests(amplifiedTestsToBePrettified);

        // TODO report numbers of excluded tests?

        // remaining tests that passed all filters
        prettifiedTests.addAll(amplifiedTestsToBePrettified);

        return prettifiedTests;
    }

    /**
     * Detects all tests that check for an exception being thrown.
     * @param tests the tests to be filtered, the filtered tests are removed from this list.
     * @return all tests where the assertion checks for an exception.
     */
    private List<CtMethod<?>> filterExceptionTests(List<CtMethod<?>> tests) {
        return Collections.emptyList();
    }

    /**
     * Detects all tests that test a simple getter or setter.
     * @param tests the tests to be filtered, the filtered tests are removed from this list.
     * @return all tests that only increase coverage in the first and only line of a method named "get..." or "set..."
     */
    private List<CtMethod<?>> filterSimpleGetterSetterTests(List<CtMethod<?>> tests) {
        return Collections.emptyList();
    }

    /**
     * Detects all tests that test the hashCode method.
     * @param tests the tests to be filtered, the filtered tests are removed from this list.
     * @return all tests that only increase coverage a method named "hashCode".
     */
    private List<CtMethod<?>> filterHashCodeTests(List<CtMethod<?>> tests) {
        return Collections.emptyList();
    }

}
