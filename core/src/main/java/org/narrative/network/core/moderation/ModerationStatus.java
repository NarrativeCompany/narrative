package org.narrative.network.core.moderation;

import org.narrative.common.util.enums.StringEnum;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Dec 1, 2005
 * Time: 11:11:42 AM
 *
 * @author Brian
 */
public enum ModerationStatus implements StringEnum {
    APPROVED("APPROVED", false, true),
    PENDING_APPROVAL("PENDING_APPROVAL", true, true),
    MODERATED("MODERATED", true, false);

    public static final String ENUM_FIELD_TYPE = "enum('APPROVED','PENDING_APPROVAL','MODERATED')";

    private static final Collection<ModerationStatus> MODERATION_STATUSES_REQUIRING_MODERATION;
    private static final Collection<ModerationStatus> LIVE_MODERATION_STATUSES;

    static {
        Set<ModerationStatus> modStatusesRequiringModeration = new HashSet<ModerationStatus>();
        Set<ModerationStatus> liveModStatuses = new HashSet<ModerationStatus>();
        for (ModerationStatus moderationStatus : ModerationStatus.values()) {
            if (moderationStatus.isRequiresModeration()) {
                modStatusesRequiringModeration.add(moderationStatus);
            }
            if (moderationStatus.isLive()) {
                liveModStatuses.add(moderationStatus);
            }
        }
        MODERATION_STATUSES_REQUIRING_MODERATION = Collections.unmodifiableCollection(modStatusesRequiringModeration);
        LIVE_MODERATION_STATUSES = Collections.unmodifiableCollection(liveModStatuses);
    }

    private final String idStr;
    private final boolean requiresModeration;
    private final boolean isLive;

    private ModerationStatus(String idStr, boolean requiresModeration, boolean isLive) {
        this.idStr = idStr;
        this.requiresModeration = requiresModeration;
        this.isLive = isLive;
    }

    @Override
    public String getIdStr() {
        return idStr;
    }

    public String getName() {
        return defaultWordlet("moderationStatus." + this);
    }

    public boolean isRequiresModeration() {
        return requiresModeration;
    }

    public boolean isLive() {
        return isLive;
    }

    public boolean isApproved() {
        return this == APPROVED;
    }

    public boolean isPendingApproval() {
        return this == PENDING_APPROVAL;
    }

    public boolean isModerated() {
        return this == MODERATED;
    }

    public static Collection<ModerationStatus> getModerationStatusesRequiringModeration() {
        return MODERATION_STATUSES_REQUIRING_MODERATION;
    }

    public static Collection<ModerationStatus> getLiveModerationStatuses() {
        return LIVE_MODERATION_STATUSES;
    }
}
