package eu.stamp_project.prettifier.testnaming;

import eu.stamp_project.dspot.common.miscellaneous.AmplificationException;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestCaseJSON;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;
import eu.stamp_project.dspot.selector.extendedcoverageselector.CoverageImprovement;
import eu.stamp_project.dspot.selector.extendedcoverageselector.MethodCoverage;
import eu.stamp_project.prettifier.Prettifier;
import eu.stamp_project.prettifier.Util;
import eu.stamp_project.prettifier.configuration.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class ImprovedCoverageTestRenamer implements Prettifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImprovedCoverageTestRenamer.class);
    public static final String PREFIX = "test";
    public static final String STR_AND = "And";

    UserInput configuration;

    public ImprovedCoverageTestRenamer(UserInput configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<CtMethod<?>> prettify(List<CtMethod<?>> amplifiedTestsToBePrettified) {
        List<CtMethod<?>> prettifiedTests = new ArrayList<>();
        // read JSON with extended coverage for the amplified test cases
        TestClassJSON resultJson = Util.readExtendedCoverageResultJSON(configuration);
        if (resultJson == null) {
            LOGGER.error("No result json found under configured DSpot output path!");
            return amplifiedTestsToBePrettified;
        }
        Map<String, TestCaseJSON> mapTestNameToResult =
                resultJson.getTestCases().stream().collect(Collectors.toMap(TestCaseJSON::getName,
                        Function.identity()));
        Map<CtMethod<?>, String> testToNames =
                buildNameMapForTests(mapTestNameToResult, amplifiedTestsToBePrettified);


        for (CtMethod<?> test : amplifiedTestsToBePrettified) {
            TestCaseJSON testCaseJSON = mapTestNameToResult.get(test.getSimpleName());
            String newTestName = testToNames.get(test);

            CtMethod<?> renamedTest = test.clone();
            renamedTest.setSimpleName(newTestName);

            resultJson.getTestCases().remove(testCaseJSON);
            TestCaseJSON renamedTestCaseJSON = new TestCaseJSON(newTestName, testCaseJSON);
            resultJson.addTestCase(renamedTestCaseJSON);

            prettifiedTests.add(renamedTest);
        }

        // update JSON with new names
        Util.writeExtendedCoverageResultJSON(configuration, resultJson);

        return prettifiedTests;
    }


    /**
     * Builds the name map based on the unique methods covered by each test.
     * Uses only two covered methods per name and numbers through duplicate names.
     *
     * @param mapTestNameToResult map of the old test name to its JSON coverage report
     * @param amplifiedTestsToBePrettified list of all methods to be considered
     * @return map of the amplifed test cases to their new name
     */
    private Map<CtMethod<?>, String> buildNameMapForTests(Map<String, TestCaseJSON> mapTestNameToResult,
                                                          List<CtMethod<?>> amplifiedTestsToBePrettified) {

        Map<CtMethod<?>, List<String>> testToCoveredMethods = new HashMap<>();

        for (CtMethod<?> test : amplifiedTestsToBePrettified) {
            TestCaseJSON testCaseJSON = mapTestNameToResult.get(test.getSimpleName());
            testToCoveredMethods.put(test, getCoveredMethods(testCaseJSON.getCoverageImprovement()));
        }

        Map<CtMethod<?>, List<String>> testToUniqueCoveredMethods = findUniqueCoveredMethods(testToCoveredMethods);

        Map<CtMethod<?>, String> testToNewNames = initializeNames(testToUniqueCoveredMethods, testToCoveredMethods);

        // Add numbers to remaining duplicate names
        fixAmbiguousTestNames(testToNewNames);

        return testToNewNames;
    }


    /**
     * @param coverageImprovement of the test case
     * @return a list of all method names where this test case improves coverage
     */
    private List<String> getCoveredMethods(CoverageImprovement coverageImprovement) {
        List<String> methodNames = new ArrayList<>();
        coverageImprovement.getInstructionImprovement().classCoverageMaps.forEach((className, classCoverageMap) -> {
            List<Map.Entry<String, MethodCoverage>> methodNamesAndMethodCoverages =
                    new ArrayList<>(classCoverageMap.methodCoverageMap.entrySet());

            // put the methods with most additional coverage first (to be first in the name later)
            methodNamesAndMethodCoverages.sort((e1, e2) ->
                Integer.compare(e1.getValue().totalAdditionallyCoveredInstructions(),
                        e2.getValue().totalAdditionallyCoveredInstructions())
            );

            methodNamesAndMethodCoverages.forEach((entry) -> {
                methodNames.add(entry.getKey());
            });
        });
        return methodNames;
    }

    /**
     * @return for each test the set of methods uniquely covered by this test
     * @param testToCoveredMethods map of the test to the names of all methods it covers
     */
    private Map<CtMethod<?>, List<String>> findUniqueCoveredMethods(Map<CtMethod<?>, List<String>> testToCoveredMethods) {
        // Could be optimised
        Map<CtMethod<?>, List<String>> testToUniqueGoals = new HashMap<>();

        for(Map.Entry<CtMethod<?>, List<String>> entry : testToCoveredMethods.entrySet()) {
            List<String> goalSet = new ArrayList<>(entry.getValue());
            for(Map.Entry<CtMethod<?>, List<String>> otherEntry : testToCoveredMethods.entrySet()) {
                if(entry == otherEntry)
                    continue;
                goalSet.removeAll(otherEntry.getValue());
            }
            testToUniqueGoals.put(entry.getKey(), goalSet);
        }
        return testToUniqueGoals;
    }

    private Map<CtMethod<?>, String> initializeNames(Map<CtMethod<?>, List<String>> testToUniqueCoveredMethods,
                                                     Map<CtMethod<?>, List<String>> testToCoveredMethods) {
        Map<CtMethod<?>, String> testToNewNames = new HashMap<>();

        // Start off with only the top goals and then iteratively add more goals
        for(Map.Entry<CtMethod<?>, List<String>> testAndUniqueCoveredMethods : testToUniqueCoveredMethods.entrySet()) {

            List<String> goalsToUse;
            if (testAndUniqueCoveredMethods.getValue().isEmpty()) {
                // make a name with max two covered methods to keep name easily readable
                goalsToUse = testToCoveredMethods.get(testAndUniqueCoveredMethods.getKey());
            } else {
                goalsToUse = testAndUniqueCoveredMethods.getValue();
            }
            String testName;
            int topIndex = Math.min(goalsToUse.size(), 2);
            testName = getTestName(goalsToUse.subList(0, topIndex));

            testToNewNames.put(testAndUniqueCoveredMethods.getKey(),testName);
        }
        return testToNewNames;
    }

    /**
     * There may be tests with the same calculated name, in which case we add a number suffix.
     */
    private void fixAmbiguousTestNames(Map<CtMethod<?>, String> testToNewNames) {
        Map<String, Integer> nameCount = new HashMap<>();
        Map<String, Integer> testCount = new HashMap<>();
        for(String methodName : testToNewNames.values()) {
            if(nameCount.containsKey(methodName))
                nameCount.put(methodName, nameCount.get(methodName) + 1);
            else {
                nameCount.put(methodName, 1);
                testCount.put(methodName, 0);
            }
        }
        for(Map.Entry<CtMethod<?>, String> entry : testToNewNames.entrySet()) {
            if(nameCount.get(entry.getValue()) > 1) {
                int num = testCount.get(entry.getValue());
                testCount.put(entry.getValue(), num + 1);
                testToNewNames.put(entry.getKey(), entry.getValue() + num);
            }
        }
    }

    /**
     * Construct name for the given list of covered methods.
     *
     * @param coveredMethods List of the names of the covered methods relevant for the test name
     * @return concatenated and camel-case capitalized name
     */
    private String getTestName(List<String> coveredMethods) {
        int num = 0;
        StringBuilder name = new StringBuilder(PREFIX);
        if(coveredMethods.isEmpty()) {
            return name.toString(); // No goals - no name
        } else if(coveredMethods.size() == 1) {
            name.append(capitalize(coveredMethods.get(0)));
        } else if(coveredMethods.size() == 2) {
            name.append(capitalize(coveredMethods.get(0)));
            name.append(STR_AND).append(capitalize(coveredMethods.get(1)));
        } else {
            name.append(coveredMethods.get(0));
            for(int i = 1; i < coveredMethods.size(); i++)
                if(i <= 3)
                    name.append(STR_AND).append(capitalize(coveredMethods.get(i)));
        }
        return name.toString();
    }

}
