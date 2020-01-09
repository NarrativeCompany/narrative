package org.narrative.batch.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.narrative.batch.service.BatchJobMetadataPurgeService;
import org.narrative.shared.spring.metrics.TimedService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@TimedService(percentiles = {0.8, 0.9, 0.99})
public class BatchJobMetadataPurgeServiceImpl implements BatchJobMetadataPurgeService {
    private static final String SUCCESSFUL_JOB_FILTER = " = 'COMPLETED'";
    private static final String UNSUCCESSFUL_JOB_FILTER = " != 'COMPLETED'";
    private static final String JOB_EXECUTION_FILTER = "CREATE_TIME < ? and STATUS";
    private static final String JOB_EXECUTION_ID_QUERY = "SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION where ";
    private static final String SUBQ_TOKEN = "@@JOB_EXEC_ID_SUBQ@@";
    private static final String FILTER_TOKEN = "@@JOB_EXEC_FILTER@@";

    private static final String DELETE_BATCH_STEP_EXECUTION_CONTEXT = "delete from BATCH_STEP_EXECUTION_CONTEXT where STEP_EXECUTION_ID in (select STEP_EXECUTION_ID from BATCH_STEP_EXECUTION where JOB_EXECUTION_ID in (" + SUBQ_TOKEN + "))";
    private static final String DELETE_BATCH_STEP_EXECUTION = "delete from BATCH_STEP_EXECUTION where JOB_EXECUTION_ID in (" + SUBQ_TOKEN + ")";
    private static final String DELETE_BATCH_JOB_EXECUTION_CONTEXT = "delete from BATCH_JOB_EXECUTION_CONTEXT where JOB_EXECUTION_ID in (" + SUBQ_TOKEN + ")";
    private static final String DELETE_BATCH_JOB_EXECUTION_PARAMS = "delete from BATCH_JOB_EXECUTION_PARAMS where JOB_EXECUTION_ID in (" + SUBQ_TOKEN + ")";
    private static final String DELETE_BATCH_JOB_EXECUTION = "delete from BATCH_JOB_EXECUTION where " + FILTER_TOKEN;
    private static final String DELETE_BATCH_JOB_INSTANCE = "delete from BATCH_JOB_INSTANCE where JOB_INSTANCE_ID not in (select JOB_INSTANCE_ID from BATCH_JOB_EXECUTION)";

    private final JdbcTemplate jdbcTemplate;

    public BatchJobMetadataPurgeServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String buildFilter(boolean purgeUnfinishedJobs) {
        return JOB_EXECUTION_FILTER + (purgeUnfinishedJobs ? UNSUCCESSFUL_JOB_FILTER : SUCCESSFUL_JOB_FILTER);
    }

    @Transactional
    @Override
    public int purgeJobMetadata(Instant olderThanInstant, boolean purgeUnfinishedJobs) {
        String filter = buildFilter(purgeUnfinishedJobs);
        String jobExecSubQ = JOB_EXECUTION_ID_QUERY + filter;
        LocalDateTime purgeDateTime = LocalDateTime.ofInstant(olderThanInstant, ZoneOffset.UTC);

        int stepExecContextCount = jdbcTemplate.update(DELETE_BATCH_STEP_EXECUTION_CONTEXT.replace(SUBQ_TOKEN, jobExecSubQ), purgeDateTime);

        int stepExecCount = jdbcTemplate.update(DELETE_BATCH_STEP_EXECUTION.replace(SUBQ_TOKEN, jobExecSubQ), purgeDateTime);

        int jobExecContextCount = jdbcTemplate.update(DELETE_BATCH_JOB_EXECUTION_CONTEXT.replace(SUBQ_TOKEN, jobExecSubQ), purgeDateTime);

        int jobExecParamsCount = jdbcTemplate.update(DELETE_BATCH_JOB_EXECUTION_PARAMS.replace(SUBQ_TOKEN, jobExecSubQ), purgeDateTime);

        int jobExecCount = jdbcTemplate.update(DELETE_BATCH_JOB_EXECUTION.replace(FILTER_TOKEN, filter), purgeDateTime);

        int jobInstanceCount = jdbcTemplate.update(DELETE_BATCH_JOB_INSTANCE);

        log.info("Removed rows BATCH_STEP_EXECUTION_CONTEXT: {} BATCH_STEP_EXECUTION: {} BATCH_JOB_EXECUTION_CONTEXT: {} BATCH_JOB_EXECUTION_PARAMS: {} BATCH_JOB_INSTANCE: {} BATCH_JOB_EXECUTION: {}", stepExecContextCount, stepExecCount, jobExecContextCount, jobExecParamsCount, jobInstanceCount, jobExecCount);

        return jobExecCount;
    }
}
