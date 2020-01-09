package org.narrative.reputation.service.impl

import org.apache.commons.lang3.RandomUtils
import org.narrative.batch.model.BatchJobCtlHistEntity
import org.narrative.batch.repository.BatchJobControlHistoryRepository
import org.narrative.batch.service.BatchJobMetadataPurgeService
import org.narrative.reputation.BaseIntegTestSpec
import org.narrative.reputation.config.ReputationProperties
import org.narrative.reputation.model.entity.ConductStatusEntity
import org.narrative.reputation.model.entity.EventDedupEntity
import org.narrative.reputation.repository.ConductStatusRepository
import org.narrative.reputation.repository.EventDedupRepository
import org.narrative.reputation.service.EphemeralDataCleanupService
import org.narrative.reputation.service.EventManagementService
import org.narrative.shared.event.reputation.ConductEventType
import org.redisson.api.RedissonClient
import org.spockframework.spring.SpringBean
import org.spockframework.spring.SpringSpy
import org.spockframework.spring.UnwrapAopProxy
import org.springframework.batch.core.BatchStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Transactional
class EphemeralDataCleanupServiceImplSpec extends BaseIntegTestSpec {
    @SpringBean
    RedissonClient redissonClient = Mock()
    @SpringBean
    EventManagementService eventManagementService = Mock()

    @Autowired
    EventDedupRepository eventDedupRepository
    @Autowired
    ConductStatusRepository conductStatusRepository
    @Autowired
    BatchJobControlHistoryRepository batchJobControlHistoryRepository
    @SpringSpy
    @UnwrapAopProxy
    EphemeralDataCleanupService tested
    @SpringBean
    BatchJobMetadataPurgeService batchJobMetadataPurgeService = Mock()
    @SpringSpy
    @UnwrapAopProxy
    ReputationProperties reputationProperties

    EventDedupEntity ent1
    EventDedupEntity ent2
    EventDedupEntity ent3
    EventDedupEntity ent4
    EventDedupEntity ent5
    EventDedupEntity ent6

    BatchJobCtlHistEntity hist1;
    BatchJobCtlHistEntity hist2;
    BatchJobCtlHistEntity hist3;
    BatchJobCtlHistEntity hist4;

    def createDedupData() {
        Instant now = Instant.now()

        ent1 = createSaveDedup(now.minus(3, ChronoUnit.HOURS), true)
        ent2 = createSaveDedup(now.minus(3, ChronoUnit.HOURS), false)
        ent3 = createSaveDedup(now.minus(6, ChronoUnit.HOURS), true)
        ent4 = createSaveDedup(now.minus(6, ChronoUnit.HOURS), false)
        ent5 = createSaveDedup(now.minus(11, ChronoUnit.HOURS), true)
        ent6 = createSaveDedup(now.minus(11, ChronoUnit.HOURS), false)
    }

    def createSaveDedup(Instant lockInstant, boolean processed) {
        EventDedupEntity ent = EventDedupEntity.builder().uuid(UUID.randomUUID()).lockTimestamp(lockInstant).build()
        ent.setProcessed(processed)
        if (processed) {
            ent.setProcessedTimestamp(lockInstant)
        }
        eventDedupRepository.save(ent)
        return ent;
    }

    def findEnt(EventDedupEntity ent) {
        eventDedupRepository.findById(ent.uuid).orElse(null)
    }

    def "test purgeEventDedupTable purge mixed"() {
        given:
            reputationProperties.dataRetention.eventDedup.setRetentionDuration(Duration.ofHours(5))
            reputationProperties.dataRetention.eventDedup.setErroredRetentionDuration(Duration.ofHours(10))
            createDedupData()
        when:
            tested.purgeEventDedupTable()
        then:
            findEnt(ent1) == ent1
            findEnt(ent2) == ent2
            findEnt(ent3) == null
            findEnt(ent4) == ent4
            findEnt(ent5) == null
            findEnt(ent6) == null
        cleanup:
            eventDedupRepository.deleteAll()
    }

