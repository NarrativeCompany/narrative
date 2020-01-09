package org.narrative.batch.service.impl

import org.narrative.batch.model.BatchJobControlEntity
import org.narrative.batch.repository.BatchJobControlRepository
import org.narrative.batch.service.BatchJobControlService
import org.narrative.reputation.BaseIntegTestSpec
import org.narrative.reputation.service.EventManagementService
import org.narrative.shared.util.NetworkUtil
import org.redisson.api.RedissonClient
import org.spockframework.spring.SpringBean
import org.spockframework.spring.SpringSpy
import org.spockframework.spring.UnwrapAopProxy
import org.springframework.batch.core.BatchStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate

import javax.persistence.EntityManager
import java.time.Instant
import java.time.temporal.ChronoUnit

@Transactional
class BatchJobControlServiceImplIntegSpec extends BaseIntegTestSpec {
    @SpringBean
    RedissonClient redissonClient = Mock()
    @SpringBean
    EventManagementService eventManagementService = Mock()
    @Autowired
    BatchJobControlRepository controlRepository
    @Autowired
    EntityManager entityManager;
    @Autowired
    TransactionTemplate txTemplate;
    @SpringSpy
    @UnwrapAopProxy
    BatchJobControlService tested

    def jobName1 = 'someJob1'
    def jobName2 = 'someJob2'

    def "test acquireOwnershipOfSingletonJob new can own"() {
        when:
            def res1 = tested.acquireOwnershipOfSingletonJob(jobName1, -1, -2, -3)
            def res2 = tested.acquireOwnershipOfSingletonJob(jobName2, -1, -2, -3)
        then:
            res1
            res2
    }

    def "test acquireOwnershipOfSingletonJob new owned by another node"() {
        given:
            def entity1_complete = BatchJobControlEntity.builder()
                    .jobName(jobName1)
                    .host(NetworkUtil.getHostName())
                    .startTime(Instant.now())
                    .status(BatchStatus.COMPLETED)
                    .version(1L)
                    .build();
        when:
            def res = tested.acquireOwnershipOfSingletonJob(jobName1, -1, -2, -3)
        then:
            1 * tested.buildNewControlEntity(_, _, _, _, _) >> {
                txTemplate.execute(new TransactionCallback<Void>() {
                    @Override
                    Void doInTransaction(TransactionStatus status) {
                        entityManager.createNativeQuery('insert into BatchJobControl(jobName, jobId, jobInstanceId, jobExecutionId, status, version, host, startTime) values (\'' + jobName1 +'\', -1, -2, -4, \'STARTED\', 1, \'somehost\', CURRENT_TIMESTAMP)').executeUpdate()
                        null
                    }
                })
                def realRes = callRealMethod()
                return  realRes
            }
            !res
    }

    def "test acquireOwnershipOfSingletonJob existing can own"() {
        given:
            def entity1 = BatchJobControlEntity.builder()
                .jobName(jobName1)
                .jobId(-1)
                .jobInstanceId(-2)
                .jobExecutionId(-3)
                .host(NetworkUtil.getHostName())
                .startTime(Instant.now())
                .status(BatchStatus.COMPLETED)
                .build();
            def entity2 = BatchJobControlEntity.builder()
                .jobName(jobName2)
                    .jobId(-3)
                    .jobInstanceId(-4)
                    .jobExecutionId(-5)
                .host(NetworkUtil.getHostName())
                .startTime(Instant.now().minus(10, ChronoUnit.DAYS))
                .status(BatchStatus.STARTED)
                .build();
        when:
            txTemplate.execute(new TransactionCallback<Void>() {
                @Override
                Void doInTransaction(TransactionStatus status) {
                    controlRepository.saveAndFlush(entity1)
                    controlRepository.saveAndFlush(entity2)
                    null
                }
            })
            def res1 = tested.acquireOwnershipOfSingletonJob(jobName1, -1, -3, -3)
            def res2 = tested.acquireOwnershipOfSingletonJob(jobName2, -3, -4, -5)
        then:
            res1
            res2
    }

    def "test acquireOwnershipOfSingletonJob existing owned by another node"() {
        given:
            def entity1_complete = BatchJobControlEntity.builder()
                    .jobName(jobName1)
                    .host(NetworkUtil.getHostName())
                    .jobId(-1)
                    .jobInstanceId(-2)
                    .jobExecutionId(-3)
                    .startTime(Instant.now())
                    .status(BatchStatus.COMPLETED)
                    .version(1L)
                    .build();
        when:
            txTemplate.execute(new TransactionCallback<Void>() {
                @Override
                Void doInTransaction(TransactionStatus status) {
                    controlRepository.saveAndFlush(entity1_complete)
                    null
                }
            })
            def res = tested.acquireOwnershipOfSingletonJob(jobName1, -1, -2, -3)
        then:
            1 * tested.isSingletonJobExecutionCandidate(_, _, _) >> {
                txTemplate.execute(new TransactionCallback<Void>() {
                    @Override
                    Void doInTransaction(TransactionStatus status) {
                        entityManager.createNativeQuery('update BatchJobControl set version=5 where jobName=\'' + jobName1 + '\'').executeUpdate()
                        null
                    }
                })
                def realRes = callRealMethod()
                return  realRes
            }
            !res
    }
}
