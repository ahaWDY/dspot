package eu.stamp_project.prettifier.filter;

import eu.stamp_project.prettifier.AbstractTest;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DevFriendlyTestFilterTest extends AbstractTest {

    private Factory factory;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        Launcher launcher = new Launcher();
        launcher.addInputResource(getAbsolutePathToProjectRoot() + "src/test/java/eu/stamp_project/AppTest.java");
        launcher.buildModel();
        TestFramework.init(launcher.getFactory());
        factory = launcher.getFactory();
    }

    @Test
    public void testFilterSimpleGetter() {
        DevFriendlyTestFilter devFriendlyTestFilter = new DevFriendlyTestFilter();
        List<CtMethod<?>> methods = new ArrayList<>();

        methods.add(factory.Class().get("eu.stamp_project.AppTest")
                .getMethodsByName("testSimpleGetter").get(0));
        List<CtMethod<?>> filteredMethods = devFriendlyTestFilter.prettify(methods);
        assertEquals(filteredMethods.size(), 0);
    }

    @Test
    public void testKeepException() {
        DevFriendlyTestFilter devFriendlyTestFilter = new DevFriendlyTestFilter();
        List<CtMethod<?>> methods = new ArrayList<>();

        methods.add(factory.Class().get("eu.stamp_project.AppTest")
                .getMethodsByName("testException").get(0));
        List<CtMethod<?>> filteredMethods = devFriendlyTestFilter.prettify(methods);
        assertEquals(filteredMethods.size(), 1);
    }

    @Test
    public void testFilterHashCode() {
        DevFriendlyTestFilter devFriendlyTestFilter = new DevFriendlyTestFilter();
        List<CtMethod<?>> methods = new ArrayList<>();

        methods.add(factory.Class().get("eu.stamp_project.AppTest")
                .getMethodsByName("testHashCode").get(0));
        List<CtMethod<?>> filteredMethods = devFriendlyTestFilter.prettify(methods);
        assertEquals(filteredMethods.size(), 0);
    }
}