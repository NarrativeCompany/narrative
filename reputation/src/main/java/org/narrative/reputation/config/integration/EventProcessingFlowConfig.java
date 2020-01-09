package org.narrative.reputation.config.integration;

import lombok.extern.slf4j.Slf4j;
import org.narrative.reputation.config.ReputationProperties;
import org.narrative.reputation.service.ConductStatusCalculatorService;
import org.narrative.reputation.service.ContentQualityCalculatorService;
import org.narrative.reputation.service.KYCVerificationService;
import org.narrative.reputation.service.RatingCorrelationService;
import org.narrative.reputation.service.TotalReputationScoreCalculatorService;
import org.narrative.reputation.service.VoteCorrelationService;
import org.narrative.shared.event.reputation.BulkUserEvent;
import org.narrative.shared.event.reputation.KYCVerificationEvent;
import org.narrative.shared.event.reputation.LikeEvent;
import org.narrative.shared.event.reputation.NegativeQualityEvent;
import org.narrative.shared.event.reputation.ConsensusChangedEvent;
import org.narrative.shared.event.reputation.RatingEvent;
import org.narrative.shared.event.reputation.ReputationEventType;
import org.narrative.shared.event.reputation.ConductStatusEvent;
import org.narrative.shared.event.reputation.UserEvent;
import org.narrative.shared.event.reputation.VoteEndedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import java.util.Collections;

@Configuration
@Slf4j
public class EventProcessingFlowConfig {
    private final ReputationProperties reputationProperties;
    private final TotalReputationScoreCalculatorService totalReputationScoreCalculatorService;
    private final ReputationFlowBuilder reputationFlowBuilder;
    private final ContentQualityCalculatorService contentQualityCalculatorService;
    private final ConductStatusCalculatorService conductStatusCalculatorService;
    private final VoteCorrelationService voteCorrelationService;
    private final RatingCorrelationService ratingCorrelationService;
    private final KYCVerificationService kycVerificationService;

    public EventProcessingFlowConfig(ReputationProperties reputationProperties,
                                     TotalReputationScoreCalculatorService totalReputationScoreCalculatorService,
                                     ReputationFlowBuilder reputationFlowBuilder,
                                     ContentQualityCalculatorService contentQualityCalculatorService,
                                     ConductStatusCalculatorService conductStatusCalculatorService,
                                     VoteCorrelationService voteCorrelationService,
                                     RatingCorrelationService ratingCorrelationService,
                                     KYCVerificationService kycVerificationService) {
        this.reputationProperties = reputationProperties;
        this.totalReputationScoreCalculatorService = totalReputationScoreCalculatorService;
        this.reputationFlowBuilder = reputationFlowBuilder;
        this.contentQualityCalculatorService = contentQualityCalculatorService;
        this.conductStatusCalculatorService = conductStatusCalculatorService;
        this.voteCorrelationService = voteCorrelationService;
        this.ratingCorrelationService = ratingCorrelationService;
        this.kycVerificationService = kycVerificationService;
    }

    /**
     * Integration flow for processing {@link ConductStatusEvent}
     */
    @Bean
    public IntegrationFlow conductStatusEventFlow() {
        return reputationFlowBuilder.buildSimpleIntegrationFlow(
                ReputationEventType.CONDUCT_STATUS_EVENT,
                reputationProperties.getSi().getEvent().getConductStatusEvent(),
                (cse) -> conductStatusCalculatorService.calculateNegativeConductExpirationDate((ConductStatusEvent) cse)
        );
    }

    /**
     * Integration flow for processing {@link LikeEvent}
     */
    @Bean
    public IntegrationFlow likeEventFlow() {
        return reputationFlowBuilder.buildSimpleIntegrationFlow(
                ReputationEventType.LIKE_EVENT,
                reputationProperties.getSi().getEvent().getLikeEvent(),
                (le) -> contentQualityCalculatorService.updateContentQualityWithEvent((LikeEvent) le)
        );
    }

