package org.narrative.network.customizations.narrative.niches.referendum;

import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.propertyset.base.services.PropertiesPropertyMap;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeUtil;
import org.narrative.network.core.security.area.community.advanced.services.GlobalSecurable;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.niches.referendum.metadata.NicheDetailChangeReferendumMetadata;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.reputation.UserReputation;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
public enum ReferendumType implements IntegerEnum {
    APPROVE_SUGGESTED_NICHE(0, null, NarrativePermissionType.VOTE_ON_APPROVALS, 72, 72, new BigDecimal("0.50")),
    APPROVE_REJECTED_NICHE(2, null, NarrativePermissionType.VOTE_ON_APPROVALS, 72, 72, new BigDecimal("0.50")),
    RATIFY_NICHE(3, null, NarrativePermissionType.VOTE_ON_APPROVALS, 72, 72, new BigDecimal("0.50")),
    TRIBUNAL_APPROVE_REJECTED_NICHE(4, GlobalSecurable.PARTICIPATE_IN_TRIBUNAL_ACTIONS, null, IPDateUtil.WEEK_IN_DAYS * IPDateUtil.DAY_IN_HOURS, 1, new BigDecimal("0.50")) {
        @Override
        public boolean doesReferendumHaveEnoughVotes(Referendum referendum, BigDecimal totalVotes, BigDecimal totalVotePoints) {
            BigDecimal totalTribunalMembers = BigDecimal.valueOf(referendum.getTribunalMemberCount());
            BigDecimal minimumVotes = totalTribunalMembers.multiply(DEFAULT_MINIMUM_TRIBUNAL_PARTICIPANT_PERCENTAGE);

            // jw: if we do not have at least 70% participation, then this referendum does not have enough votes.
            if(totalVotes.compareTo(minimumVotes) < 0) {
                return false;
            }

            // bl: last but not least, don't allow a Tribunal referendum to end in a tie. consider it
            // as not having enough votes if this is the case
            return referendum.getVotePointsFor() != referendum.getVotePointsAgainst();
        }
    },
    TRIBUNAL_RATIFY_NICHE(5, GlobalSecurable.PARTICIPATE_IN_TRIBUNAL_ACTIONS, null, IPDateUtil.WEEK_IN_DAYS * IPDateUtil.DAY_IN_HOURS, 1, new BigDecimal("0.50")) {
        @Override
        public boolean doesReferendumHaveEnoughVotes(Referendum referendum, BigDecimal totalVotes, BigDecimal totalVotePoints) {
            // jw: the logic is identical between tribunal issue types, so let's just delegate up.
            return TRIBUNAL_APPROVE_REJECTED_NICHE.doesReferendumHaveEnoughVotes(referendum, totalVotes, totalVotePoints);
        }
    },
    TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE(1, GlobalSecurable.PARTICIPATE_IN_TRIBUNAL_ACTIONS, null, IPDateUtil.WEEK_IN_DAYS * IPDateUtil.DAY_IN_HOURS, 1, new BigDecimal("0.50"), NicheDetailChangeReferendumMetadata.class) {
        @Override
        public boolean doesReferendumHaveEnoughVotes(Referendum referendum, BigDecimal totalVotes, BigDecimal totalVotePoints) {
            // jw: the logic is identical between tribunal issue types, so let's just delegate up.
            return TRIBUNAL_APPROVE_REJECTED_NICHE.doesReferendumHaveEnoughVotes(referendum, totalVotes, totalVotePoints);
        }
    },
    TRIBUNAL_RATIFY_PUBLICATION(6, GlobalSecurable.PARTICIPATE_IN_TRIBUNAL_ACTIONS, null, IPDateUtil.WEEK_IN_DAYS * IPDateUtil.DAY_IN_HOURS, 1, new BigDecimal("0.50")) {
        @Override
        public boolean doesReferendumHaveEnoughVotes(Referendum referendum, BigDecimal totalVotes, BigDecimal totalVotePoints) {
            // jw: the logic is identical between tribunal issue types, so let's just delegate up.
            return TRIBUNAL_APPROVE_REJECTED_NICHE.doesReferendumHaveEnoughVotes(referendum, totalVotes, totalVotePoints);
        }
    }
    ;

