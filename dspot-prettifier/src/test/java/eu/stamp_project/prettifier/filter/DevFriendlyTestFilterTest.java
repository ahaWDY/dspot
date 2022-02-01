package eu.stamp_project.prettifier.filter;

import eu.stamp_project.AbstractTest;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.ArrayList;
import java.util.List;

class DevFriendlyTestFilterTest extends AbstractTest {

    private Factory factory;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Launcher launcher = new Launcher();
        launcher.addInputResource(getAbsolutePathToProjectRoot() + "src/test/java/eu/stamp_project/AppTest.java");
        launcher.buildModel();

        TestFramework.init(launcher.getFactory());

        factory = launcher.getFactory();

    }

    @Test
    public void testPrettify() {
        DevFriendlyTestFilter devFriendlyTestFilter = new DevFriendlyTestFilter();
        List<CtMethod<?>> methods = new ArrayList<>();

        final CtMethod<?> test1 = factory.Class().get("eu.stamp_project.AppTest")
                .getMethodsByName("test1").get(0);
        System.out.println(test1);
    }
}