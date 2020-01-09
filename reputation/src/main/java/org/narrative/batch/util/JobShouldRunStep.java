package org.narrative.batch.util;

import org.slf4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class JobShouldRunStep implements Tasklet, StepExecutionListener {
    public static final String EXIT_JOB = "exitJob";
    public static final String CONTINUE_JOB = "continueJob";
    public static final String SHOULD_RUN_KEY = "shouldRun";
    private static final String SHOULD_RUN_STEP = "ShouldRunStep";

    private final Logger log;

    public JobShouldRunStep(Logger log) {
        this.log = log;
    }

    public String buildStepName(String jobName) {
        return jobName + SHOULD_RUN_STEP;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        //NOOP
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
        // Provided by {@link JobExecutionListener} built via buildSingletonJobExecutionListener
        Boolean shouldRun = (Boolean) stepExecution.getJobExecution().getExecutionContext().get(SHOULD_RUN_KEY);
        if (shouldRun != null && shouldRun) {
            log.info("Job {} will start", jobName);
            return ExitStatus.COMPLETED;
        } else {
            log.info("Job {} will not start - job should not run", jobName);
            return new ExitStatus(EXIT_JOB);
        }
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        return RepeatStatus.FINISHED;
    }
}
