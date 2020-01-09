package org.narrative.network.customizations.narrative.publications;

import org.narrative.network.customizations.narrative.channels.ChannelRole;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-07-31
 * Time: 08:02
 *
 * @author jonmark
 */
public enum PublicationRole implements ChannelRole {
    ADMIN(0) {
        @Override
        public Integer getPlanLimit(PublicationPlanType plan) {
            // bl: no admin limit, so return null to indicate that
            return null;
        }
    }
    ,EDITOR(1) {
        @Override
        public Integer getPlanLimit(PublicationPlanType plan) {
            return plan.getMaxEditors();
        }
    }
    ,WRITER(2) {
        @Override
        public Integer getPlanLimit(PublicationPlanType plan) {
            return plan.getMaxWriters();
        }
    }
    ;

    private final int id;

    PublicationRole(int id) {
        this.id = id;
    }

    private static final Map<PublicationRole,Set<PublicationRole>> ROLE_TO_INVITE_ROLES;

    static {
        Map<PublicationRole,Set<PublicationRole>> map = new HashMap<>();
        map.put(ADMIN, Collections.unmodifiableSet(EnumSet.of(EDITOR, WRITER)));
        map.put(EDITOR, Collections.unmodifiableSet(EnumSet.of(WRITER)));
        map.put(WRITER, Collections.emptySet());
        assert map.keySet().containsAll(EnumSet.allOf(PublicationRole.class)) : "Should add invite role mapping for all PublicationRoles!";
        ROLE_TO_INVITE_ROLES = Collections.unmodifiableMap(map);
    }

    @Override
    public int getId() {
        return id;
    }

    public String getNameForDisplay() {
        return wordlet("publicationRole." + this);
    }

    public String getNameForDisplayWithArticle() {
        return wordlet("publicationRole." + this + ".withArticle");
    }

    public Set<PublicationRole> getInviteRoles() {
        return ROLE_TO_INVITE_ROLES.get(this);
    }

    public abstract Integer getPlanLimit(PublicationPlanType plan);

    public boolean isPlanLimitReached(Publication publication) {
        Integer limit = getPlanLimit(publication.getPlan());

        // jw: if there is no limit then obviously the limit has not been reached.
        if (limit==null) {
            return false;
        }

        return publication.getUserCountForRole(this) >= limit;
    }
}