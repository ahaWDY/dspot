package eu.stamp_project;

import eu.stamp_project.prettifier.Main;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class MainTest {

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
                "--path-to-amplified-test-class", "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java"
        });
        assertTrue(new File("target/dspot/output/fr/inria/amplified/AmplifiedTest.java").exists());
    }

    @Test
    public void testApplyGeneralMinimizer() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java",
                "--apply-general-minimizer"
        });
        assertTrue(new File("target/dspot/output/fr/inria/amplified/AmplifiedTest.java").exists());
    }

    @Test
    public void testApplyPitMinimizer() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java",
                "--apply-pit-minimizer"
        });
        assertTrue(new File("target/dspot/output/fr/inria/amplified/AmplifiedTest.java").exists());
    }

    @Test
    public void testImprovedCoverageTestNames() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java",
                "--rename-test-methods=ImprovedCoverageTestRenamer",
                "--output-path", "src/test/resources/sample/amplified-output",
                "--test", "eu.stamp_project.AppTest",
                "--test-cases", "test1_mg12_assSep41,test1_mg13_failAssert0"
        });
        assertTrue(new File("target/dspot/output/fr/inria/amplified/AmplifiedTest.java").exists());
    }

    @Ignore // DOES NOT WORK ON TRAVIS, CANNOT FIND python3 cmd
    @Test
    public void testRenameTestMethods() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java",
                "--path-to-code2vec", "src/test/resources/code2vec/code2vec",
                "--path-to-code2vec-model", "../model",
                "--rename-test-methods=Code2VecTestRenamer"
        });
        assertTrue(new File("target/dspot/output/fr/inria/amplified/AmplifiedTest.java").exists());
    }

    @Test
    public void testSimpleVariableNames() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java",
                "--rename-local-variables=SimpleVariableRenamer"
        });
        assertTrue(new File("target/dspot/output/fr/inria/amplified/AmplifiedTest.java").exists());
    }

    @Test
    public void testRenameLocalVariables() throws Exception {
        Main.main(new String[]{
                "--absolute-path-to-project-root", "src/test/resources/sample/",
                "--path-to-amplified-test-class", "src/test/resources/sample/src/test/java/fr/inria/amplified/AmplifiedTest.java",
                "--path-to-code2vec", "src/test/resources/code2vec/code2vec",
                "--path-to-code2vec-model", "../model",
                "--rename-local-variables=Context2NameVariableRenamer"
        });
        assertTrue(new File("target/dspot/output/fr/inria/amplified/AmplifiedTest.java").exists());
    }

}
