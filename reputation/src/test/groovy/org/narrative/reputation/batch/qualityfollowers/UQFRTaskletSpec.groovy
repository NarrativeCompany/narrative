package org.narrative.reputation.batch.qualityfollowers

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.RandomUtils
import org.narrative.batch.util.BatchJobHelper
import org.narrative.reputation.BaseContainerizedReputationApplicationSpec
import org.narrative.reputation.model.entity.CurrentReputationEntity
import org.narrative.reputation.model.entity.FollowedUserEntity
import org.narrative.reputation.model.entity.FollowerQualityEntity
import org.narrative.reputation.repository.CurrentQualityMembersRepository
import org.narrative.reputation.repository.CurrentReputationRepository
import org.narrative.reputation.repository.FollowedUserRepository
import org.narrative.reputation.repository.FollowerQualityRepository
import org.narrative.reputation.service.EventManagementService
import org.narrative.reputation.util.datagen.CurRepGen
import org.redisson.api.RedissonClient
import org.spockframework.spring.SpringBean
import org.springframework.batch.core.Job
import org.springframework.batch.core.launch.support.SimpleJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.IgnoreIf
import test.util.TestCategory

@DirtiesContext
@IgnoreIf({ TestCategory.isIntegContainerizedTestsDisabled()})
@Slf4j
@ActiveProfiles ( profiles = ['default','local','containerInteg, UQFR'] )
class UQFRTaskletSpec extends BaseContainerizedReputationApplicationSpec {
    @SpringBean
    RedissonClient redissonClient = Mock()
    @SpringBean
    EventManagementService eventManagementService = Mock()
    // Replace job launcher - this will create a job launcher with a synchronous executor
    @SpringBean
    SimpleJobLauncher jobLauncher = new SimpleJobLauncher();

    @Autowired
    CurrentReputationRepository currentReputationRepository
    @Autowired
    FollowedUserRepository followedUserRepository
    @Autowired
    CurrentQualityMembersRepository currentQualityMembersRepository;
    @Autowired
    FollowerQualityRepository followerQualityRepository;
    @Autowired
    JobRepository jobRepository
    @Autowired
    @Qualifier("qualityOfFollowersJob")
    Job qualityOfFollowersJobJob
    @Autowired
    TransactionTemplate txTemplate;

    def genSize = 100

    BatchJobHelper batchJobHelper = new BatchJobHelper(null, jobLauncher)

    def setup() {
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.afterPropertiesSet()

        log.info("Creating test data")

        // Generate members and followers
        def repUsers = CurRepGen.buildCurrentRepList(genSize / 2 )
        def repUsersNoQuality = CurRepGen.buildCurrentRepList(5, repUsers.get(repUsers.size() - 1).userOid + 1)
        repUsers.addAll(CurRepGen.buildCurrentRepList(genSize / 2, repUsersNoQuality.get(repUsersNoQuality.size() - 1).userOid + 1))
        repUsersNoQuality.addAll(CurRepGen.buildCurrentRepList(5, repUsers.get(repUsers.size() - 1).userOid + 1))

        txTemplate.execute({ TransactionStatus status ->
            currentReputationRepository.saveAll(repUsers);
            currentReputationRepository.flush();

            null
        });

        txTemplate.execute({ TransactionStatus status ->
            currentReputationRepository.saveAll(repUsersNoQuality);
            currentReputationRepository.flush();

            null
        });

        txTemplate.execute({ TransactionStatus status ->
            // For each user, generate a list of followers of random length
            for (CurrentReputationEntity curUser : repUsersNoQuality) {
                FollowerQualityEntity fqe = FollowerQualityEntity.builder()
                    .userOid(curUser.userOid)
                    .userQualityFollowerRatio(100)
                    .userQualityFollowerPctRank(200)
                    .batchJobId(-100)
                    .build();
                followerQualityRepository.save(fqe);
            }
        })

        txTemplate.execute({ TransactionStatus status ->
            // For each user, generate a list of followers of random length
            for (CurrentReputationEntity curUser : repUsers) {
                def followers = []
                def followersCount = RandomUtils.nextInt(1, repUsers.size())
                for (CurrentReputationEntity curFollower : repUsers) {
                    if (!curUser.equals(curFollower)) {
                        FollowedUserEntity fue = new FollowedUserEntity()
                        fue.setFollowedUserOid(curUser.userOid)
                        fue.setFollowingUserOid(curFollower.userOid)
                        followers.add(fue);

                        if (followers.size() >= followersCount) {
                            break
                        }
                    }
                }
                followedUserRepository.saveAll(followers)
                followedUserRepository.flush()
                followers.clear()
            }
            return null
        })
        log.info("Finished creating test data")
    }

    def cleanup() {
        currentReputationRepository.deleteAll()
        followedUserRepository.deleteAll()
    }

    def "Test something" () {
        when:
            batchJobHelper.launchJob(qualityOfFollowersJobJob, log)

            def mean = currentReputationRepository.calculateMeanReputationScore()
            def gtmCount = currentReputationRepository.countByReputationScoreGreaterThanEqual(mean)

            def cqme = currentQualityMembersRepository.findById(1)

            log.info("{} {}", mean, gtmCount)
        then:
            mean == cqme.get().meanReputationScore
            gtmCount == cqme.get().getTotalQualityMembers()
    }
}
