package org.narrative.reputation.util

import org.apache.commons.lang3.RandomUtils
import org.narrative.shared.event.reputation.*
import org.narrative.shared.redisson.codec.NarrativeJsonJacksonCodec
import org.narrative.shared.redisson.config.RedissonClientConfigProperties
import org.narrative.shared.reputation.config.redisson.ReputationRedissonQueueService
import org.redisson.Redisson
import org.redisson.api.RedissonClient

class ReputationEventSpewer {
    static final ConductEventType[] cetValues = ConductEventType.values();
    static final List<Long> oidPool = new ArrayList<>();

    ReputationRedissonQueueService redissonQueueService;

    public static void main(String[] args) {
        ReputationEventSpewer spewer = new ReputationEventSpewer()

        spewer.spew()

        spewer.shutdown()

        System.exit(0)
    }

    ReputationEventSpewer(ReputationRedissonQueueService redissonQueueService) {
        RedissonClientConfigProperties redissonConfig =
                RedissonClientConfigProperties.builder()
                        .codecName(NarrativeJsonJacksonCodec.name)
                        .scheme('redis')
                        .host('127.0.0.1')
                        .port('6379')
                        .threads(4)
                        .nettyThreads(4)
                        .timeout(5000)
                        .connectTimeout(30000)
                        .singleServer(true)
                        .databaseNumber(0)
                        .build()

        RedissonClient redissonClient = Redisson.create(redissonConfig.buildConfig());

        ReputationRedissonQueueService qs = new ReputationRedissonQueueService(redissonClient);

        this.redissonQueueService = qs
    }

    def initOidPool(int poolSize) {
        oidPool.clear();
        for(int i = 1; i < poolSize; i++) {
            oidPool.add(RandomUtils.nextLong())
        }
    }

    def getRandomPooledOid() {
        def idx = RandomUtils.nextInt(0, oidPool.size() - 1);
        return oidPool.get(idx);
    }

    def buildMeSomeEvents() {
        def res = []

        int idx = RandomUtils.nextInt(0, cetValues.length - 1);
        ConductEventType cet = cetValues[idx];

        def oid = getRandomPooledOid();

        res.add ConductStatusEvent.builder().userOid(oid).conductEventType(cet).build()
        res.add ContentLikeEvent.builder().userOid(oid).likePoints(10).likeEventType(LikeEventType.DISLIKE).build();
        res.add CommentLikeEvent.builder().userOid(oid).likePoints(10).likeEventType(LikeEventType.DISLIKE).build();
        res.add KYCVerificationEvent.builder().userOid(oid).isVerified(true).build();
        res.add RatingEvent.builder().userOid(oid).ratedWithConsensus(false).build();
        res.add ConsensusChangedEvent.builder().usersConsensusChangedMap(buildBooleanPairMap()).build();
        res.add VoteEndedEvent.builder().userVotesMap(buildVotePairMap()).build();
        res.add NegativeQualityEvent.builder().userOid(oid).negativeQualityEventType(NegativeQualityEventType.CHANGE_REQUEST_DENIED_BY_TRIBUNAL).build();

        res
    }

    def buildBooleanPairMap() {
        def res = [:]
        RandomUtils.nextInt(1, 30).times {
            res.put(getRandomPooledOid(), it % 2 == 0)
        }
        res
    }

    def buildVotePairMap() {
        def res = [:]
        RandomUtils.nextInt(1, 30).times {
            res.put(getRandomPooledOid(), it % 2 == 0 ? DecisionEnum.ACCEPTED : DecisionEnum.REJECTED)
        }
        res
    }

    def spewEvent(ReputationEvent event) {
        redissonQueueService.pushMessage(event.getEventType().eventQueueName, event)
    }

    def spew() {
        initOidPool(10);

        for (int i=0; i<100; i++) {
            def event = buildMeSomeEvents()
            for (def theEvent : event) {
                spewEvent(theEvent)
  //            Thread.sleep(1000)
            }
        }
    }

    def shutdown() {
        redissonQueueService.redissonClient.shutdown()
    }
}
