package eu.stamp_project.prettifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestClassJSON;
import eu.stamp_project.prettifier.configuration.UserInput;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.*;

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

    public static TestClassJSON readExtendedCoverageResultJSON(UserInput configuration) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(new FileReader(configuration.getPathToDSpotReports()+ File.separator
                                                + configuration.getTestClasses().get(0) + "_report.json"),
                    TestClassJSON.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeExtendedCoverageResultJSON(UserInput configuration, TestClassJSON testClassJSON) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(configuration.getPathToDSpotReports()+ File.separator
                                   + configuration.getTestClasses().get(0) + "_report.json");
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(gson.toJson(testClassJSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
