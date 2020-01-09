package org.narrative.batch.service.impl

import groovy.util.logging.Slf4j
import org.narrative.batch.config.BatchProperties
import org.narrative.batch.model.BatchJobControlEntity
import org.narrative.batch.model.BatchJobCtlHistEntity
import org.narrative.batch.repository.BatchJobControlHistoryRepository
import org.narrative.batch.repository.BatchJobControlRepository
import org.narrative.shared.util.NetworkUtil
import org.springframework.batch.core.BatchStatus
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

@Slf4j
class BatchJobControlServiceImplSpec extends Specification {
    BatchJobControlRepository controlRepository = Mock()
    BatchJobControlHistoryRepository historyRepository = Mock()
    PlatformTransactionManager platformTransactionManager = Mock()
    TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager)
    BatchProperties batchProperties = Spy(BatchProperties)

    BatchJobControlServiceImpl tested = Spy(BatchJobControlServiceImpl, constructorArgs: [controlRepository, historyRepository, batchProperties, transactionTemplate])

    def jobName = 'someJob'

    def "test findLastJobExecutionStartInstant"() {
        when:
            def res = tested.findLastJobExecutionStartInstant(jobName)
        then:
            1 * controlRepository.findById(jobName) >> Optional.empty()
            res == Instant.EPOCH
    }

    def "test findLastJobExecutionStartInstant no entity"() {
        given:
            def entity =BatchJobControlEntity.builder().startTime(Instant.now().minusSeconds(123)).build()
        when:
            def res = tested.findLastJobExecutionStartInstant(jobName)
        then:
            1 * controlRepository.findById(jobName) >> Optional.of(entity)
            res == entity.startTime
    }

    def "test isSingletonJobExecutionCandidate no entity"() {
        when:
            def res = tested.isSingletonJobExecutionCandidate(jobName)
        then:
            1 * controlRepository.findById(jobName) >> Optional.empty()
            res
    }

    def "test isSingletonJobExecutionCandidate ownable status"() {
        when:
            def res = tested.isSingletonJobExecutionCandidate(jobName)
        then:
            1 * controlRepository.findById(jobName) >> Optional.of(entity)
            res
        where:
            entity << [
                    BatchJobControlEntity.builder().status(BatchStatus.COMPLETED).build(),
                    BatchJobControlEntity.builder().status(BatchStatus.FAILED).build()
            ]
    }

    def "test isSingletonJobExecutionCandidate timed out"() {
        given:
            def entity = BatchJobControlEntity.builder()
                    .status(BatchStatus.STARTED)
                    .startTime(Instant.now().minus(batchProperties.maxJobDuration).minus(1, ChronoUnit.MINUTES))
                    .build()
        when:
            def res = tested.isSingletonJobExecutionCandidate(jobName)
        then:
            1 * controlRepository.findById(jobName) >> Optional.of(entity)
            res
    }

    def "test isSingletonJobExecutionCandidate not timed out"() {
        given:
            def entity = BatchJobControlEntity.builder()
                    .status(BatchStatus.STARTED)
                    .startTime(Instant.now().minus(batchProperties.maxJobDuration).plus(10, ChronoUnit.MINUTES))
                    .build()
        when:
            def res = tested.isSingletonJobExecutionCandidate(jobName)
        then:
            1 * controlRepository.findById(jobName) >> Optional.of(entity)
            !res
    }

    def "test acquireOwnershipOfSingletonJob existing can own"() {
        given:
            def entity = Mock(BatchJobControlEntity)
        when:
            def res = tested.acquireOwnershipOfSingletonJob(jobName, -1, -2, -3)
        then:
            1 * controlRepository.findById(jobName) >> Optional.of(entity)
            1 * tested.isSingletonJobExecutionCandidate(jobName, entity, _) >> true
            1 * entity.setJobId(-1)
            1 * entity.setJobInstanceId(-2)
            1 * entity.setJobExecutionId(-3)
            1 * entity.setHost(NetworkUtil.getHostName())
            1 * entity.setStartTime(_)
            1 * entity.setEndTime(null)
            1 * entity.setStatus(BatchStatus.STARTED)
            1 * controlRepository.saveAndFlush(entity)
            res
    }

    def "test acquireOwnershipOfSingletonJob existing can own updated by another node"() {
        given:
            def entity = Mock(BatchJobControlEntity)
        when:
            def res = tested.acquireOwnershipOfSingletonJob(jobName, -1, -2, -3)
        then:
            1 * controlRepository.findById(jobName) >> Optional.of(entity)
            1 * tested.isSingletonJobExecutionCandidate(jobName, entity, _) >> true
            1 * entity.setJobId(-1)
            1 * entity.setJobInstanceId(-2)
            1 * entity.setJobExecutionId(-3)
            1 * entity.setHost(NetworkUtil.getHostName())
            1 * entity.setStartTime(_)
            1 * entity.setEndTime(null)
            1 * entity.setStatus(BatchStatus.STARTED)
            1 * controlRepository.saveAndFlush(entity) >> {throw new ObjectOptimisticLockingFailureException('', null)}
            !res
    }

    def "test acquireOwnershipOfSingletonJob new job can own"() {
        given:
            def entity = BatchJobControlEntity.builder()
                    .jobName(jobName)
                    .host(NetworkUtil.getHostName())
                    .startTime(Instant.now())
                    .status(BatchStatus.STARTED)
                    .build();
        when:
            def res = tested.acquireOwnershipOfSingletonJob(jobName, -1, -2, -3)
        then:
            1 * controlRepository.findById(jobName) >> Optional.empty()
            1 * controlRepository.saveAndFlush(_) >> {args ->
                BatchJobControlEntity param = args[0]
                assert param.jobName == jobName
                assert param.host == NetworkUtil.hostName
                assert param.startTime != null
                assert param.startTime.isBefore(Instant.now())
                assert param.status.equals(BatchStatus.STARTED)
            }
            res
    }

    def "test acquireOwnershipOfSingletonJob new job created by another node"() {
        when:
            def res = tested.acquireOwnershipOfSingletonJob(jobName, -1, -2, -3)
        then:
            1 * controlRepository.findById(jobName) >> Optional.empty()
            1 * controlRepository.saveAndFlush(_) >> {throw new ObjectOptimisticLockingFailureException('', null)}
            !res
    }

    def "test completeOwnedSingletonJob"() {
        given:
            def entity = BatchJobControlEntity.builder()
                    .jobName(jobName)
                    .jobId(-1)
                    .jobInstanceId(-2)
                    .jobExecutionId(-3)
                    .host(NetworkUtil.hostName)
                    .status(BatchStatus.STARTED)
                    .startTime(Instant.now().minus(12, ChronoUnit.MINUTES))
                    .build()
            BatchJobControlEntity saved
        when:
            tested.completeOwnedSingletonJob(jobName, BatchStatus.COMPLETED)
        then:
            controlRepository.getOne(jobName) >> entity
            controlRepository.save(_) >> {args ->
                BatchJobControlEntity param = args[0]
                saved = param
                assert param.host == NetworkUtil.hostName
                assert param.endTime.isAfter(param.startTime)
                assert param.endTime.isBefore(Instant.now().plusSeconds(10))
                assert param.jobName == jobName
                assert param.status == BatchStatus.COMPLETED
            }
            historyRepository.save(_) >> { args ->
                BatchJobCtlHistEntity param = args[0]
                assert param.host == saved.host
                assert param.endTime == saved.endTime
                assert param.jobName == jobName
                assert param.jobId == saved.jobId
                assert param.jobInstanceId == saved.jobInstanceId
                assert param.jobExecutionId == saved.jobExecutionId
                assert param.status == BatchStatus.COMPLETED
            }
            historyRepository.deleteByEndTimeBefore(_) >> { args ->
                assert (args[0] as Instant).isBefore(Instant.now().minus(batchProperties.getJobHistoryRetention()).plus(1, ChronoUnit.MINUTES))
            }
    }
}
