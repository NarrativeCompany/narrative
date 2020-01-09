package org.narrative.reputation.batch.historyrollup

import groovy.util.logging.Slf4j
import org.narrative.batch.model.BatchJobControlEntity
import org.narrative.batch.repository.BatchJobControlRepository
import org.narrative.batch.util.BatchJobHelper
import org.narrative.reputation.BaseContainerizedMySQLApplicationSpec
import org.narrative.reputation.config.batch.HistoryRollupJobConfig
import org.narrative.reputation.model.entity.*
import org.narrative.reputation.repository.*
import org.narrative.reputation.util.datagen.ContentQualGen
import org.narrative.reputation.util.datagen.CurRepGen
import org.narrative.shared.testsupport.LogSuppressor
import org.spockframework.spring.SpringBean
import org.spockframework.spring.SpringSpy
import org.spockframework.spring.UnwrapAopProxy
import org.springframework.batch.core.Job
import org.springframework.batch.core.launch.support.SimpleJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.AbstractStep
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Stepwise
import test.util.TestCategory

import javax.persistence.EntityManager
import java.time.*
import java.time.temporal.ChronoUnit

@Stepwise
@IgnoreIf({ TestCategory.isIntegContainerizedTestsDisabled()})
@Slf4j
@ActiveProfiles ( profiles = ['default','local','containerInteg', 'liquibase', 'history'] )
class HistoryRollupTaskletSpec extends BaseContainerizedMySQLApplicationSpec {
    // Replace job launcher - this will create a job launcher with a synchronous executor
    @SpringBean
    SimpleJobLauncher jobLauncher = new SimpleJobLauncher();

    @Autowired
    JobRepository jobRepository
    @Autowired
    @Qualifier("historyRollupJob")
    Job historyRollupJob

    @SpringSpy
    @UnwrapAopProxy
    DailyHistoryRollupTasklet dailyHistoryRollupTasklet
    @SpringSpy
    @UnwrapAopProxy
    DailyHistoryShouldExecuteDecider dailyHistoryShouldExecuteDecider
    @SpringSpy
    @UnwrapAopProxy
    WeeklyHistoryRollupTasklet weeklyHistoryRollupTasklet
    @SpringSpy
    @UnwrapAopProxy
    WeeklyHistoryShouldExecuteDecider weeklyHistoryShouldExecuteDecider
    @SpringSpy
    @UnwrapAopProxy
    MonthlyHistoryRollupTasklet monthlyHistoryRollupTasklet
    @SpringSpy
    @UnwrapAopProxy
    MonthlyHistoryShouldExecuteDecider monthlyHistoryShouldExecuteDecider

    @Autowired
    TransactionTemplate txTemplate
    @Autowired
    CurrentReputationRepository currentReputationRepository
    @Autowired
    ContentQualityRepository contentQualityRepository
    @Autowired
    ReputationHistoryRepository historyRepository
    @Autowired
    ReputationHistoryTestRepository historyTestRepository
    @Autowired
    EntityManager entityManager
    @Autowired
    BatchJobControlRepository batchJobControlRepository

    @Shared
    genSize = 100
    @Shared
    repUsers = CurRepGen.buildCurrentRepList(genSize, 1)
    @Shared
    def contentList = ContentQualGen.buildContentQualList(genSize, 1)
    @Shared
    Map<Long, TestData> testDataMap = new HashMap<>()

    LocalDate nowDate
    Instant nowInstant

    BatchJobHelper batchJobHelper = new BatchJobHelper(null, jobLauncher)

    def setup() {
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.afterPropertiesSet()

        log.info("Creating test data")

        // Generate members
        txTemplate.execute({ TransactionStatus status ->
            currentReputationRepository.saveAll(repUsers)
            // Build some rep pieces - missing pieces will get zero values
            contentQualityRepository.saveAll(contentList)
            null
        })

        for (int i=0; i< genSize; i++) {
            def repUser = repUsers.get(i)
            testDataMap.put(repUser.userOid, new TestData(
                    repUser,
                    contentList.get(i),
            ))
        }

        log.info("Finished creating test data")
    }

    def cleanupDB() {
        currentReputationRepository.deleteAll()
        contentQualityRepository.deleteAll()
        historyRepository.deleteAll()
        batchJobControlRepository.deleteAll()
    }

    def mockNowMethods = {
        1 * dailyHistoryShouldExecuteDecider.getNow() >> nowInstant
        1 * weeklyHistoryShouldExecuteDecider.getNow() >> nowInstant
        1 * monthlyHistoryShouldExecuteDecider.getNow() >> nowInstant
    }

