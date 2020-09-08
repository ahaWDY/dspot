package eu.stamp_project.dspot.selector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.compilation.DSpotCompiler;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.Counter;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReport;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReportImpl;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestCaseJSON;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;
import eu.stamp_project.dspot.selector.extendedcoverageselector.ExtendedCoverage;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import org.apache.commons.io.FileUtils;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;

import javax.xml.stream.events.Comment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class ExtendedCoverageSelector extends TakeAllSelector {

    ExtendedCoverage initialCoverage;

    ExtendedCoverage cumulativeAmplifiedCoverage;

    public ExtendedCoverageSelector(AutomaticBuilder automaticBuilder, UserInput configuration) {
        super(automaticBuilder, configuration);
    }

    @Override
    public List<CtMethod<?>> selectToAmplify(CtType<?> classTest, List<CtMethod<?>> testsToBeAmplified) {
        this.currentClassTestToBeAmplified = classTest;
        // calculate existing coverage of the whole test suite
        try {
            this.initialCoverage = new ExtendedCoverage(EntryPoint.runCoverage(
                    classpath + AmplificationHelper.PATH_SEPARATOR + targetClasses,
                    this.targetClasses,
                    this.currentClassTestToBeAmplified.getQualifiedName()
            ));
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        this.cumulativeAmplifiedCoverage = this.initialCoverage;
        return testsToBeAmplified;
    }

    @Override
    public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
        CoveragePerTestMethod coveragePerTestMethod = computeCoverageForGivenTestMethods(amplifiedTestToBeKept);

        final List<CtMethod<?>> methodsKept = new ArrayList<>();
        for (CtMethod<?> ctMethod : amplifiedTestToBeKept) {
            ExtendedCoverage newCoverage =
                    new ExtendedCoverage(coveragePerTestMethod.getCoverageOf(ctMethod.getSimpleName()));
            if (newCoverage.isBetterThan(cumulativeAmplifiedCoverage)) {
                //note: we still explain the improvement to the coverage before amplification. Maybe we should change?
                DSpotUtils.addComment(ctMethod, newCoverage.explainImprovementOver(initialCoverage),
                        CtComment.CommentType.BLOCK);
                methodsKept.add(ctMethod);
                cumulativeAmplifiedCoverage.accumulate(newCoverage);
            }
        }
        this.selectedAmplifiedTest.addAll(methodsKept);

        return methodsKept;
    }

    private CoveragePerTestMethod computeCoverageForGivenTestMethods(List<CtMethod<?>> testsToBeAmplified) {
        final String[] methodNames = testsToBeAmplified.stream().map(CtNamedElement::getSimpleName).toArray(String[]::new);
        try {
            return EntryPoint.runCoveragePerTestMethods(
                    this.classpath + AmplificationHelper.PATH_SEPARATOR + this.targetClasses,
                    this.targetClasses,
                    this.currentClassTestToBeAmplified.getQualifiedName(),
                    methodNames
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TestSelectorElementReport report() {
        final String report = "Amplification results with " + this.selectedAmplifiedTest.size() + " new tests.";

        // todo: json could report on initial vs. cumulative coverage

//        // compute the new coverage obtained by the amplification
//        final CtType<?> clone = this.currentClassTestToBeAmplified.clone();
//        this.currentClassTestToBeAmplified.getPackage().addType(clone);
//        this.selectedAmplifiedTest.forEach(clone::addMethod);
//        try {
//            FileUtils.deleteDirectory(new File(DSpotCompiler.getPathToAmplifiedTestSrc()));
//        } catch (IOException ignored) {
//            //ignored
//        }
//        DSpotUtils.printCtTypeToGivenDirectory(clone, new File(DSpotCompiler.getPathToAmplifiedTestSrc()));
//        DSpotCompiler.compile(
//                DSpotCompiler.getPathToAmplifiedTestSrc(),
//                this.classpath + AmplificationHelper.PATH_SEPARATOR + this.targetClasses,
//                new File(this.pathToTestClasses)
//        );
//        try {
//            final Coverage coverageResults = EntryPoint.runCoverage(
//                    this.classpath,
//                    this.targetClasses,
//                    this.currentClassTestToBeAmplified.getQualifiedName()
//            );
//            return new TestSelectorElementReportImpl(report,
//                    jsonReport(coverageResults),
//                    Collections.emptyList(), "");
//        } catch (TimeoutException e) {
//            throw new RuntimeException(e);
//        }
        return new TestSelectorElementReportImpl(report, jsonReport(), Collections.emptyList(),"");
    }

    private TestClassJSON jsonReport() {
        TestClassJSON testClassJSON;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(this.outputDirectory + "/" +
                this.currentClassTestToBeAmplified.getQualifiedName() + "report.json");
        testClassJSON = new TestClassJSON();
        this.selectedAmplifiedTest.stream().map(CtNamedElement::getSimpleName).map(TestCaseJSON::new).forEach(testClassJSON::addTestCase);

        return testClassJSON;
    }


}