    private final int id;
    private final GlobalSecurable votingSecurable;
    private final NarrativePermissionType votingPermissionType;
    private final long initialVotingPeriodInHours;
    private final long votingPeriodExtensionInHours;
    private final BigDecimal successRatio;
    private final Class<? extends ReferendumMetadata> metadataClass;

    private static final BigDecimal DEFAULT_MINIMUM_VOTERS = BigDecimal.valueOf(20);
    // bl: require at least 1 full vote point before an approval ends
    private static final BigDecimal DEFAULT_MINIMUM_VOTE_POINTS = BigDecimal.valueOf(UserReputation.MAX_POINTS_PER_VOTE);
    private static final BigDecimal DEFAULT_MINIMUM_TRIBUNAL_PARTICIPANT_PERCENTAGE = new BigDecimal("0.7");

    public static final Set<ReferendumType> NICHE_TYPES;
    public static final Set<ReferendumType> TRIBUNAL_TYPES;
    public static final Set<ReferendumType> TYPES_VALID_FOR_NEW_NICHE_REFERENDUMS;

    static {
        // jw: currently, we do not care about ordering on these sets, but use LinkedHashSet if that ever changes
        Set<ReferendumType> nicheTypes = new LinkedHashSet<>();
        Set<ReferendumType> tribunalTypes = new LinkedHashSet<>();

        for (ReferendumType type : values()) {
            if (type.isTribunalReferendum()) {
                tribunalTypes.add(type);
            } else if (type.isNicheRelated()) {
                nicheTypes.add(type);
            }
        }

        NICHE_TYPES = Collections.unmodifiableSet(nicheTypes);
        TRIBUNAL_TYPES = Collections.unmodifiableSet(tribunalTypes);

        // bl: new referendums only support approving suggested niches and tribunal issue types
        Set<ReferendumType> typesValidForNewNicheReferendums = new LinkedHashSet<>();
        typesValidForNewNicheReferendums.add(APPROVE_SUGGESTED_NICHE);
        typesValidForNewNicheReferendums.add(TRIBUNAL_APPROVE_REJECTED_NICHE);
        typesValidForNewNicheReferendums.add(TRIBUNAL_RATIFY_NICHE);
        typesValidForNewNicheReferendums.add(TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE);
        TYPES_VALID_FOR_NEW_NICHE_REFERENDUMS = Collections.unmodifiableSet(typesValidForNewNicheReferendums);
    }

    ReferendumType(int id, GlobalSecurable votingSecurable, NarrativePermissionType votingPermissionType, int initialVotingPeriodInHours, int votingPeriodExtensionInHours, BigDecimal successRatio) {
        this(id, votingSecurable, votingPermissionType, initialVotingPeriodInHours, votingPeriodExtensionInHours, successRatio, null);
    }

    ReferendumType(int id, GlobalSecurable votingSecurable, NarrativePermissionType votingPermissionType, int initialVotingPeriodInHours, int votingPeriodExtensionInHours, BigDecimal successRatio, Class<? extends ReferendumMetadata> metadataClass) {
        this.id = id;
        this.votingSecurable = votingSecurable;
        this.votingPermissionType = votingPermissionType;
        this.initialVotingPeriodInHours = initialVotingPeriodInHours;
        this.votingPeriodExtensionInHours = votingPeriodExtensionInHours;
        this.successRatio = successRatio;
        this.metadataClass = metadataClass;
    }