    /**
     * Integration flow for processing {@link KYCVerificationEvent}
     */
    @Bean
    public IntegrationFlow kycEventFlow() {
        return reputationFlowBuilder.buildSimpleIntegrationFlow(
                ReputationEventType.KYC_VERIFICATION_EVENT,
                reputationProperties.getSi().getEvent().getKycVerifyEvent(),
                (kyce) -> kycVerificationService.updateCurrentReputationWithKYCVerificationEvent((KYCVerificationEvent) kyce)
        );
    }

    /**
     * Integration flow for processing {@link RatingEvent}
     */
    @Bean
    public IntegrationFlow ratingEventFlow() {
        return reputationFlowBuilder.buildSimpleIntegrationFlow(
                ReputationEventType.RATING_EVENT,
                reputationProperties.getSi().getEvent().getRatingEvent(),
                (re) -> ratingCorrelationService.updateRatingCorrelationWithRatingEvent((RatingEvent) re)
        );
    }

    /**
     * Integration flow for processing {@link VoteEndedEvent}
     */
    @Bean
    public IntegrationFlow voteEndedEventFlow() {
        return reputationFlowBuilder.buildBulkUserIntegrationFlow(
                ReputationEventType.VOTE_ENDED_EVENT,
                reputationProperties.getSi().getEvent().getVotingEndedEvent(),
                (vee) -> voteCorrelationService.updateVoteCorrelationWithEvent((VoteEndedEvent) vee),
                ReputationMessageChannelConfig.AFTER_BULK_USER_FLOW_FINISHED
        );
    }

    /**
     * Integration flow for processing {@link NegativeQualityEvent}
     */
    @Bean
    public IntegrationFlow negativeQualityEventFlow() {
        return reputationFlowBuilder.buildSimpleIntegrationFlow(
                ReputationEventType.NEGATIVE_QUALITY_EVENT,
                reputationProperties.getSi().getEvent().getNegativeQualityEvent(),
                (nqe) -> voteCorrelationService.updateVoteCorrelationForNegativeEvent((NegativeQualityEvent) nqe)
        );
    }

    /**
     * Integration flow for processing {@link ConsensusChangedEvent}
     */
    @Bean
    public IntegrationFlow ratingConsensusChangedEventFlow() {
        return reputationFlowBuilder.buildBulkUserIntegrationFlow(
                ReputationEventType.RATING_CONSENSUS_CHANGED_EVENT,
                reputationProperties.getSi().getEvent().getConsensusChangedEvent(),
                (rcce) -> ratingCorrelationService.updateRatingCorrelationWithRatingConsensusChangedEvent((ConsensusChangedEvent) rcce),
                ReputationMessageChannelConfig.AFTER_BULK_USER_FLOW_FINISHED
        );
    }

    @Bean
    IntegrationFlow afterSimpleFlowFlow() {
        return IntegrationFlows
                .from(ReputationMessageChannelConfig.AFTER_SIMPLE_FLOW)
                /*
                 * Re-calculate the event user's reputation - use a wiretap since this is a void method and will
                 * not return the message for the next processing step
                 */
                .wireTap(
                        flowDefinition -> flowDefinition.<UserEvent>handle((body, headers) -> {
                            totalReputationScoreCalculatorService.calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(Collections.singleton(body.getUserOid()));
                            return null;
                        })
                )

                /*
                 * Notify interested parties that processing is complete for this event
                 */
                .channel(ReputationMessageChannelConfig.MARK_PROCESSED_PUBLISH_EVENT)

                .get();
    }

    @Bean
    IntegrationFlow afterBulkUserEventFlow() {
        return IntegrationFlows
                .from(ReputationMessageChannelConfig.AFTER_BULK_USER_FLOW_FINISHED)
                /*
                 * Re-calculate the event user's reputation for all users called out in the message - use a wiretap
                 * since this is a void method and will not return the message for the next processing step
                 */
                .wireTap(
                        flowDefinition -> flowDefinition.<BulkUserEvent>handle((body, headers) -> {
                            totalReputationScoreCalculatorService.calculateTotalScoreAndUpdateCurrentReputationEntityForUsers(body.getUserOidSet());
                            return null;
                        })
                )

                /*
                 * Notify interested parties that processing is complete for this event
                 */
                .channel(ReputationMessageChannelConfig.MARK_PROCESSED_PUBLISH_EVENT)

                .get();
    }
}
