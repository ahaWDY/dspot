package eu.stamp_project.prettifier.variablenaming;

import eu.stamp_project.dspot.amplifier.amplifiers.utils.AmplificationChecker;
import eu.stamp_project.dspot.common.test_framework.TestFramework;
import eu.stamp_project.prettifier.Prettifier;
import eu.stamp_project.prettifier.Util;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleVariableNamePrettifier implements Prettifier {

    @Override
    public List<CtMethod<?>> prettify(List<CtMethod<?>> amplifiedTestsToBePrettified) {
        return amplifiedTestsToBePrettified.stream().map(this::prettifyTest).collect(Collectors.toList());
    }

    private CtMethod<?> prettifyTest(CtMethod<?> test) {
        CtMethod<?> prettifiedTest = test.clone();
        // find all new variables named by DSpot
        final List<CtLocalVariable<?>> localVariables = prettifiedTest.getElements(new TypeFilter<>(CtLocalVariable.class));
        int counter = 1;
        for (CtLocalVariable<?> localVariable : localVariables) {
            if (variableNamedByDSpot(localVariable.getSimpleName())) {
                String newName = localVariable.getType().getSimpleName() + counter;
                counter++;

                // rename usages of variable
                List<CtVariableRead<?>> variableReads =
                        prettifiedTest.getElements(new Util.LOCAL_VARIABLE_READ_FILTER(localVariable));
                variableReads.forEach(ctVariableRead -> ctVariableRead.getVariable().setSimpleName(newName));

                // rename at assignment
                localVariable.setSimpleName(newName);
            }
        }

        return prettifiedTest;
    }

    private static boolean variableNamedByDSpot(String variableName) {
        return variableName.startsWith("__DSPOT_") || (variableName.startsWith("o_") && variableName.contains("__"));
    }

}
