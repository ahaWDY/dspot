package eu.stamp_project.prettifier.output.report;

import spoon.reflect.declaration.CtMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RenamingReport {

    /**
     * Maps 'testName#oldVariableName' to 'newVariableName'
     */
    private final Map<String, String> renamedVariables;

    /**
     * Maps the old test name to the new test name.
     */
    private final Map<String, String> renamedTests;

    public RenamingReport() {
        renamedVariables = new HashMap<>();
        renamedTests = new HashMap<>();
    }

    /**
     * Reports the renaming of a variable within a test.
     * @param test the test containing the variable.
     * @param oldName
     * @param newName
     */
    public void addVariableRenaming(CtMethod<?> test, String oldName, String newName) {
        renamedVariables.put(test.getSimpleName() + "#" + oldName, newName);
    }

    public Optional<String> getNewVariableName(CtMethod<?> test, String oldName) {
        return Optional.ofNullable(renamedVariables.get(test.getSimpleName() + "#" + oldName));
    }

    public Map<String, String> getRenamedVariables() {
        return renamedVariables;
    }

    /**
     * Reports the renaming of a test method
     *
     * @param test    the amplified test case *before* renaming.
     * @param newName the original test case name.
     */
    public void addTestRenaming(CtMethod<?> test, String newName) {
        renamedTests.put(newName, test.getSimpleName());
    }

    public Optional<String> getNewTestName(String oldName) {
        return Optional.ofNullable(renamedTests.get(oldName));
    }
}