    def "test purgeEventDedupTable purge all older than one hour no errored"() {
        given:
            reputationProperties.dataRetention.eventDedup.setRetentionDuration(Duration.ofHours(1))
            reputationProperties.dataRetention.eventDedup.setErroredRetentionDuration(Duration.ofHours(20))
            createDedupData()
        when:
            tested.purgeEventDedupTable()
        then:
            findEnt(ent1) == null
            findEnt(ent2) == ent2
            findEnt(ent3) == null
            findEnt(ent4) == ent4
            findEnt(ent5) == null
            findEnt(ent6) == ent6
        cleanup:
            eventDedupRepository.deleteAll()
    }

    def "test purgeEventDedupTable purge all older than one hour"() {
        given:
            reputationProperties.dataRetention.eventDedup.setRetentionDuration(Duration.ofHours(1))
            reputationProperties.dataRetention.eventDedup.setErroredRetentionDuration(Duration.ofHours(1))
            createDedupData()
        when:
            tested.purgeEventDedupTable()
        then:
            findEnt(ent1) == null
            findEnt(ent2) == null
            findEnt(ent3) == null
            findEnt(ent4) == null
            findEnt(ent5) == null
            findEnt(ent6) == null
        cleanup:
            eventDedupRepository.deleteAll()
    }

    def "test purgeEventDedupTable purge none"() {
        given:
            reputationProperties.dataRetention.eventDedup.setRetentionDuration(Duration.ofHours(100))
            reputationProperties.dataRetention.eventDedup.setErroredRetentionDuration(Duration.ofHours(100))
            createDedupData()
        when:
            tested.purgeEventDedupTable()
        then:
            findEnt(ent1) == ent1
            findEnt(ent2) == ent2
            findEnt(ent3) == ent3
            findEnt(ent4) == ent4
            findEnt(ent5) == ent5
            findEnt(ent6) == ent6
        cleanup:
            eventDedupRepository.deleteAll()
    }

    def createSaveHistData() {
        Instant now = Instant.now()
        hist1 = BatchJobCtlHistEntity.builder()
                .id(
                BatchJobCtlHistEntity.BatchJobCtlHistId.builder()
                        .jobName('test1')
                        .jobId(1)
                        .jobInstanceId(1)
                        .jobExecutionId(1)
                        .build())
                .startTime(now.minus(2, ChronoUnit.DAYS))
                .endTime(now.minus(2, ChronoUnit.DAYS))
                .status(BatchStatus.COMPLETED)
                .host('localhost')
                .build()
        hist2 = BatchJobCtlHistEntity.builder()
                .id(
                BatchJobCtlHistEntity.BatchJobCtlHistId.builder()
                        .jobName('test1')
                        .jobId(1)
                        .jobInstanceId(2)
                        .jobExecutionId(2)
                        .build())
                .startTime(now.minus(2, ChronoUnit.DAYS))
                .endTime(now.minus(2, ChronoUnit.DAYS))
                .status(BatchStatus.FAILED)
                .host('localhost')
                .build()
        hist3 = BatchJobCtlHistEntity.builder()
                .id(
                BatchJobCtlHistEntity.BatchJobCtlHistId.builder()
                        .jobName('test2')
                        .jobId(1)
                        .jobInstanceId(1)
                        .jobExecutionId(1)
                        .build())
                .startTime(now.minus(5, ChronoUnit.DAYS))
                .endTime(now.minus(5, ChronoUnit.DAYS))
                .status(BatchStatus.COMPLETED)
                .host('localhost')
                .build()
        hist4 = BatchJobCtlHistEntity.builder()
                .id(
                BatchJobCtlHistEntity.BatchJobCtlHistId.builder()
                        .jobName('test2')
                        .jobId(1)
                        .jobInstanceId(2)
                        .jobExecutionId(2)
                        .build())
                .startTime(now.minus(5, ChronoUnit.DAYS))
                .endTime(now.minus(5, ChronoUnit.DAYS))
                .status(BatchStatus.FAILED)
                .host('localhost')
                .build()

        batchJobControlHistoryRepository.saveAll([hist1, hist2, hist3, hist4])
    }

