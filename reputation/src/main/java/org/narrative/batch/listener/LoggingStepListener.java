package org.narrative.batch.listener;

import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.batch.core.step.tasklet.Tasklet;

import java.util.List;

/**
 * A logging step listener that can be used for non-chunked {@link Tasklet}s.  See
 * {@link LoggingChunkedStepListener} for chunked steps.
 */
public class LoggingStepListener<T, S> extends StepListenerSupport<T, S> {
    private final ThreadLocal<String> stepName = new ThreadLocal<>();
    private final Logger log;
    private final Level logLevel;

    public LoggingStepListener(Logger log, Level logLevel) {
        this.log = log;
        this.logLevel = logLevel;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepName.set(stepExecution.getStepName());
        logAtLevel("beforeStep {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logAtLevel("afterStep {}: Exit status: {}",
                stepExecution.getStepName(),
                stepExecution.getExitStatus().getExitCode());

        List<Throwable> throwableList = stepExecution.getFailureExceptions();
        boolean failed = false;
        if (throwableList != null && !throwableList.isEmpty()) {
            failed = true;
            log.error("afterStep: {} - errors occured during processing step", stepName);
            int i = 1;
            for (Throwable t : throwableList) {
                log.error("Step error {}", i++, t);
            }
        }

        this.stepName.remove();
        return failed ? ExitStatus.FAILED : super.afterStep(stepExecution);
    }

    private void logAtLevel(String message, Object...args) {
        switch(logLevel) {
            case INFO:
                log.info(message, args);
                break;
            case WARN:
                log.warn(message, args);
                break;
            case ERROR:
                log.error(message, args);
                break;
            case DEBUG:
                log.debug(message, args);
                break;
            case TRACE:
                log.trace(message, args);
                break;
            default:
                log.info(message, args);
        }
    }
}

