package eu.stamp_project.prettifier;

import com.google.gson.Gson;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;
import eu.stamp_project.prettifier.configuration.UserInput;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

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

    public static TestClassJSON getExtendedCoverageResultJSON(UserInput configuration) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(new FileReader(configuration.getOutputDirectory()+ File.separator
                                                + configuration.getTestClasses().get(0) + "_report.json"),
                    TestClassJSON.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
