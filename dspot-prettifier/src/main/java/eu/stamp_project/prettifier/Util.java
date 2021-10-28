package eu.stamp_project.prettifier;

import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class Util {

    public static final class LOCAL_VARIABLE_READ_FILTER extends TypeFilter<CtVariableRead<?>> {
        private final CtLocalVariableReference<?> localVariableReference;

        public LOCAL_VARIABLE_READ_FILTER(CtLocalVariable<?> localVariable) {
            super(CtVariableRead.class);
            this.localVariableReference = localVariable.getReference();
        }

        @Override
        public boolean matches(CtVariableRead element) {
            return localVariableReference.equals(element.getVariable());
        }
    }
}
