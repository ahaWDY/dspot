package eu.stamp_project.dspot.selector;

import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.report.output.selector.TestSelectorElementReport;
import eu.stamp_project.dspot.selector.extendedcoverageselector.ExtendedCoverage;
import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class ExtendedCoverageSelector extends TakeAllSelector {

    ExtendedCoverage initialCoverage;

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
        return testsToBeAmplified;
    }

    @Override
    public List<CtMethod<?>> selectToKeep(List<CtMethod<?>> amplifiedTestToBeKept) {
        CoveragePerTestMethod coveragePerTestMethod = computeCoverageForGivenTestMethods(amplifiedTestToBeKept);
        final List<CtMethod<?>> methodsKept = amplifiedTestToBeKept.stream()
                .filter(ctMethod ->
                    new ExtendedCoverage(coveragePerTestMethod.getCoverageOf(ctMethod.getSimpleName())).isBetterThan(initialCoverage)
                )
                .collect(Collectors.toList());
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
        return super.report();
    }

    @Override
    protected void reset() {
        super.reset();
    }
}
