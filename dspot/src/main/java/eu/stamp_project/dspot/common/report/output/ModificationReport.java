package eu.stamp_project.dspot.common.report.output;

import eu.stamp_project.dspot.common.report.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        LOGGER.info("Modification report was output to {}", outputDirectory);
        LOGGER.info("Modification report was: {}", reportsPerClass.toString());
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
        String testClassName = "null_class_name";
        if (testClass != null) {
            testClassName = testClass.getQualifiedName();
        }

        ClassModificationReport classModificationReport = reportsPerClass.get(testClassName);
        if (classModificationReport == null) {
            classModificationReport = new ClassModificationReport();
            reportsPerClass.put(testClassName, classModificationReport);
        }
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
