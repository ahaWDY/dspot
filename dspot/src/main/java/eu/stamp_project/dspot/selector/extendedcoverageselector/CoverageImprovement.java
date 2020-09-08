package eu.stamp_project.dspot.selector.extendedcoverageselector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoverageImprovement {

    public CoverageImprovement(Map<String, List<Integer>> instructionDiff) {
        this.instructionImprovement = new HashMap<>();

        instructionDiff.forEach((methodName, value) -> {
            Map<Integer, Integer> lineCoverage = new HashMap<>();

            int index = -1;
            for (Integer instructionImprovement : value) {
                index++;
                if (instructionImprovement <= 0) {
                    continue;
                }
                lineCoverage.put(index, instructionImprovement);
            }

            this.instructionImprovement.put(methodName, lineCoverage);
        });
    }

    /**
     * For each class name (FQ), for each line number (jacoco line with statements!) the improvement in coverage
     */
    private final Map<String, Map<Integer, Integer>> instructionImprovement;

    public Map<String, Map<Integer, Integer>> getInstructionImprovement() {
        return instructionImprovement;
    }

    @Override
    public String toString() {
        StringBuilder explanation = new StringBuilder("Coverage improved at");
        this.instructionImprovement.forEach((methodName, instructionImprovement) -> {

            explanation.append("\n").append(methodName).append(":\n");

            instructionImprovement.forEach((line, instructions) ->
                    explanation.append("L. ").append(line).append(" +")
                    .append(instructions).append(" instr.").append("\n"));

            explanation.replace(explanation.length() - 2, explanation.length(), "");
        });
        return explanation.toString();
    }
}
