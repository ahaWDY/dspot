package eu.stamp_project.dspot.selector.extendedcoverageselector;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.impl.CoverageImpl;
import eu.stamp_project.testrunner.listener.junit4.JUnit4Coverage;
import org.jacoco.core.data.ExecutionDataStore;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.*;
import java.io.FilenameFilter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExtendedCoverage {

    private Map<String, List<Integer>> instructionsCoveredPerClass;

    public ExtendedCoverage(Coverage coverage) {

        this.instructionsCoveredPerClass =
                Arrays.stream(coverage.getExecutionPath().split(";"))
                        .collect(Collectors.toMap(
                                s -> s.split(":")[0],
                                s -> {
                                    String[] split = s.split(":");
                                    if (split.length < 2) {
                                        return Collections.emptyList();
                                    } else {
                                        return Arrays.stream(split[1].split(","))
                                            .map(Integer::parseInt).collect(Collectors.toList());
                                    }
                                }));

        this.instructionsCoveredPerClass = cleanAllZeroValuesFromMap(this.instructionsCoveredPerClass);
    }

    public Map<String, List<Integer>> getInstructionsCoveredPerClass() {
        return instructionsCoveredPerClass;
    }

    private Map<String, List<Integer>> cleanAllZeroValuesFromMap(Map<String, List<Integer>> map) {
        Map<String, List<Integer>> cleaned = new HashMap<>();
        map.forEach((k, v) -> {
            if (!v.stream().allMatch(i -> i == 0)) {
                cleaned.put(k, v);
            }
        });
        return cleaned;
    }

    public boolean isBetterThan(ExtendedCoverage that) {
        if (that == null) {
            return true;
        }
        Map<String, List<Integer>> instructionDiff = improvementDiff(this.instructionsCoveredPerClass,
        that.instructionsCoveredPerClass);

        return !instructionDiff.keySet().isEmpty();
    }

    public void accumulate(ExtendedCoverage toAdd) {
        this.instructionsCoveredPerClass = accumulate(this.instructionsCoveredPerClass,
                toAdd.instructionsCoveredPerClass);
    }

    /**
     * @param base original coverage map
     * @param toAdd coverage map to be accumulated on top
     * @return accumulative coverage of base and toAdd
     */
    private static Map<String, List<Integer>> accumulate(Map<String, List<Integer>> base,
                                                         Map<String, List<Integer>> toAdd) {
        Set<String> mergedKeys = new HashSet<>();
        mergedKeys.addAll(base.keySet());
        mergedKeys.addAll(toAdd.keySet());

        Map<String, List<Integer>> accumulated = new HashMap<>();
        for (String mergedKey : mergedKeys) {
            List<Integer> valuesBase = base.get(mergedKey);
            List<Integer> valuesToAdd = toAdd.get(mergedKey);

            if (valuesBase == null) {
                accumulated.put(mergedKey, valuesToAdd);
            }
            else if (valuesToAdd == null) {
                accumulated.put(mergedKey, valuesBase);
            } else {
                accumulated.put(mergedKey,
                        IntStream.range(0, valuesToAdd.size())
                                .mapToObj(i -> Math.max(valuesBase.get(i), valuesToAdd.get(i)))
                                .collect(Collectors.toList()));
            }
        }
        return accumulated;
    }

    private static Map<String, List<Integer>> improvementDiff(Map<String, List<Integer>> thiz,
                                                        Map<String, List<Integer>> that) {

        Map<String, List<Integer>> thizBetterDiff = new HashMap<>();
        thiz.entrySet().stream()
                .filter(entry -> that.containsKey(entry.getKey()))
                .forEach(entry -> {
                    List<Integer> instructionsCoveredByThat = that.get(entry.getKey());
                    // calculate 'diff' of the coverage values, i.e. all the lines where thiz covered more than that
                    List<Integer> betterAt = IntStream.range(0, entry.getValue().size())
                            .mapToObj(i -> entry.getValue().get(i) - instructionsCoveredByThat.get(i))
                            .collect(Collectors.toList());

                    if (betterAt.parallelStream().anyMatch(i -> i > 0)) {
                        thizBetterDiff.put(entry.getKey(), betterAt);
                    }
                });
        return thizBetterDiff;
    }

    public String explainImprovementOver(ExtendedCoverage other) {

        Map<String, List<Integer>> instructionDiff = improvementDiff(this.instructionsCoveredPerClass,
                other.instructionsCoveredPerClass);
        StringBuilder explanation = new StringBuilder("Coverage improved at");
        instructionDiff.forEach((key, value) -> {
            explanation.append("\n").append(key).append(":\n");
            int index = -1;
            for (Integer instructionImprovement : value) {
                index++;
                if (instructionImprovement <= 0) {
                    continue;
                }
                explanation.append("L. ").append(index).append(" +").append(instructionImprovement).append(" instr.").append("\n");
            }
            explanation.replace(explanation.length() - 2, explanation.length(), "");
        });

        return explanation.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtendedCoverage that = (ExtendedCoverage) o;
        return Objects.equals(instructionsCoveredPerClass, that.instructionsCoveredPerClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instructionsCoveredPerClass);
    }
}
