package org.narrative.batch.listener;

import org.slf4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;

import java.util.List;

/**
 * A logging step listener that can be used for chunked steps.
 */
public class LoggingChunkedStepListener<T, S> extends StepListenerSupport<T, S> {
    private final ThreadLocal<String> stepName = new ThreadLocal<>();
    private final Logger log;
    private final Integer chunkSize;

    public LoggingChunkedStepListener(Logger log, Integer chunkSize) {
        this.log = log;
        this.chunkSize = chunkSize;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepName.set(stepExecution.getStepName());
        log.debug("beforeStep {} chunk size: {}", stepExecution.getStepName(), chunkSize);
    }

    @Override
    public void beforeChunk(ChunkContext chunkContext) {
        if (chunkSize != null) {
            log.debug("beforeChunk {}: - Reading items: {}", stepName.get(), chunkSize);
        }
    }

    @Override
    public void beforeWrite(List<? extends S> items) {
        log.debug("beforeWrite {}: Writing {} items", stepName.get(), items.size());
    }

    @Override
    public void afterWrite(List<? extends S> items) {
        if ((items != null) && !items.isEmpty()) {
            log.debug("afterWrite {}: Items written: {}", stepName.get(), items.size());
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (log.isInfoEnabled()) {

            log.info("afterStep {}: Exit status: {} Read count: {} Write count: {} Commit count: {}",
                    stepExecution.getStepName(),
                    stepExecution.getExitStatus().getExitCode(),
                    stepExecution.getReadCount(),
                    stepExecution.getWriteCount(),
                    stepExecution.getCommitCount());
        }

        List<Throwable> throwableList = stepExecution.getFailureExceptions();
        boolean failed = false;
        if (throwableList != null && !throwableList.isEmpty()) {
            failed = true;
            log.error("afterStep: {} - errors occured during processing step", stepName);
            int i = 1;
            for(Throwable t: throwableList) {
                log.error("Step error {}", i++, t);
            }
        }

        this.stepName.remove();
        return failed ? ExitStatus.FAILED : super.afterStep(stepExecution);
    }
}
