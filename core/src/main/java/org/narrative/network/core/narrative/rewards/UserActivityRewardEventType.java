package org.narrative.network.core.narrative.rewards;

import org.narrative.common.util.enums.IntegerEnum;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Date: 2019-05-28
 * Time: 15:11
 *
 * @author brian
 */
public enum UserActivityRewardEventType implements IntegerEnum {
    PUBLISH_POST(0, 20)
    ,SUBMIT_COMMENT(1, 5)
    ,RATE_POST_COMMENT(2, 3)
    ,VOTE(3, 3)
    ,VOTE_ON_APPEAL(4, 7)
    ,BID_ON_AUCTION(5, 5)
    ,WIN_NICHE_AUCTION(6, 20)
    ,SUGGESTED_NICHE_APPROVED(7, 10)
    ,FOLLOW_SOMETHING(8, 1)
    ,ACCEPT_MODERATOR_NOMINATION(9, 3)
    ,WIN_NICHE_MODERATOR_ELECTION(10, 10)
    ,GET_CERTIFIED(11, 100)
    ,ADD_PROFILE_PICTURE(12, 20, false)
    ,TIP_SOMEONE(13, 10)
    ,PROMOTE_POST(14, 20)
    ,PAY_FOR_NICHE(15, 100)
    ,PAY_FOR_PUBLICATION(16, 20)
    ,REPORTED_AUP_VIOLATION_REMOVED(17, 50)
    ,APPEAL_UPHELD(18, 30)
    ;

    private final int id;
    private final int points;
    private final boolean reputationAdjusted;

    UserActivityRewardEventType(int id, int points) {
        this(id, points, true);
    }

    UserActivityRewardEventType(int id, int points, boolean reputationAdjusted) {
        this.id = id;
        this.points = points;
        this.reputationAdjusted = reputationAdjusted;
    }

    public static final Collection<UserActivityRewardEventType> TYPES_FOR_EVENTS = Collections.unmodifiableSet(Arrays.asList(values()).stream().filter(UserActivityRewardEventType::isUsesUserActivityRewardEvent).collect(Collectors.toSet()));

    @Override
    public int getId() {
        return id;
    }

    public int getPoints() {
        return points;
    }

    public boolean isReputationAdjusted() {
        return reputationAdjusted;
    }

    public boolean isUsesUserActivityRewardEvent() {
        // bl: when adding extra types here, be sure to also update the bulk insert statement:
        // userActivityReward.insertTempRecordsForRewardEvents
        return isReportedAupViolationRemoved();
    }

    public boolean isReportedAupViolationRemoved() {
        return this == REPORTED_AUP_VIOLATION_REMOVED;
    }
}
