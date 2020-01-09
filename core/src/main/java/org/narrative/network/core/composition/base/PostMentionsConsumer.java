package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.mentions.MentionsUtil;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.preferences.AreaNotificationType;

import javax.persistence.Transient;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/27/16
 * Time: 10:39 AM
 */
public interface PostMentionsConsumer<T extends PostMentionsBase> {
    String getBody();

    T getMentions();

    User getUser();

    // jw: this method is used to signify to the implementor that it needs to add the PostMentionsBase if not defined already.
    T getMentionsForUpdate();

    @Transient
    default List<User> getMentionedMembersToNotify() {
        Set<OID> memberOidsToNotify = MentionsUtil.getMentionedMemberOids(getBody());
        if (isEmptyOrNull(memberOidsToNotify)) {
            return Collections.emptyList();
        }

        // jw: remove any members that we have already notified previously.
        T mentions = getMentions();
        if (mentions != null) {
            memberOidsToNotify.removeAll(mentions.getMentionedMemberOids());
        }

        return AreaUser.dao().getAllUsersWithCommunitySubscription(memberOidsToNotify, AreaNotificationType.SOMEONE_MENTIONS_ME);
    }

    default void addNotifiedMentionedMembers(List<User> mentionedMembers) {
        assert !isEmptyOrNull(mentionedMembers);

        T mentions = getMentionsForUpdate();

        mentions.addMentionedMemberOids(User.dao().getIdsFromObjects(mentionedMembers));
    }
}
