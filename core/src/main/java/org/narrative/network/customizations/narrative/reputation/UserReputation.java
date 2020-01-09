package org.narrative.network.customizations.narrative.reputation;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.customizations.narrative.reputation.dao.UserReputationDAO;
import org.narrative.network.customizations.narrative.service.api.model.kyc.UserKycStatus;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Proxy;
import org.narrative.shared.calculator.TotalReputationCalculator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * This is a global view into the reputation database's CurrentReputationEntity.
 *
 * Date: 2018-12-13
 * Time: 10:56
 *
 * @author brian
 */
@Entity
@Proxy
@Immutable
@Getter
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserReputation implements DAOObject<UserReputationDAO> {
    public static final int MIN_POINTS_PER_VOTE = 1;
    public static final int MAX_POINTS_PER_VOTE = 100;
    public static final int MIN_POINTS_FOR_TWO_VOTES = MIN_POINTS_PER_VOTE + MAX_POINTS_PER_VOTE;
    public static final BigDecimal MAX_POINTS_PER_VOTE_BD = BigDecimal.valueOf(MAX_POINTS_PER_VOTE);
    public static final BigDecimal POINTS_MULTIPLIER = BigDecimal.valueOf(100);

    @Id
    private OID userOid;
    private Instant negativeConductStartTimestamp;
    private Instant negativeConductExpirationTimestamp;
    private double qualityAnalysis;

    @Getter(AccessLevel.NONE)
    private transient int qualityAnalysisScore = -1;
    @Getter(AccessLevel.NONE)
    private transient int kycVerifiedScore = -1;
    @Getter(AccessLevel.NONE)
    private transient int totalScore = -1;
    @Getter(AccessLevel.NONE)
    private transient ReputationLevel level;
    @Getter(AccessLevel.NONE)
    private transient Boolean kycVerificationPending;

    @Builder
    public UserReputation(OID userOid, Instant negativeConductStartTimestamp, Instant negativeConductExpirationTimestamp, double qualityAnalysis) {
        this.userOid = userOid;
        this.negativeConductStartTimestamp = negativeConductStartTimestamp;
        this.negativeConductExpirationTimestamp = negativeConductExpirationTimestamp;
        this.qualityAnalysis = qualityAnalysis;
    }

    @Transient
    @Override
    public OID getOid() {
        // just use the userOid as the oid. this way the UserReputationDTO is consistent with other DTO's oid property.
        return getUserOid();
    }

    @Override
    public void setOid(OID oid) {
        throw UnexpectedError.getRuntimeException("Should never attempt to set OID for UserReputation!");
    }

    @Transient
    public boolean isConductNegative() {
        // bl: if you're currently conduct neutral, then there is nothing further to do
        if (getNegativeConductExpirationTimestamp() == null || getNegativeConductExpirationTimestamp().isBefore(Instant.now())) {
            return false;
        }
        // if you are conduct negative, then there's an edge case where you may have just become KYC certified
        // but the reputation module hasn't yet processed the event (or the 5 minute cache TTL hasn't expired yet).
        // in that case, the UserReputation data will appear to be conduct negative. we can detect this special case
        // by inspecting your UserKyc data.
        UserKyc userKyc = UserKyc.dao().get(getUserOid());
        UserKycStatus status = userKyc.getKycStatus();

        // if you are KYC approved, then you might not be conduct negative anymore. we need to check to see
        // if the KYC verification time is between the conduct negative start time and end time.
        if (status.isApproved()) {
            // if the user has become KYC approved in the midst of their Conduct Negative window, then we know
            // that the user should be marked as Conduct Neutral. it should be just a very short matter of time
            // until the negativeConductStartTimestamp gets updated to be in the past.
            // note also that UserReputation has a 5-minute TTL since it contains data from a global view into the
            // reputation database. that's why we don't have update-based invalidations.
            if (userKyc.getLastUpdated().isAfter(getNegativeConductStartTimestamp()) && userKyc.getLastUpdated().isBefore(getNegativeConductExpirationTimestamp())) {
                return false;
            }
        }

        // didn't hit the special case above? then that means you are conduct negative.
        return true;
    }

    @Transient
    public int getTotalScore() {
        if (totalScore < 0) {
            UserKyc userKyc = UserKyc.dao().get(getUserOid());
            totalScore = TotalReputationCalculator.calculateTotalScore(getQualityAnalysis(), isConductNegative(), userKyc.getKycStatus().isApproved());
        }
        return totalScore;
    }

    @Transient
    public int getQualityAnalysisScore() {
        if (qualityAnalysisScore < 0) {
            qualityAnalysisScore = roundRepScore(getQualityAnalysis());
        }
        return qualityAnalysisScore;
    }

    @Transient
    public int getKycVerifiedScore() {
        if (kycVerifiedScore < 0) {
            // core is the master of the KYC flag, so derive it from UserKyc instead of pulling it from reputation,
            // which may be out of date with core (depending on event processing latency).
            UserKyc userKyc = UserKyc.dao().get(getUserOid());
            kycVerifiedScore = TotalReputationCalculator.calculateKycVerifiedScore(userKyc.getKycStatus().isApproved());
        }
        return kycVerifiedScore;
    }

    @Transient
    public Boolean getKycVerificationPending() {
        // bl: only return this prop for the current user
        if (!User.dao().get(getUserOid()).isCurrentUserThisUser()) {
            return null;
        }
        if (kycVerificationPending == null) {
            UserKyc userKyc = UserKyc.dao().get(getUserOid());
            kycVerificationPending = userKyc.getKycStatus().isPendingStatus();
        }
        return kycVerificationPending;
    }

    private static int roundRepScore(double score) {
        return BigDecimal.valueOf(score).setScale(0, RoundingMode.DOWN).intValueExact();
    }

    @Transient
    public ReputationLevel getLevel() {
        if (level == null) {
            if (isConductNegative()) {
                level = ReputationLevel.CONDUCT_NEGATIVE;
            } else {
                // bl: make sure the score is always at least zero so we're guaranteed to find a ReputationLevel.
                // otherwise, negative values (if it somehow happened) would break this and we wouldn't find a level
                int totalScore = Math.max(getTotalScore(), 0);
                for (ReputationLevel reputationLevel : ReputationLevel.SCORE_BASED_LEVELS_DESC) {
                    if (totalScore >= reputationLevel.getMinimumScore()) {
                        level = reputationLevel;
                        break;
                    }
                }
                assert level!=null : "Failed to identify reputation level! totalScore/" + totalScore;
            }
        }
        return level;
    }

    @Transient
    public int getAdjustedVotePoints() {
        int adjustVotePoints = getVotePointsMultiplier()
                .multiply(BigDecimal.valueOf(getTotalScore()))
                .multiply(POINTS_MULTIPLIER)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();

        // jw: if the user has a totalScore of 0, then we need to ensure a minimum of 1, otherwise it will just be 0.
        return Math.max(1, adjustVotePoints);
    }

    protected BigDecimal getVotePointsMultiplier() {
        int totalScore = getTotalScore();

        return ReputationMultiplierTier.getForScore(totalScore).getMultiplier();

    }

    public static UserReputationDAO dao() {
        return NetworkDAOImpl.getDAO(UserReputation.class);
    }

}
