package eu.stamp_project.prettifier.description;

import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
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
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestDescriptionGenerator implements Prettifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDescriptionGenerator.class);

    UserInput configuration;

    public TestDescriptionGenerator(UserInput configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<CtMethod<?>> prettify(List<CtMethod<?>> amplifiedTestsToBePrettified) {
        List<CtMethod<?>> prettifiedTests = new ArrayList<>();

        TestClassJSON amplificationReport;
        try {
            amplificationReport = (TestClassJSON) Main.report.amplificationReport;
        } catch (ClassCastException e) {
            LOGGER.error("No DSpot output is not from ExtendedCoverageSelector! TestDescriptionGenerator not " +
                         "applied");
            return amplifiedTestsToBePrettified;
        }
        if (amplificationReport == null) {
            LOGGER.error("No json found under configured DSpot output path! TestDescriptionGenerator not " +
                         "applied");
            return amplifiedTestsToBePrettified;
        }

        Map<String, TestCaseJSON> mapTestNameToResult = amplificationReport.mapTestNameToResult();

        Map<CtMethod<?>, List<String>> testToCoveredMethods = new HashMap<>();

        for (CtMethod<?> test : amplifiedTestsToBePrettified) {
            TestCaseJSON testCaseJSON = mapTestNameToResult.get(test.getSimpleName());
            testToCoveredMethods.put(test, Util.getCoveredMethods(testCaseJSON.getCoverageImprovement()));
        }

        for (CtMethod<?> test : amplifiedTestsToBePrettified) {
            StringBuilder testDescription = new StringBuilder("Checks ");
            for (String methodName : testToCoveredMethods.get(test)) {
                testDescription.append(methodName);
                testDescription.append(" and ");
            }
            // replace last " and "
            testDescription.replace(testDescription.length() - 5, testDescription.length(),".");

            String description = testDescription.toString();
            DSpotUtils.addComment(test, description, CtComment.CommentType.JAVADOC, CommentEnum.All);
            amplificationReport.updateTestCase(mapTestNameToResult.get(test.getSimpleName()),
                    mapTestNameToResult.get(test.getSimpleName()).copyAndUpdateDescription(description));

            prettifiedTests.add(test);
        }

        return prettifiedTests;
    }
}
