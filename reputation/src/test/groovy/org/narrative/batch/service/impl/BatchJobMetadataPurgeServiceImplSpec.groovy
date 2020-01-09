package org.narrative.batch.service.impl


import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.RandomUtils
import org.narrative.batch.service.BatchJobMetadataPurgeService
import org.narrative.reputation.BaseIntegTestSpec
import org.narrative.reputation.service.EventManagementService
import org.redisson.api.RedissonClient
import org.spockframework.spring.SpringBean
import org.springframework.batch.core.BatchStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate

import java.time.Instant
import java.time.temporal.ChronoUnit

class BatchJobMetadataPurgeServiceImplSpec extends BaseIntegTestSpec  {
    @SpringBean
    RedissonClient redissonClient = Mock()
    @SpringBean
    EventManagementService eventManagementService = Mock()

    @Autowired
    TransactionTemplate txTemplate;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    BatchJobMetadataPurgeService tested

    Instant now = Instant.now()
    Instant purgableInstant = now.minus(15, ChronoUnit.DAYS)
    Instant purgableErrorInstant = now.minus(100, ChronoUnit.DAYS)
    int total = 46
    int purgeableFinishedCount = 15
    int finishedCount = 3
    int purgeableErrorCount = 17
    int errorCount = 11
    long maxDaysAfterPurgable = 20



    def setup() {
        execInsertSQL('BATCH_JOB_INSTANCE')
        execInsertSQL('BATCH_JOB_EXECUTION')
        execInsertSQL('BATCH_JOB_EXECUTION_PARAMS')
        execInsertSQL('BATCH_JOB_EXECUTION_CONTEXT')
        execInsertSQL('BATCH_STEP_EXECUTION')
        execInsertSQL('BATCH_STEP_EXECUTION_CONTEXT')
    }

    def cleanup() {
        cleanTable('BATCH_STEP_EXECUTION_CONTEXT')
        cleanTable('BATCH_STEP_EXECUTION')
        cleanTable('BATCH_JOB_EXECUTION_CONTEXT')
        cleanTable('BATCH_JOB_EXECUTION_PARAMS')
        cleanTable('BATCH_JOB_EXECUTION')
        cleanTable('BATCH_JOB_INSTANCE')
    }

    def execInsertSQL(String tableName) {
        List<String> sqlList = FileUtils.readLines(new File(getClass().getResource('/test-data/batch/' + tableName.toUpperCase() + '.sql').toURI()), 'UTF-8')

        boolean execTable = 'BATCH_JOB_EXECUTION'.equals(tableName)

        txTemplate.execute({ status ->

            def purgeError, purgeFinish, finish, error = 0
            BatchStatus batchStatus = BatchStatus.COMPLETED
            if (execTable) {
                purgeError = purgeableErrorCount
                purgeFinish = purgeableFinishedCount
                finish = finishedCount
                error = errorCount
            }

            for(String sql: sqlList) {
                 if (execTable) {
                    Instant start
                    if (purgeError > 0) {
                        start = purgableErrorInstant.minus(RandomUtils.nextLong(1L, maxDaysAfterPurgable), ChronoUnit.DAYS)
                        batchStatus = BatchStatus.FAILED
                        purgeError -= 1
                    } else if (purgeFinish > 0) {
                        start = purgableInstant.minus(RandomUtils.nextLong(1L, maxDaysAfterPurgable), ChronoUnit.DAYS)
                        batchStatus = BatchStatus.COMPLETED
                        purgeFinish -= 1
                    } else if (finish > 0) {
                        start = purgableInstant.plus(RandomUtils.nextLong(1L, 100L), ChronoUnit.DAYS)
                        batchStatus = BatchStatus.COMPLETED
                        finish -= 1
                    } else if (error > 0) {
                        start = purgableErrorInstant.plus(RandomUtils.nextLong(1L, 100L), ChronoUnit.DAYS)
                        batchStatus = BatchStatus.FAILED
                        error -= 1
                    }

                    jdbcTemplate.update(sql, start, batchStatus.name(), batchStatus.name())
                } else {
                    jdbcTemplate.execute(sql)
                }
            }
        })
    }

    def cleanTable(String tableName) {
        txTemplate.execute({ status ->
           jdbcTemplate.execute('delete from ' + tableName)
        })
    }

    def "test purge only non-error"() {
        when:
            def res
            txTemplate.execute({ status ->
                res = tested.purgeJobMetadata(purgableInstant, false)
            })
        then:
            res == purgeableFinishedCount
            jdbcTemplate.queryForObject('select count(1) from BATCH_JOB_EXECUTION', Long) == total - purgeableFinishedCount
    }

    def "test purge error"() {
        when:
            def res
            txTemplate.execute({ status ->
                res = tested.purgeJobMetadata(purgableErrorInstant, true)
            })
        then:
            res == purgeableErrorCount
            jdbcTemplate.queryForObject('select count(1) from BATCH_JOB_EXECUTION', Long) == total - purgeableErrorCount
    }

    def "test purge error - non inclusive"() {
        when:
            def res
            txTemplate.execute({ status ->
                res = tested.purgeJobMetadata(purgableErrorInstant.minus(maxDaysAfterPurgable + 1, ChronoUnit.DAYS), true)
            })
        then:
            res == 0
    }

    def "test purge finished - non inclusive"() {
        when:
            def res
            txTemplate.execute({ status ->
                res = tested.purgeJobMetadata(purgableInstant.minus(maxDaysAfterPurgable + 1, ChronoUnit.DAYS), false)
            })
        then:
            res == 0
    }
}
