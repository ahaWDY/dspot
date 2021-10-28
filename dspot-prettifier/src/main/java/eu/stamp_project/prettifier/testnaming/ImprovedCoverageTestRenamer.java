package eu.stamp_project.prettifier.testnaming;

import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestCaseJSON;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;
import eu.stamp_project.dspot.selector.extendedcoverageselector.CoverageImprovement;
import eu.stamp_project.prettifier.Prettifier;
import eu.stamp_project.prettifier.Util;
import eu.stamp_project.prettifier.configuration.UserInput;
import eu.stamp_project.prettifier.testnaming.code2vec.Code2VecExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        TestClassJSON resultJson = Util.getExtendedCoverageResultJSON(configuration);
        if (resultJson == null) {
            LOGGER.error("No result json found under configured DSpot output path!");
            return amplifiedTestsToBePrettified;
        }

        Map<String, TestCaseJSON> mapTestNameToResult =
                resultJson.getTestCases().stream().collect(Collectors.toMap(TestCaseJSON::getName,
                        Function.identity()));

        for (CtMethod<?> test : amplifiedTestsToBePrettified) {
            CtMethod<?> renamedTest = test.clone();
            TestCaseJSON testCaseJSON = mapTestNameToResult.get(renamedTest.getSimpleName());
            renamedTest.setSimpleName(getTestName(getCoveredMethods(testCaseJSON.getCoverageImprovement())));

            // TODO update name in testCaseJSON too!!!

            prettifiedTests.add(renamedTest);
        }

        return prettifiedTests;
    }

    private List<String> getCoveredMethods(CoverageImprovement coverageImprovement) {
        List<String> methodNames = new ArrayList<>();
        coverageImprovement.getInstructionImprovement().classCoverageMaps.forEach((className, classCoverageMap) -> {
            classCoverageMap.methodCoverageMap.forEach((methodName, methodCoverage) -> {
                methodNames.add(methodName);
            });
        });
        return methodNames;
    }
    /**
     * Determine name for the given test
     *
     * @param coveredMethods List of the names of the covered methods relevant for the test name
     * @return concatenated and camel-case capitalized name
     */

    private static String getTestName(List<String> coveredMethods) {
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
            // TODO don't add more than two covered methods!
            name.append(coveredMethods.get(0));
            for(int i = 1; i < coveredMethods.size(); i++)
                if(i <= 3)
                    name.append(STR_AND).append(capitalize(coveredMethods.get(i)));
        }
        return name.toString();
    }

}
