package eu.stamp_project.dspot.common.report.output.amplifiers;

import eu.stamp_project.dspot.common.miscellaneous.TypeUtils;
import eu.stamp_project.dspot.common.report.output.AmplifierReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;

/**
 * Reports any changes where a literal was changed to another value.
 * E.g. by the {@link eu.stamp_project.dspot.amplifier.amplifiers.FastLiteralAmplifier},
 * or the {@link eu.stamp_project.dspot.amplifier.amplifiers.NullifierAmplifier}
 */
public class LiteralAmplifierReport extends AmplifierReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiteralAmplifierReport.class);

    private final String variableName;
    private final String originalValue;
    private final String newValue;
    private final String type;
    /**
     * Whether the literal is assigned to a local variable.
     */
    private final boolean isLocalVariable;
    /**
     * Only defined if the literal was not assigned to a local variable (and therefore used as a parameter).
     */
    private final String methodName;

    public LiteralAmplifierReport(CtExpression<?> literal, Object newValue) {

        // Determine what literal occurrence we have:
        CtElement parent = TypeUtils.getFirstStatementParent(literal);
        // - assignment to local variable
        if (parent instanceof CtLocalVariable<?>) {
            this.variableName = ((CtLocalVariable<?>) parent).getSimpleName();
            this.isLocalVariable = true;
            this.methodName = "";
        }
        // - method/constructor parameter
        else if (parent instanceof CtInvocation<?>) {
            int literalIndex = ((CtInvocation<?>) parent).getArguments().indexOf(literal);
            CtExecutable<?> methodCalled = ((CtInvocation<?>) parent).getExecutable().getDeclaration();
            CtParameter<?> parameter = methodCalled.getParameters().get(literalIndex);
            this.variableName = parameter.getSimpleName();
            this.methodName =
                    methodCalled.getParent(CtClass.class).getSimpleName() + "." + methodCalled.getSimpleName();
            this.isLocalVariable = false;
        } else {
            LOGGER.warn("Tried to report modification for Literal that is not in variable declaration or method call!");
            this.variableName = "unknown";
            this.isLocalVariable = false;
            this.methodName = "";
        }
        this.originalValue = literal.toString();
        this.newValue = newValue.toString();
        this.type = literal.getType().toString();
    }

    @Override
    public boolean isAssertionReport() {
        return false;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public boolean isLocalVariable() {
        return isLocalVariable;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getType() {
        return type;
    }
}
