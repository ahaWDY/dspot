package eu.stamp_project.dspot.selector;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.collector.NullCollector;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.configuration.options.CommentEnum;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.Counter;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReport;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReportImpl;
import eu.stamp_project.dspot.common.report.output.selector.branchcoverage.json.TestCaseJSON;
import eu.stamp_project.dspot.common.report.output.selector.branchcoverage.json.TestClassJSON;
import eu.stamp_project.dspot.selector.branchcoverageselector.BranchCoverage;
import eu.stamp_project.dspot.selector.branchcoverageselector.Coverage;
import eu.stamp_project.dspot.selector.branchcoverageselector.LineCoverage;
import eu.stamp_project.dspot.selector.branchcoverageselector.clover.CloverExecutor;
import eu.stamp_project.dspot.selector.branchcoverageselector.clover.CloverReader;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class BranchCoverageSelector extends TakeAllSelector {
    // for branchcoverageselector
    protected String absolutePathToProjectRoot;
    protected String absolutePathToTestSourceCode;
    protected String targetClass;
    protected String targetMethod;
    protected String targetBranch;

    Coverage initialCoverage;

    List<BranchCoverage> initialBranchCoverage;

    List<LineCoverage> initialLineCoverage;

    Map<CtMethod<?>, List<BranchCoverage>> branchCoveragePerTestCase;

    Map<CtMethod<?>, List<LineCoverage>> lineCoveragePerPerTestCase;

    public BranchCoverageSelector(AutomaticBuilder automaticBuilder, UserInput configuration) {
        super(automaticBuilder, configuration);
        this.absolutePathToProjectRoot = configuration.getAbsolutePathToProjectRoot();
        this.absolutePathToTestSourceCode = configuration.getAbsolutePathToTestSourceCode();
        String[] classAddress = configuration.getTargetClass().split("\\.");
        this.targetClass = classAddress[classAddress.length-1];
        this.targetMethod = configuration.getTargetMethod();
        this.targetBranch = configuration.getTargetBranch();

        this.branchCoveragePerTestCase = new HashMap<>();
        this.lineCoveragePerPerTestCase = new HashMap<>();
    }

    @Override
    public List<CtMethod<?>> selectToAmplify(CtType<?> classTest, List<CtMethod<?>> testsToBeAmplified) {
        this.currentClassTestToBeAmplified = classTest;
        // calculate existing coverage of the whole test suite
        String testClassName = currentClassTestToBeAmplified.getSimpleName();
        new CloverExecutor().instrumentAndRunGivenTestClass(absolutePathToProjectRoot, currentClassTestToBeAmplified.getQualifiedName());
        Coverage result = new CloverReader().read(absolutePathToProjectRoot);
        this.initialCoverage = result;
        this.initialBranchCoverage = result.getBranchCoverageForTestClassAndClassNameMethodName(testClassName, targetClass, targetMethod);
        this.initialLineCoverage = result.getLineCoverageForTestClassAndClassNameMethodName(testClassName, targetClass, targetMethod);
        new CloverExecutor().runMavenClean(absolutePathToProjectRoot);
        return testsToBeAmplified;
    }

    @Override
    public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
        //compute the coverage
        final CtType<?> amplifiedTestClass = AmplificationHelper.createAmplifiedTest(amplifiedTestToBeKept, currentClassTestToBeAmplified);
        final String amplifiedClassName = AmplificationHelper.getAmplifiedName(amplifiedTestClass);
        amplifiedTestClass.setSimpleName(amplifiedClassName);
        final File outputDirectory = new File(absolutePathToTestSourceCode);
        DSpotUtils.printAndCompileToCheck(amplifiedTestClass, outputDirectory, new NullCollector());

        new CloverExecutor().instrumentAndRunGivenTestClass(absolutePathToProjectRoot, amplifiedClassName);
        Coverage result = new CloverReader().read(absolutePathToProjectRoot);

        final String regex = File.separator.equals("/") ? "/" : "\\\\";
        final String pathname =
                outputDirectory.getAbsolutePath() + File.separator +
                        amplifiedTestClass.getQualifiedName().replaceAll("\\.", regex) + ".java";

        deleteFile(pathname);

        final List<CtMethod<?>> methodsKept = new ArrayList<>();
        //parse target branch
        if(targetBranch.equals("noBranch")){
            for (CtMethod<?> ctMethod : amplifiedTestToBeKept) {
                List<LineCoverage> lineCoverageList = result.getLineCoverageForTestClassTestMethodAndClassNameMethodName(amplifiedClassName, ctMethod.getSimpleName(), targetClass, targetMethod);
                    if (!(lineCoverageList==null)){
                        methodsKept.add(ctMethod);
                        lineCoveragePerPerTestCase.put(ctMethod, lineCoverageList);
                    }
            }
        }
        else {
            String[] splits = targetBranch.split(":");
            int branchLine = Integer.valueOf(splits[0]).intValue();
            int symbol = targetBranch.contains("True") ? 0 : 1;
            for (CtMethod<?> ctMethod : amplifiedTestToBeKept) {
                List<BranchCoverage> branchCoverageList = result.getBranchCoverageForTestClassTestMethodAndClassNameMethodName(amplifiedClassName, ctMethod.getSimpleName(), targetClass, targetMethod);
                if (symbol == 0) {
                    if (!(branchCoverageList==null) && branchCoverageList.stream().filter(branchCoverage -> branchCoverage.getRegion().getStartLine() == branchLine && branchCoverage.getFalseHitCount() > 0).findAny().isPresent()) {
                        methodsKept.add(ctMethod);
                        branchCoveragePerTestCase.put(ctMethod, branchCoverageList);
                    }
                } else {
                    if (!(branchCoverageList==null) && branchCoverageList.stream().filter(branchCoverage -> branchCoverage.getRegion().getStartLine() == branchLine && branchCoverage.getTrueHitCount() > 0).findAny().isPresent()) {
                        methodsKept.add(ctMethod);
                        lineCoveragePerPerTestCase.put(ctMethod, result.getLineCoverageForTestClassTestMethodAndClassNameMethodName(amplifiedClassName, ctMethod.getSimpleName(), targetClass, targetMethod));
                    }
                }
            }
        }
        this.selectedAmplifiedTest.addAll(methodsKept);
        return methodsKept;
    }

    @Override
    public TestSelectorElementReport report() {
        final String report = "Amplification results with " + this.selectedAmplifiedTest.size() + " new tests.";
//        return new TestSelectorElementReportImpl(report, jsonReport(), Collections.emptyList(), "");
        return new TestSelectorElementReportImpl(report, jsonReport(), Collections.emptyList(), "");
    }

    private TestClassJSON jsonReport() {
        TestClassJSON testClassJSON;
        testClassJSON = new TestClassJSON(this.initialBranchCoverage, this.initialLineCoverage, this.branchCoveragePerTestCase,
                this.lineCoveragePerPerTestCase);
        this.selectedAmplifiedTest.stream()
                .map(ctMethod -> new TestCaseJSON(ctMethod.getSimpleName(),
                        Counter.getAssertionOfSinceOrigin(ctMethod),
                        Counter.getInputOfSinceOrigin(ctMethod),
                        this.branchCoveragePerTestCase.get(ctMethod),
                        this.lineCoveragePerPerTestCase.get(ctMethod)))
                .forEach(testClassJSON::addTestCase);

        return testClassJSON;
    }

    private void deleteFile(String pathname){
        File file = new File(pathname);
        file.delete();
    }

}
