package eu.stamp_project.dspot.common.report.output;

import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.dspot.common.miscellaneous.DSpotUtils;
import eu.stamp_project.dspot.common.report.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Keeps track of the modifications made to each test case during the amplification process.
 */
public class ModificationReport implements Report {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModificationReport.class);

    // maps the qualified name of the class to its report
    private Map<String, ClassModificationReport> reportsPerClass;

    public ModificationReport() {
        this.reportsPerClass = new HashMap<>();
    }

    @Override
    public void output(String outputDirectory) {
        this.reportsPerClass.entrySet().stream().filter(entry -> entry.getValue() != null)
                .forEach(entry -> entry.getValue().output(entry.getKey(), outputDirectory));
    }

    @Override
    public void reset() {
        reportsPerClass.clear();
    }

    /**
     * Report a modification made to a test case during the amplification.
     *
     * @param testClass                  fully qualified name of the class containing the tests
     * @param testNameBeforeModification simple name of the test before the amplifier was applied
     * @param testNameAfterModification  simple name of the test case after modification (with amplifier suffix)
     * @param report                     the {@link AmplifierReport} that represents the modification
     */
    public void reportModification(CtType<?> testClass, String testNameBeforeModification,
                                   String testNameAfterModification, AmplifierReport report) {
        String testClassName = "null class name";
        if (testClass != null) {
            testClassName = testClass.getQualifiedName();
        }

        ClassModificationReport classModificationReport = reportsPerClass.computeIfAbsent(testClassName,
                s -> new ClassModificationReport());
        classModificationReport.reportModification(testNameBeforeModification, testNameAfterModification, report);
    }

    /**
     * Filter the report to only keep modifications made to the test cases given in
     *
     * @param testClass     fully qualified name of the class containing the tests
     * @param selectedTests tests of which to keep the reports
     */
    public void filterModifications(CtType<?> testClass, List<CtMethod<?>> selectedTests) {
        ClassModificationReport classModificationReport = reportsPerClass.get(testClass.getQualifiedName());
        if (classModificationReport != null) {
            classModificationReport.filterModifications(selectedTests);
        }
    }
}