    def findBHEnt(BatchJobCtlHistEntity ent) {
        batchJobControlHistoryRepository.findById(ent.getId()).orElse(null)
    }

    def "test purgeBatchJobControlHistoryTable purge mixed"() {
        given:
            reputationProperties.dataRetention.batchJobControlHistory.setRetentionDuration(Duration.ofDays(1))
            reputationProperties.dataRetention.batchJobControlHistory.setErroredRetentionDuration(Duration.ofDays(3))
            createSaveHistData()
        when:
            tested.purgeBatchJobControlHistoryTable()
        then:
            findBHEnt(hist1) == null
            findBHEnt(hist2) == hist2
            findBHEnt(hist3) == null
            findBHEnt(hist4) == null
        cleanup:
            batchJobControlHistoryRepository.deleteAll()
    }

    def "test purgeBatchJobControlHistoryTable purge only finished"() {
        given:
            reputationProperties.dataRetention.batchJobControlHistory.setRetentionDuration(Duration.ofDays(1))
            reputationProperties.dataRetention.batchJobControlHistory.setErroredRetentionDuration(Duration.ofDays(10))
            createSaveHistData()
        when:
            tested.purgeBatchJobControlHistoryTable()
        then:
            findBHEnt(hist1) == null
            findBHEnt(hist2) == hist2
            findBHEnt(hist3) == null
            findBHEnt(hist4) == hist4
        cleanup:
            batchJobControlHistoryRepository.deleteAll()
    }

    def "test purgeBatchJobControlHistoryTable purge all"() {
        given:
            reputationProperties.dataRetention.batchJobControlHistory.setRetentionDuration(Duration.ofDays(1))
            reputationProperties.dataRetention.batchJobControlHistory.setErroredRetentionDuration(Duration.ofDays(1))
            createSaveHistData()
        when:
            tested.purgeBatchJobControlHistoryTable()
        then:
            findBHEnt(hist1) == null
            findBHEnt(hist2) == null
            findBHEnt(hist3) == null
            findBHEnt(hist4) == null
        cleanup:
            batchJobControlHistoryRepository.deleteAll()
    }


    def "test purgeBatchJobControlHistoryTable purge none"() {
        given:
            reputationProperties.dataRetention.batchJobControlHistory.setRetentionDuration(Duration.ofDays(30))
            reputationProperties.dataRetention.batchJobControlHistory.setErroredRetentionDuration(Duration.ofDays(30))
            createSaveHistData()
        when:
            tested.purgeBatchJobControlHistoryTable()
        then:
            findBHEnt(hist1) == hist1
            findBHEnt(hist2) == hist2
            findBHEnt(hist3) == hist3
            findBHEnt(hist4) == hist4
        cleanup:
            batchJobControlHistoryRepository.deleteAll()
    }

    def "test purgeSpringBatchJobData"() {
        given:
            def now = Instant.now()
        when:
            tested.purgeSpringBatchJobData()
        then:
            1 * tested.getNow() >> now
            1 * batchJobMetadataPurgeService.purgeJobMetadata(_, false)
            1 * batchJobMetadataPurgeService.purgeJobMetadata(_, true)
    }

    def "test purge old conduct status events"() {
        given:
            def now = Instant.now()

            // create 10 events, 3 before 180 days from now
            10.times {
                ConductStatusEntity entity = ConductStatusEntity.builder()
                        .eventTimestamp(now.minus(it*2 + 175, ChronoUnit.DAYS))
                        .eventId(UUID.randomUUID())
                        .userOid(RandomUtils.nextLong())
                        .conductEventType(ConductEventType.FAILURE_TO_PAY_FOR_NICHE)
                        .build()
                conductStatusRepository.save(entity)
            }
        when:
            tested.purgeOldConductStatusEvents()

        then:
            // There should be 3 events left after the purge
            assert conductStatusRepository.count() == 3
    }
}
