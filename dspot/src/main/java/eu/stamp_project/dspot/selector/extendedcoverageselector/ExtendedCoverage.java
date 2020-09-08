package eu.stamp_project.dspot.selector.extendedcoverageselector;

import eu.stamp_project.testrunner.listener.Coverage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExtendedCoverage {

    private Map<String, List<Integer>> instructionsCoveredPerClass;

    public ExtendedCoverage(Coverage coverage) {

        this.instructionsCoveredPerClass = Arrays.stream(coverage.getExecutionPath().split(";"))
                .collect(Collectors.toMap(s -> s.split(":")[0], s -> {
                    String[] split = s.split(":");
                    if (split.length < 2) {
                        return Collections.emptyList();
                    } else {
                        return Arrays.stream(split[1].split(",")).map(Integer::parseInt).collect(Collectors.toList());
                    }
                }));

        this.instructionsCoveredPerClass = cleanAllZeroValuesFromMap(this.instructionsCoveredPerClass);
    }

    private ExtendedCoverage(Map<String, List<Integer>> instructionsCoveredPerClass) {
        this.instructionsCoveredPerClass = instructionsCoveredPerClass;
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
     * @param base  original coverage map
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
            } else if (valuesToAdd == null) {
                accumulated.put(mergedKey, valuesBase);
            } else {
                accumulated.put(mergedKey, IntStream.range(0, valuesToAdd.size())
                        .mapToObj(i -> Math.max(valuesBase.get(i), valuesToAdd.get(i))).collect(Collectors.toList()));
            }
        }
        return accumulated;
    }

    private static Map<String, List<Integer>> improvementDiff(Map<String, List<Integer>> thiz, Map<String,
            List<Integer>> that) {

        Map<String, List<Integer>> thizBetterDiff = new HashMap<>();
        thiz.entrySet().stream().filter(entry -> that.containsKey(entry.getKey())).forEach(entry -> {
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

    public CoverageImprovement coverageImprovementOver(ExtendedCoverage other) {
        Map<String, List<Integer>> instructionDiff = improvementDiff(this.instructionsCoveredPerClass,
                other.instructionsCoveredPerClass);
        return new CoverageImprovement(instructionDiff);
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

    public ExtendedCoverage clone() {
        return new ExtendedCoverage(this.instructionsCoveredPerClass);
    }
}
