package eu.stamp_project.prettifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.common.automaticbuilder.AutomaticBuilder;
import eu.stamp_project.prettifier.configuration.TestRenamerEnum;
import eu.stamp_project.prettifier.configuration.VariableRenamerEnum;
import eu.stamp_project.prettifier.description.TestDescriptionGenerator;
import eu.stamp_project.prettifier.testnaming.Code2VecTestRenamer;
import eu.stamp_project.prettifier.variablenaming.Context2NameVariableRenamer;
import eu.stamp_project.prettifier.minimization.GeneralMinimizer;
import eu.stamp_project.prettifier.minimization.Minimizer;
import eu.stamp_project.prettifier.minimization.PitMutantMinimizer;
import eu.stamp_project.prettifier.configuration.UserInput;
import eu.stamp_project.prettifier.output.PrettifiedTestMethods;
import eu.stamp_project.prettifier.output.report.ReportJSON;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.dspot.common.configuration.DSpotState;
import eu.stamp_project.dspot.common.configuration.InitializeDSpot;
import eu.stamp_project.dspot.common.configuration.check.Checker;
import eu.stamp_project.dspot.common.configuration.check.InputErrorException;
import eu.stamp_project.prettifier.testnaming.ImprovedCoverageTestRenamer;
import eu.stamp_project.prettifier.variablenaming.SimpleVariableRenamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import spoon.Launcher;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/02/19
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static ReportJSON report;
    private static DSpotState dSpotState;

    public static void main(String[] args) {
        UserInput inputConfiguration = new UserInput();
        final CommandLine commandLine = new CommandLine(inputConfiguration);
        commandLine.setUsageHelpWidth(120);
        try {
            commandLine.parseArgs(args);
        } catch (Exception e) {
            e.printStackTrace();
            commandLine.usage(System.err);
            return;
        }
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        }
        if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            return;
        }
        if (inputConfiguration.shouldRunExample()) {
            inputConfiguration.configureExample();
        }
        try {
            Checker.preChecking(inputConfiguration);
        } catch (InputErrorException e) {
            e.printStackTrace();
            commandLine.usage(System.err);
            return;
        }

        InitializeDSpot initializeDSpot = new InitializeDSpot();
        initializeDSpot.init(inputConfiguration);
        dSpotState = initializeDSpot.getDSpotState();

        report = new ReportJSON(inputConfiguration);

        runPrettifier(inputConfiguration);
    }

    public static void runPrettifier(UserInput configuration) {

        if (dSpotState.getTestClassesToBeAmplified().size() > 1) {
            LOGGER.error("More than one test class passed! The prettifier can only process one amplified test class " +
                         "at a time.");
            return;
        }
        if (dSpotState.getTestClassesToBeAmplified().size() < 1 && configuration.getPathToAmplifiedTestClass().isEmpty()) {
            LOGGER.error("No test class passed! Please pass the class to be prettified with --test or --path-to" +
                         "-amplified-test-class");
            return;
        }
        CtType<?> amplifiedTestClass;
        if (!configuration.getPathToAmplifiedTestClass().isEmpty()) {
            amplifiedTestClass = loadAmplifiedTestClassFromFile(configuration);
        } else {
            amplifiedTestClass = dSpotState.getTestClassesToBeAmplified().get(0);
        }

        final List<CtMethod<?>> testMethods =
                dSpotState.getTestFinder().findTestMethods(amplifiedTestClass,
                        dSpotState.getTestMethodsToBeAmplifiedNames());
        Main.report.nbTestMethods = testMethods.size();

        final List<CtMethod<?>> prettifiedAmplifiedTestMethods =
                prettify(
                        amplifiedTestClass,
                        testMethods,
                        configuration
                );

        output(amplifiedTestClass, prettifiedAmplifiedTestMethods, configuration);
    }

    public static CtType<?> loadAmplifiedTestClassFromFile(UserInput configuration) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.addInputResource(configuration.getPathToAmplifiedTestClass());
        launcher.buildModel();
        return launcher.getFactory().Class().getAll().get(0);
    }

    public static List<CtMethod<?>> prettify(CtType<?> amplifiedTestClass, List<CtMethod<?>> testMethods,
                                             UserInput configuration) {
        final List<CtMethod<?>> minimizedAmplifiedTestMethods;

        // 1 minimize amplified test methods
        if (configuration.isApplyAllPrettifiers() ||  configuration.isApplyGeneralMinimizer() || configuration.isApplyPitMinimizer()) {
            minimizedAmplifiedTestMethods = applyMinimization(
                    testMethods,
                    amplifiedTestClass,
                    configuration
            );
        } else {
            minimizedAmplifiedTestMethods = testMethods;
        }
        List<CtMethod<?>> prettifiedTestMethods = minimizedAmplifiedTestMethods;
        // 2 rename test methods
        if (configuration.isApplyAllPrettifiers() || configuration.getTestRenamer() != TestRenamerEnum.None) {
            prettifiedTestMethods = applyTestRenaming(prettifiedTestMethods, configuration);
        }
        // 3 rename local variables
        if (configuration.isApplyAllPrettifiers() || configuration.getVariableRenamer() != VariableRenamerEnum.None) {
            prettifiedTestMethods = applyVariableRenaming(prettifiedTestMethods, configuration);
        }
        // 4 generate test descriptions
        if (configuration.isApplyAllPrettifiers() || configuration.isGenerateTestDescriptions()) {
            prettifiedTestMethods = new TestDescriptionGenerator(configuration).prettify(prettifiedTestMethods);
        }
        return prettifiedTestMethods;
    }

    public static List<CtMethod<?>> applyMinimization(List<CtMethod<?>> amplifiedTestMethodsToBeMinimized,
                                                      CtType<?> amplifiedTestClass,
                                                      UserInput configuration) {

        Main.report.medianNbStatementBefore = Main.getMedian(amplifiedTestMethodsToBeMinimized.stream()
                .map(ctMethod -> ctMethod.getElements(new TypeFilter<>(CtStatement.class)))
                .map(List::size)
                .collect(Collectors.toList()));

        // 1 apply general minimization
        if (configuration.isApplyAllPrettifiers() || configuration.isApplyGeneralMinimizer()) {
            amplifiedTestMethodsToBeMinimized = Main.applyGivenMinimizer(new GeneralMinimizer(), amplifiedTestMethodsToBeMinimized);
            // update the test class with minimized test methods
            final ArrayList<CtMethod<?>> allMethods = new ArrayList<>(amplifiedTestClass.getMethods());
            allMethods.stream()
                    .filter(TestFramework.get()::isTest)
                    .forEach(amplifiedTestClass::removeMethod);
            amplifiedTestMethodsToBeMinimized.forEach(amplifiedTestClass::addMethod);
        }

        // 2 apply pit minimization
        if (configuration.isApplyAllPrettifiers() || configuration.isApplyPitMinimizer()) {
            final AutomaticBuilder automaticBuilder = configuration.getBuilderEnum().getAutomaticBuilder(configuration);
            amplifiedTestMethodsToBeMinimized = Main.applyGivenMinimizer(
                    new PitMutantMinimizer(
                            amplifiedTestClass,
                            automaticBuilder,
                            configuration.getAbsolutePathToProjectRoot(),
                            configuration.getClasspathClassesProject(),
                            configuration.getAbsolutePathToTestClasses()
                    ),
                    amplifiedTestMethodsToBeMinimized
            );
        }

        Main.report.medianNbStatementAfter = Main.getMedian(amplifiedTestMethodsToBeMinimized.stream()
                .map(ctMethod -> ctMethod.getElements(new TypeFilter<>(CtStatement.class)))
                .map(List::size)
                .collect(Collectors.toList()));

        return amplifiedTestMethodsToBeMinimized;
    }

    private static List<CtMethod<?>> applyGivenMinimizer(Minimizer minimizer, List<CtMethod<?>> amplifiedTestMethodsToBeMinimized) {
        final List<CtMethod<?>> minimizedAmplifiedTestMethods = amplifiedTestMethodsToBeMinimized.stream()
                .map(minimizer::minimize)
                .collect(Collectors.toList());
        minimizer.updateReport(Main.report);
        return minimizedAmplifiedTestMethods;
    }

    private static List<CtMethod<?>> applyTestRenaming(List<CtMethod<?>> testMethods, UserInput configuration) {
        List<CtMethod<?>> prettifiedTestMethods = testMethods;
        if (configuration.isApplyAllPrettifiers() || configuration.getTestRenamer() == TestRenamerEnum.ImprovedCoverageTestRenamer) {
            prettifiedTestMethods = new ImprovedCoverageTestRenamer(configuration).prettify(prettifiedTestMethods);
        }
        if (configuration.isApplyAllPrettifiers() || configuration.getTestRenamer() == TestRenamerEnum.Code2VecTestRenamer) {
            prettifiedTestMethods = new Code2VecTestRenamer(configuration).prettify(prettifiedTestMethods);
        }
        return prettifiedTestMethods;
    }

    private static List<CtMethod<?>> applyVariableRenaming(List<CtMethod<?>> testMethods, UserInput configuration) {
        List<CtMethod<?>> prettifiedTestMethods = testMethods;
        if (configuration.isApplyAllPrettifiers() || configuration.getVariableRenamer() == VariableRenamerEnum.SimpleVariableRenamer) {
            prettifiedTestMethods = new SimpleVariableRenamer().prettify(prettifiedTestMethods);
        }
        // TODO train one better model
        if (configuration.isApplyAllPrettifiers() || configuration.getVariableRenamer() == VariableRenamerEnum.Context2NameVariableRenamer) {
            prettifiedTestMethods = new Context2NameVariableRenamer().prettify(prettifiedTestMethods);
        }
        return prettifiedTestMethods;
    }

    public static <T extends Number & Comparable<T>> Double getMedian(List<T> list) {
        Collections.sort(list);
        return list.size() % 2 == 0 ?
                list.stream().skip(list.size() / 2 - 1).limit(2).mapToDouble(value -> new Double(value.toString())).average().getAsDouble() :
                new Double(list.stream().skip(list.size() / 2).findFirst().get().toString());
    }

    public static void output(CtType<?> amplifiedTestClass,
                              List<CtMethod<?>> prettifiedAmplifiedTestMethods,
                              UserInput configuration) {
        new PrettifiedTestMethods(configuration.getOutputDirectory())
                .output(amplifiedTestClass, prettifiedAmplifiedTestMethods);
        Main.report.output(configuration, amplifiedTestClass);
    }

}
