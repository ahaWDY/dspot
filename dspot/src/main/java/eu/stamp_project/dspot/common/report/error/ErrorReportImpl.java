package eu.stamp_project.dspot.common.report.error;

import eu.stamp_project.dspot.common.miscellaneous.AmplificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 08/04/19
 */
public class ErrorReportImpl implements ErrorReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorReport.class);

    protected List<Error> errors;

    protected List<Error> inputErrors;

    public ErrorReportImpl() {
        this.errors = new ArrayList<>();
        this.inputErrors = new ArrayList<>();
    }

    @Override
    public void reset() {
        this.errors.clear();
        this.inputErrors.clear();
    }

    @Override
    public List<Error> getErrors() {
        return this.errors;
    }

    @Override
    public List<Error> getInputError() {
        return this.inputErrors;
    }

    private boolean hasError() {
        return !(this.errors.isEmpty() && this.inputErrors.isEmpty());
    }

    @Override
    public void output(String outputDirectory) {
        if (!hasError()) {
            LOGGER.info("DSpot amplified your test suite without errors. (no errors report will be outputted)");
        } else {
            final StringBuilder report = new StringBuilder();
            if (!this.inputErrors.isEmpty()) {
                LOGGER.error("DSpot encountered some input errors.");
                displayAndAppendErrors(this.inputErrors, report, "DSpot encountered %d input error(s).");
            }
            if (!this.errors.isEmpty()) {
                LOGGER.warn("DSpot encountered some errors during amplification.");
                displayAndAppendErrors(this.errors, report, "DSpot encountered %d error(s) during amplification.");
            }
            final String stringReport = report.toString();
            LOGGER.warn(stringReport);
            try (FileWriter writer = new FileWriter(outputDirectory + "/errors_report.txt", false)) {
                writer.write(stringReport);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void outputForClass(String outputDirectory, CtType<?> testClass) {
        if (hasError()) {
            // output errors already
            this.output(outputDirectory);
        }
        // we do nothing to not fill up the log unnecessarily with repeated "everything is okay" messages
    }

    protected void displayAndAppendErrors(List<Error> currentErrors, StringBuilder report, String intro) {
        report.append(String.format(intro, currentErrors.size()))
                .append(AmplificationHelper.LINE_SEPARATOR)
                .append(
                        currentErrors.stream().map(Error::toString).collect(Collectors.joining(AmplificationHelper.LINE_SEPARATOR))
                );
    }

    @Override
    public void addInputError(Error error) {
        this.addErrorToGivenList(this.inputErrors, error);
    }

    @Override
    public void addError(Error error) {
        this.addErrorToGivenList(this.errors, error);
    }

    protected void addErrorToGivenList(List<Error> givenErrors, Error newError) {
        LOGGER.warn(newError.toString());
        givenErrors.add(newError);
    }

}