    def setNowDate(int year, Month month, int dayOfMonth) {
        LocalDateTime localDateTime = LocalDateTime.of(year, month, dayOfMonth, 12, 20)
        nowDate = localDateTime.toLocalDate()
        nowInstant = ZonedDateTime.of(localDateTime, ZoneId.of("America/New_York")).toInstant()
    }

    def compareTestDataEntity(ReputationHistoryEntity hist, TestData td) {
        assert hist.commentLikePoints == td.contentQualityEntity.commentLikePoints
        assert hist.commentDislikePoints == td.contentQualityEntity.commentDislikePoints
        assert hist.contentLikePoints == td.contentQualityEntity.contentLikePoints
        assert hist.contentDislikePoints == td.contentQualityEntity.contentDislikePoints
        assert hist.contentRatingsReceivedCount == td.contentQualityEntity.contentRatingsReceivedCount

        assert hist.kycVerified == td.currentReputationEntity.kycVerified
        assert hist.qualityAnalysis == td.currentReputationEntity.qualityAnalysis
        assert hist.totalScore == td.currentReputationEntity.totalScore
    }

    def compareToTestData(List<ReputationHistoryEntity> entities) {
        for (ReputationHistoryEntity hist: entities) {
            TestData td = testDataMap.get(hist.userOid)
            compareTestDataEntity(hist, td)
        }
        true
    }