    public boolean doesReferendumHaveEnoughVotes(Referendum referendum, BigDecimal totalVotes, BigDecimal totalVotePoints) {
        // bl: make sure we have met the default of both the minimum number of voters and the minimum number of vote points
        return DEFAULT_MINIMUM_VOTERS.compareTo(totalVotes) <= 0 && DEFAULT_MINIMUM_VOTE_POINTS.compareTo(totalVotePoints) <= 0;
    }

    public boolean wasReferendumPassed(BigDecimal totalVotePoints, BigDecimal totalVotePointsFor) {
        if (totalVotePoints.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        return totalVotePointsFor.divide(totalVotePoints, 10, BigDecimal.ROUND_FLOOR).compareTo(successRatio) > 0;
    }

    @Override
    public int getId() {
        return id;
    }

    public Class<? extends ReferendumMetadata> getMetadataClass() {
        return metadataClass;
    }

    public <T extends ReferendumMetadata> T getMetadata(Properties properties) {
        return (T) PropertySetTypeUtil.getPropertyWrapper(metadataClass, new PropertiesPropertyMap(properties));
    }

    public boolean isCanUserVote(User user) {
        if(votingSecurable!=null) {
            return user.getLoneAreaUser().hasGlobalRight(votingSecurable);
        }
        return user.getLoneAreaUser().hasNarrativeRight(votingPermissionType);
    }

    public long getInitialVotingPeriodInHours() {
        return initialVotingPeriodInHours;
    }

    public long getVotingPeriodExtensionInHours() {
        return votingPeriodExtensionInHours;
    }

    public boolean isTribunalReferendum() {
        return votingSecurable!=null && votingSecurable.isParticipateInTribunalActions();
    }

    public boolean isApproveSuggestedNiche() {
        return this == APPROVE_SUGGESTED_NICHE;
    }

    public boolean isTribunalApproveNicheDetails() {
        return this == TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE;
    }

    public boolean isTribunalApproveRejectedNiche() {
        return this == TRIBUNAL_APPROVE_REJECTED_NICHE;
    }

    public boolean isTribunalRatifyNiche() {
        return this == TRIBUNAL_RATIFY_NICHE;
    }

    public boolean isTribunalRatifyPublication() {
        return this == TRIBUNAL_RATIFY_PUBLICATION;
    }

    public boolean isApproveRejectedNiche() {
        return this == APPROVE_REJECTED_NICHE;
    }

    public boolean isRatifyNiche() {
        return this == RATIFY_NICHE;
    }

    public boolean isRatifyStatus() {
        return isTribunalRatifyNiche() || isTribunalRatifyPublication();
    }

    public String getNameForDisplay() {
        return wordlet("referendumType." + this);
    }

    public String getTypeForDisplay() {
        return wordlet("referendumType.type." + this);
    }

    public String getNameForVoteUp() {
        return wordlet("referendumType." + this + ".voteUpLabel");
    }

    public String getNameForVoteDown() {
        return wordlet("referendumType." + this + ".voteDownLabel");
    }

    public String getNameForDescriptionWordletKey() {
        if (isTribunalApproveNicheDetails() || isApproveRejectedNiche() || isRatifyNiche()) {
            return "referendumType." + this + ".description";
        }
        return "";
    }

    public boolean isPublicationRelated() {
        return isTribunalRatifyPublication();
    }

    public boolean isNicheRelated() {
        // jw: currently, all referendums deal with the status of either a Niche, or a Publication. So, if this is not
        //     publication related then it is by definition Niche related.
        return !isPublicationRelated();
    }

    public boolean isRequireReasonWhenVotingAgainst() {
        return isApproveSuggestedNiche() || isRatifyNiche();
    }

    public boolean isSupportsQualityRatingReplies() {
        // bl: tribunal referendums do not support quality rating replies
        return !isTribunalReferendum();
    }

    public ChannelType getChannelType() {
        if (isNicheRelated()) {
            return ChannelType.NICHE;
        }

        assert isPublicationRelated() : "Expected publication related, not/"+this;

        return ChannelType.PUBLICATION;
    }
}
