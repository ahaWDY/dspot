package eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json;

import eu.stamp_project.dspot.selector.extendedcoverageselector.CoverageImprovement;
import eu.stamp_project.dspot.selector.extendedcoverageselector.ExtendedCoverage;

public class TestCaseJSON {

    private final String name;
    private final int nbAssertionAdded;
    private final int nbInputAdded;
    private final CoverageImprovement coverageImprovement;
    private final ExtendedCoverage fullCoverage;
    private final String description;

    public TestCaseJSON(String name, int nbAssertionAdded, int nbInputAdded, CoverageImprovement coverageImprovement,
                        ExtendedCoverage fullCoverage) {
        this.name = name;
        this.nbAssertionAdded = nbAssertionAdded;
        this.nbInputAdded = nbInputAdded;
        this.coverageImprovement = coverageImprovement;
        this.fullCoverage = fullCoverage;
        this.description = "";
    }

    public TestCaseJSON(String name, int nbAssertionAdded, int nbInputAdded, CoverageImprovement coverageImprovement,
                        ExtendedCoverage fullCoverage, String description) {
        this.name = name;
        this.nbAssertionAdded = nbAssertionAdded;
        this.nbInputAdded = nbInputAdded;
        this.coverageImprovement = coverageImprovement;
        this.fullCoverage = fullCoverage;
        this.description = description;
    }

    public TestCaseJSON copyAndUpdateName(String name) {
        return new TestCaseJSON(name, this.nbAssertionAdded, this.nbInputAdded, this.coverageImprovement, this.fullCoverage, this.description);
    }

    public TestCaseJSON copyAndUpdateDescription(String description) {
        return new TestCaseJSON(this.name, this.nbAssertionAdded, this.nbInputAdded, this.coverageImprovement, this.fullCoverage, description);
    }

    public String getName() {
        return name;
    }

    public int getNbAssertionAdded() {
        return nbAssertionAdded;
    }

    public int getNbInputAdded() {
        return nbInputAdded;
    }

    public CoverageImprovement getCoverageImprovement() {
        return coverageImprovement;
    }

    public ExtendedCoverage getFullCoverage() {
        return fullCoverage;
    }
}