    def "Test daily job only" () {
        given:
            setNowDate(2018, Month.DECEMBER, 29)
        when:
            batchJobHelper.launchJob(historyRollupJob, log)
            def entities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.DAILY, nowDate.minusDays(1));
            log.info("Count in history: {}", entities.size())
        then:
            interaction mockNowMethods
            1 * dailyHistoryRollupTasklet.getNow() >> nowDate
            11 * dailyHistoryRollupTasklet.execute(_, _)
            0 * weeklyHistoryRollupTasklet.execute(_, _)
            0 * monthlyHistoryRollupTasklet.execute(_, _)
            entities.size() == genSize
            compareToTestData(entities)
        cleanup:
            historyRepository.deleteAll()
            batchJobControlRepository.deleteAll()
    }

    def "Test daily and weekly" () {
        given:
            setNowDate(2018, Month.DECEMBER, 9)
        when:
            batchJobHelper.launchJob(historyRollupJob, log)
            long count = historyRepository.count();
            log.info("Count in history: {}", count)
            def dailyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.DAILY, nowDate.minusDays(1));
            def weeklyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.WEEKLY, nowDate.minusDays(1));
        then:
            interaction mockNowMethods
            1 * dailyHistoryRollupTasklet.getNow() >> nowDate
            1 * weeklyHistoryRollupTasklet.getNow() >> nowDate
            11 * dailyHistoryRollupTasklet.execute(_, _)
            11 * weeklyHistoryRollupTasklet.execute(_, _)
            0 * monthlyHistoryRollupTasklet.execute(_, _)
            count == genSize * 2
            compareToTestData(dailyEntities)
            compareToTestData(weeklyEntities)
        cleanup:
            batchJobControlRepository.deleteAll()
    }

    def "Test daily and monthly" () {
        given:
            // First day of the month not on the first day of the week
            setNowDate(2019, Month.JANUARY, 1)
        when:
            batchJobHelper.launchJob(historyRollupJob, log)
            long count = historyRepository.count();
            log.info("Count in history: {}", count)
            def dailyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.DAILY, nowDate.minusDays(1));
            def monthlyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.MONTHLY, nowDate);
        then:
            interaction mockNowMethods
            1 * dailyHistoryRollupTasklet.getNow() >> nowDate
            1 * monthlyHistoryRollupTasklet.getNow() >> nowDate
            11 * dailyHistoryRollupTasklet.execute(_, _)
            0 * weeklyHistoryRollupTasklet.execute(_, _)
            11 * monthlyHistoryRollupTasklet.execute(_, _)
            count == genSize * 4
            compareToTestData(dailyEntities)
            compareToTestData(monthlyEntities)
        cleanup:
            historyRepository.deleteAll()
            batchJobControlRepository.deleteAll()
    }

    def "Test daily, weekly and monthly" () {
        given:
            // First day of month on the first day of the week
            setNowDate(2019, Month.SEPTEMBER, 1)
        when:
            batchJobHelper.launchJob(historyRollupJob, log)
            long count = historyRepository.count();
            log.info("Count in history: {}", count)
            def dailyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.DAILY, nowDate.minusDays(1));
            def weeklyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.WEEKLY, nowDate.minusDays(1));
            def monthlyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.MONTHLY, nowDate);
        then:
            interaction mockNowMethods
            1 * dailyHistoryRollupTasklet.getNow() >> nowDate
            1 * weeklyHistoryRollupTasklet.getNow() >> nowDate
            1 * monthlyHistoryRollupTasklet.getNow() >> nowDate
            11 * dailyHistoryRollupTasklet.execute(_, _)
            11 * weeklyHistoryRollupTasklet.execute(_, _)
            11 * monthlyHistoryRollupTasklet.execute(_, _)
            count == genSize * 3
            compareToTestData(dailyEntities)
            compareToTestData(weeklyEntities)
            compareToTestData(monthlyEntities)
        cleanup:
            historyRepository.deleteAll()
            batchJobControlRepository.deleteAll()
    }

    def "Test daily, weekly and monthly job step failure recovery" () {
        given:
            // First day of month on the first day of the week
            setNowDate(2019, Month.SEPTEMBER, 1)
            def logSuppressor = new LogSuppressor()

        // Fail on daily
        when:
            logSuppressor.suppressLogs(HistoryRollupJobConfig, AbstractStep)
            batchJobHelper.launchJob(historyRollupJob, log)
            logSuppressor.resumeLogs(HistoryRollupJobConfig, AbstractStep)

            long count = historyRepository.count();
            log.info("Count in history: {}", count)
        then:
            1 * dailyHistoryShouldExecuteDecider.getNow() >> nowInstant
            1 * dailyHistoryRollupTasklet.getNow() >> nowDate
            2 * dailyHistoryRollupTasklet.execute(_, _)
            1 * dailyHistoryRollupTasklet.execute(_, _) >> {
                throw new RuntimeException("Job failure daily!")
            }
            count == (int) (genSize / 10 * 2)

        // Recovery for daily - fail weekly
        when:
            pushHistoryControlRecordIntoPast()

            logSuppressor.suppressLogs(HistoryRollupJobConfig, AbstractStep)
            batchJobHelper.launchJob(historyRollupJob, log)
            logSuppressor.resumeLogs(HistoryRollupJobConfig, AbstractStep)

            count = historyRepository.count();
            log.info("Count in history: {}", count)
            def dailyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.DAILY, nowDate.minusDays(1));
        then: "Check recovery for daily"
            1 * dailyHistoryShouldExecuteDecider.getNow() >> nowInstant
            1 * weeklyHistoryShouldExecuteDecider.getNow() >> nowInstant
            1 * dailyHistoryRollupTasklet.getNow() >> nowDate
            1 * weeklyHistoryRollupTasklet.getNow() >> nowDate
            9 * dailyHistoryRollupTasklet.execute(_, _)
            3 * weeklyHistoryRollupTasklet.execute(_, _)
            1 * weeklyHistoryRollupTasklet.execute(_, _) >> {
                throw new RuntimeException("Job failure weekly!")
            }
            count == genSize + (int) (genSize / 10 * 3)
            compareToTestData(dailyEntities)

        // Recovery for weekly - fail monthly
        when:
            pushHistoryControlRecordIntoPast()

            logSuppressor.suppressLogs(HistoryRollupJobConfig, AbstractStep)
            batchJobHelper.launchJob(historyRollupJob, log)
            logSuppressor.resumeLogs(HistoryRollupJobConfig, AbstractStep)

            count = historyRepository.count();
            log.info("Count in history: {}", count)
            dailyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.DAILY, nowDate.minusDays(1));
            def weeklyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.WEEKLY, nowDate.minusDays(1));
        then: "Check recovery for daily"
            interaction mockNowMethods
            0 * dailyHistoryRollupTasklet.getNow() >> nowDate
            2 * weeklyHistoryRollupTasklet.getNow() >> nowDate
            1 * monthlyHistoryRollupTasklet.getNow() >> nowDate
            0 * dailyHistoryRollupTasklet.execute(_, _)
            8 * weeklyHistoryRollupTasklet.execute(_, _)
            5 * monthlyHistoryRollupTasklet.execute(_, _)
            1 * monthlyHistoryRollupTasklet.execute(_, _) >> {
                throw new RuntimeException("Job failure monthly!")
            }
            count == genSize * 2 + + (int) (genSize / 10 * 5)
            compareToTestData(dailyEntities)
            compareToTestData(weeklyEntities)

        // Recovery for monthly
        when:
            pushHistoryControlRecordIntoPast()

            batchJobHelper.launchJob(historyRollupJob, log)

            count = historyRepository.count();
            log.info("Count in history: {}", count)
            dailyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.DAILY, nowDate.minusDays(1));
            weeklyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.WEEKLY, nowDate.minusDays(1));
            def monthlyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.MONTHLY, nowDate);
        then: "Check recovery for daily"
            interaction mockNowMethods
            0 * dailyHistoryRollupTasklet.getNow() >> nowDate
            1 * weeklyHistoryRollupTasklet.getNow() >> nowDate
            2 * monthlyHistoryRollupTasklet.getNow() >> nowDate
            0 * dailyHistoryRollupTasklet.execute(_, _)
            0 * weeklyHistoryRollupTasklet.execute(_, _)
            6 * monthlyHistoryRollupTasklet.execute(_, _)
            count == genSize * 3
            compareToTestData(dailyEntities)
            compareToTestData(weeklyEntities)
            compareToTestData(monthlyEntities)

        cleanup:
            batchJobControlRepository.deleteAll()
    }

    def "Test no job should run - already ran" () {
        given:
            // First day of month on the first day of the week
            setNowDate(2019, Month.SEPTEMBER, 1)
        when:
            batchJobHelper.launchJob(historyRollupJob, log)
        then:
            interaction mockNowMethods
            0 * dailyHistoryRollupTasklet.getNow() >> nowDate
            1 * weeklyHistoryRollupTasklet.getNow() >> nowDate
            1 * monthlyHistoryRollupTasklet.getNow() >> nowDate
            0 * dailyHistoryRollupTasklet.execute(_, _)
            0 * weeklyHistoryRollupTasklet.execute(_, _)
            0 * monthlyHistoryRollupTasklet.execute(_, _)
        cleanup:
            //Last test in this sequence - clean up.  We can't do this in a cleanupSpec since it doesn't have access
            //to beans
            batchJobControlRepository.deleteAll()
            historyRepository.deleteAll()
    }

    def "Test simulate daily step server shutdown recovery" () {
        given:
            // First day of month on the first day of the week
            setNowDate(2019, Month.SEPTEMBER, 1)

        when:
            batchJobHelper.launchJob(historyRollupJob, log)
            long count = historyRepository.count();
            log.info("Count in history: {}", count)
        then:
            interaction mockNowMethods
            1 * dailyHistoryRollupTasklet.getNow() >> nowDate
            // Stop iterating after the pre-set amount
            4 * dailyHistoryRollupTasklet.performHistoryBatchInsert(_, _, _)
            1 * dailyHistoryRollupTasklet.performHistoryBatchInsert(_, _, _) >> {
                0
            }
            _ * weeklyHistoryRollupTasklet.performHistoryBatchInsert(_, _, _) >> {
                0
            }
            _ * monthlyHistoryRollupTasklet.performHistoryBatchInsert(_, _, _) >> {
                0
            }
            5 * dailyHistoryRollupTasklet.execute(_, _)

            count == (int) (genSize / 10 * 4)

        when:
            pushHistoryControlRecordIntoPast()
            batchJobHelper.launchJob(historyRollupJob, log)
            count = historyRepository.count();
            log.info("Count in history: {}", count)
            def dailyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.DAILY, nowDate.minusDays(1));
            def weeklyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.WEEKLY, nowDate.minusDays(1));
            def monthlyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.MONTHLY, nowDate);
        then:
            interaction mockNowMethods
            1 * dailyHistoryRollupTasklet.getNow() >> nowDate
            1 * weeklyHistoryRollupTasklet.getNow() >> nowDate
            1 * monthlyHistoryRollupTasklet.getNow() >> nowDate
            7 * dailyHistoryRollupTasklet.execute(_, _)
            11 * weeklyHistoryRollupTasklet.execute(_, _)
            11 * monthlyHistoryRollupTasklet.execute(_, _)
            count == genSize * 3
            compareToTestData(dailyEntities)
            compareToTestData(weeklyEntities)
            compareToTestData(monthlyEntities)
        cleanup:
            historyRepository.deleteAll()
            batchJobControlRepository.deleteAll()
    }

    def "Test simulate weekly step server shutdown recovery" () {
        given:
            // First day of month on the first day of the week
            setNowDate(2019, Month.SEPTEMBER, 1)

        when:
            batchJobHelper.launchJob(historyRollupJob, log)
            long count = historyRepository.count();
            log.info("Count in history: {}", count)
        then:
            interaction mockNowMethods
            1 * dailyHistoryRollupTasklet.getNow() >> nowDate
            1 * weeklyHistoryRollupTasklet.getNow() >> nowDate
            // Stop iterating after the pre-set amount
            6 * weeklyHistoryRollupTasklet.performHistoryBatchInsert(_, _, _)
            _ * weeklyHistoryRollupTasklet.performHistoryBatchInsert(_, _, _) >> {
                0
            }
            _ * monthlyHistoryRollupTasklet.performHistoryBatchInsert(_, _, _) >> {
                0
            }
            11 * dailyHistoryRollupTasklet.execute(_, _)
            7 * weeklyHistoryRollupTasklet.execute(_, _)

            count == genSize + (int) (genSize / 10 * 6)

        when:
            pushHistoryControlRecordIntoPast()
            batchJobHelper.launchJob(historyRollupJob, log)
            count = historyRepository.count();
            log.info("Count in history: {}", count)
            def dailyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.DAILY, nowDate.minusDays(1));
            def weeklyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.WEEKLY, nowDate.minusDays(1));
            def monthlyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.MONTHLY, nowDate);
        then:
            interaction mockNowMethods
            0 * dailyHistoryRollupTasklet.getNow() >> nowDate
            2 * weeklyHistoryRollupTasklet.getNow() >> nowDate
            1 * monthlyHistoryRollupTasklet.getNow() >> nowDate
            0 * dailyHistoryRollupTasklet.execute(_, _)
            5 * weeklyHistoryRollupTasklet.execute(_, _)
            11 * monthlyHistoryRollupTasklet.execute(_, _)
            count == genSize * 3
            compareToTestData(dailyEntities)
            compareToTestData(weeklyEntities)
            compareToTestData(monthlyEntities)
        cleanup:
            historyRepository.deleteAll()
            batchJobControlRepository.deleteAll()
    }

    def "Test simulate monthly step server shutdown recovery" () {
        given:
            // First day of month on the first day of the week
            setNowDate(2019, Month.SEPTEMBER, 1)

        when:
            batchJobHelper.launchJob(historyRollupJob, log)
            long count = historyRepository.count();
            log.info("Count in history: {}", count)
        then:
            interaction mockNowMethods
            1 * dailyHistoryRollupTasklet.getNow() >> nowDate
            1 * weeklyHistoryRollupTasklet.getNow() >> nowDate
            1 * monthlyHistoryRollupTasklet.getNow() >> nowDate
            // Stop iterating after the pre-set amount
            8 * monthlyHistoryRollupTasklet.performHistoryBatchInsert(_, _, _)
            _ * monthlyHistoryRollupTasklet.performHistoryBatchInsert(_, _, _) >> {
                0
            }
            11 * dailyHistoryRollupTasklet.execute(_, _)
            11 * weeklyHistoryRollupTasklet.execute(_, _)
            9 * monthlyHistoryRollupTasklet.execute(_, _)

            count == genSize * 2 + (int) (genSize / 10 * 8)

        when:
            pushHistoryControlRecordIntoPast()
            batchJobHelper.launchJob(historyRollupJob, log)
            count = historyRepository.count();
            log.info("Count in history: {}", count)
            def dailyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.DAILY, nowDate.minusDays(1));
            def weeklyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.WEEKLY, nowDate.minusDays(1));
            def monthlyEntities = historyTestRepository.findAllByPeriodAndSnapshotDateOrderByUserOidAsc(RollupPeriod.MONTHLY, nowDate);
        then:
            interaction mockNowMethods
            0 * dailyHistoryRollupTasklet.getNow() >> nowDate
            1 * weeklyHistoryRollupTasklet.getNow() >> nowDate
            2 * monthlyHistoryRollupTasklet.getNow() >> nowDate
            0 * dailyHistoryRollupTasklet.execute(_, _)
            0 * weeklyHistoryRollupTasklet.execute(_, _)
            3 * monthlyHistoryRollupTasklet.execute(_, _)
            count == genSize * 3
            compareToTestData(dailyEntities)
            compareToTestData(weeklyEntities)
            compareToTestData(monthlyEntities)
        cleanup:
            historyRepository.deleteAll()
            batchJobControlRepository.deleteAll()
    }

    //Allow the job to re-execute by pushing the timestamp back for the last execution
    def pushHistoryControlRecordIntoPast() {
        txTemplate.execute({ TransactionStatus status ->
            BatchJobControlEntity bjce = batchJobControlRepository.getOne(HistoryRollupJobConfig.HISTORY_ROLLUP_JOB)
            bjce.setStartTime(bjce.getStartTime().minus(1, ChronoUnit.DAYS))
            batchJobControlRepository.save(bjce)
        })
    }

    class TestData {
        CurrentReputationEntity currentReputationEntity
        ContentQualityEntity contentQualityEntity

        TestData(CurrentReputationEntity currentReputationEntity, ContentQualityEntity contentQualityEntity) {
            this.currentReputationEntity = currentReputationEntity
            this.contentQualityEntity = contentQualityEntity
        }
    }
}
