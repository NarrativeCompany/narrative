package org.narrative.network.core.security.area.base;

import org.jetbrains.annotations.Nullable;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.security.Guest;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.security.TransientRole;

import javax.persistence.Transient;

import java.util.Collections;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 5, 2005
 * Time: 5:25:16 PM
 */
public class AreaGuest extends AreaRole implements TransientRole {

    private final PrimaryRole primaryRole;
    private final OID areaOid;
    private final OID oid;

    public AreaGuest(Area area, PrimaryRole primaryRole) {
        super(true);
        this.primaryRole = primaryRole;
        this.oid = primaryRole.getOid();
        // bl: do NOT cache the Area object here, as it presents issues since these objects may be used
        // in other Hibernate sessions (e.g. for shared search engine spiders when calling valueUnbound()).
        this.areaOid = area.getOid();
        // todo: do we want to do a better job of tracking this user as an AreaGuest?
        // for now, just using the user's network userOID for tracking purposes.
    }

    public OID getOid() {
        return oid;
    }

    @Transient
    public PrimaryRole getPrimaryRole() {
        return primaryRole;
    }

    @Transient
    public String getGuestName() {
        // bl: in order to avoid "Username (Guest) (Guest)" from appearing in chat UIs, we do _not_
        // want to use getDisplayNameResolved() for the Guest object, since that includes a "(Guest)" suffix
        // which will already be added automatically as a suffix in the UI via JSP tags like displayNameText.tag.
        // note that we do _not_ want to use Guest.getDisplayNameResolved() below or else that will include the "(Guest)" suffix.
        return primaryRole.isRegisteredUser() ? primaryRole.getDisplayNameResolved() : ((Guest) primaryRole).getDisplayName();
    }

    public String getDisplayNameResolved() {
        return primaryRole.getDisplayNameResolved();
    }

    @Nullable
    public User getUser() {
        if (!primaryRole.isRegisteredUser()) {
            return null;
        }

        return primaryRole.getUser();
    }

    public AreaUser getAreaUser() {
        return null;
    }

    @Transient
    @Nullable
    public AreaUserRlm getAreaUserRlm() {
        return null;
    }

    @Override
    public Set<AreaCircle> getEffectiveAreaCircles() {
        return Collections.emptySet();
    }

    @Transient
    public boolean isAnAreaUser() {
        return false;
    }

    @Transient
    public boolean isActiveRegisteredAreaUser() {
        return false;
    }

    public Area getArea() {
        return Area.dao().get(areaOid);
    }
}
