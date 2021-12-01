package eu.stamp_project;

import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import eu.stamp_project.prettifier.Main;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class MainTest {

    public static String PATH_INPUT_TEST_CLASS = "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java";
    public static String OUTPUT_PATH_AMPLIFIED_TEST = "target/dspot/output/fr/inria/amplified/AmplifiedTest.java";
    public static String OUTPUT_PATH_APP_TEST = "target/dspot/output/eu/stamp_project/AppTest.java";
    public static String REPORT_PATH_APP_TEST = "target/dspot/output/eu.stamp_project.AppTest_prettifier_report.json";

    @Before
    public void setUp() throws Exception {
        try {
            FileUtils.deleteDirectory(new File("target/dspot/output/"));
            FileUtils.deleteDirectory(new File("src/test/resources/sample/target"));
        } catch (Exception ignored) {

        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            //FileUtils.deleteDirectory(new File("target/dspot/output/"));
            //FileUtils.deleteDirectory(new File("src/test/resources/sample/target"));
        } catch (Exception ignored) {

        }
    }

    @Test
    public void testNoPrettifiers() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", PATH_INPUT_TEST_CLASS,
                "--test", "fr.inria.amplified.AmplifiedTest",
        });
        assertTrue(new File(OUTPUT_PATH_AMPLIFIED_TEST).exists());
    }

    @Test
    public void testApplyGeneralMinimizer() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", PATH_INPUT_TEST_CLASS,
                "--test", "fr.inria.amplified.AmplifiedTest",
                "--apply-general-minimizer"
        });
        assertTrue(new File(OUTPUT_PATH_AMPLIFIED_TEST).exists());
    }

    @Test
    public void testApplyPitMinimizer() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", PATH_INPUT_TEST_CLASS,
                "--test", "fr.inria.amplified.AmplifiedTest",
                "--apply-pit-minimizer"
        });
        assertTrue(new File(OUTPUT_PATH_AMPLIFIED_TEST).exists());
    }

    @Test
    public void testImprovedCoverageTestNames() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--rename-test-methods=ImprovedCoverageTestRenamer",
                "--path-to-dspot-reports", "src/test/resources/sample/amplified-output",
                "--test", "eu.stamp_project.AppTest",
                "--test-cases", "test1_mg12_assSep41,test1_mg13_failAssert0"
        });
        assertTrue(new File(OUTPUT_PATH_APP_TEST).exists());
        assertFileContains("Name changed to covered methods in test", "testCompute", OUTPUT_PATH_APP_TEST);
        assertFileContains("Name changed to covered methods in test", "testThrowException", OUTPUT_PATH_APP_TEST);
        assertFileContains("New name included in json report", "testThrowException", REPORT_PATH_APP_TEST);
    }

    @Ignore // DOES NOT WORK ON TRAVIS, CANNOT FIND python3 cmd
    @Test
    public void testCode2VecTestNames() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", PATH_INPUT_TEST_CLASS,
                "--test", "fr.inria.amplified.AmplifiedTest",
                "--path-to-code2vec", "src/test/resources/code2vec/code2vec",
                "--path-to-code2vec-model", "../model",
                "--rename-test-methods=Code2VecTestRenamer"
        });
        assertTrue(new File(OUTPUT_PATH_AMPLIFIED_TEST).exists());
    }

    @Test
    public void testSimpleVariableNames() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", PATH_INPUT_TEST_CLASS,
                "--test", "fr.inria.amplified.AmplifiedTest",
                "--rename-local-variables=SimpleVariableRenamer"
        });
        assertTrue(new File(OUTPUT_PATH_AMPLIFIED_TEST).exists());
        assertOutputClassContains("int1");
        assertOutputClassContains("Integer1");
        assertOutputClassContains("Non-DSpot named variables are not modified","testingAnInt");
    }

    @Test
    public void testContext2VecLocalVariableNames() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", PATH_INPUT_TEST_CLASS,
                "--test", "fr.inria.amplified.AmplifiedTest",
                "--path-to-code2vec", "src/test/resources/code2vec/code2vec",
                "--path-to-code2vec-model", "../model",
                "--rename-local-variables=Context2NameVariableRenamer"
        });
        assertTrue(new File(OUTPUT_PATH_AMPLIFIED_TEST).exists());
    }

    @Test
    public void testTestDescriptions() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--generate-descriptions",
                "--with-comment", "All",
                "--path-to-dspot-reports", "src/test/resources/sample/amplified-output",
                "--test", "eu.stamp_project.AppTest",
                "--test-cases", "test1_mg12_assSep41,test1_mg13_failAssert0"
        });
        assertTrue(new File(OUTPUT_PATH_APP_TEST).exists());
        assertFileContains("Added description to method", "* Checks compute.", OUTPUT_PATH_APP_TEST);
        assertFileContains("Added description to method", "* Checks throwException.", OUTPUT_PATH_APP_TEST);
        assertFileContains("Description included in json report", "Checks throwException.", REPORT_PATH_APP_TEST);
    }

    private void assertOutputClassContains(String expected) throws Exception{
        assertOutputClassContains(null,expected);
    }

    private void assertOutputClassContains(String message, String expected) throws Exception{
        final File amplifiedTestClass = new File(OUTPUT_PATH_AMPLIFIED_TEST);

        try (BufferedReader reader = new BufferedReader(new FileReader(amplifiedTestClass))) {
            String content = reader.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
            assertTrue(message, content.contains(expected));
        }
    }

    private void assertFileContains(String message, String expected, String path) throws Exception{
        final File amplifiedTestClass = new File(path);

        try (BufferedReader reader = new BufferedReader(new FileReader(amplifiedTestClass))) {
            String content = reader.lines().collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR));
            assertTrue(message, content.contains(expected));
        }
    }
}
