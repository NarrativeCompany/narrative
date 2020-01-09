package org.narrative.network.shared.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.security.area.community.advanced.services.GlobalSecurable;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.reputation.UserReputation;
import org.jetbrains.annotations.NotNull;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Dec 22, 2005
 * Time: 7:51:52 AM
 *
 * @author Brian
 */
@MappedSuperclass
public abstract class PrimaryRole implements Role {

    private OID oid;

    @Deprecated
    public PrimaryRole() {}

    public PrimaryRole(boolean init) {}

    @JsonIgnore
    @Transient
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @JsonIgnore
    @Transient
    public final PrimaryRole getPrimaryRole() {
        return this;
    }

    @JsonIgnore
    @Transient
    public final String getUniqueName() {
        return getDisplayNameResolved();
    }

    @JsonIgnore
    @Transient
    public abstract AuthZone getAuthZone();

    @JsonIgnore
    @Transient
    public abstract boolean isRegisteredUser();

    @JsonIgnore
    @Transient
    public abstract boolean isSpider();

    @JsonIgnore
    @NotNull
    public abstract AreaRole getAreaRoleForArea(Area area);

    @JsonIgnore
    @Transient
    public AreaRole getLoneAreaRole() {
        return getAreaRoleForArea(getAuthZone().getArea());
    }

    public void checkRegisteredUser() {
        if (!isRegisteredUser()) {
            throw new AuthZoneLoginRequired();
        }
    }

    public void checkCanVoteOnNiches() {
        getLoneAreaRole().checkNarrativeRight(NarrativePermissionType.VOTE_ON_APPROVALS);
    }

    public void checkCanParticipateInTribunalIssues() {
        getLoneAreaRole().checkGlobalRight(GlobalSecurable.PARTICIPATE_IN_TRIBUNAL_ACTIONS);
    }

    public void checkCanRemoveAupViolations() {
        getLoneAreaRole().checkGlobalRight(GlobalSecurable.REMOVE_AUP_VIOLATIONS);
    }

    public void checkNicheModerator(Niche niche) {
        checkRegisteredUser();
        if(!niche.isNicheModerator(this)) {
            throw new AccessViolation("accessViolation.notNicheModerator");
        }
    }

    @JsonIgnore
    @Transient
    public boolean isCanParticipateInTribunalIssues() {
        try {
            checkCanParticipateInTribunalIssues();
        } catch (AccessViolation av) {
            return false;
        }
        return true;
    }

    @JsonIgnore
    @Transient
    public boolean isCanRemoveAupViolations() {
        try {
            checkCanRemoveAupViolations();
        } catch (AccessViolation av) {
            return false;
        }
        return true;
    }

    public void checkCanPostIssueToTribunal() {
        getLoneAreaRole().checkNarrativeRight(NarrativePermissionType.SUBMIT_TRIBUNAL_APPEAL);
    }

    @JsonIgnore
    @Transient
    public abstract String getRoleStringForLogging();

    public static PrimaryRole getPrimaryRole(AuthZone authZone, User user, String guestName) {
        if (exists(user)) {
            return user;
        }

        Guest guest = new Guest(authZone);
        guest.setDisplayName(guestName);

        return guest;
    }

    @Transient
    public Set<AgeRating> getPreferredAgeRatings() {
        return AgeRating.ALL_AUDIENCES_ONLY;
    }

    @Transient
    public Set<AgeRating> getPermittedAgeRatings() {
        return AgeRating.ALL_AUDIENCES_ONLY;
    }

    @Transient
    public int getReputationAdjustedVotePoints() {
        // bl: by default, everyone (e.g. guests) just get a single vote point (the minimum)
        return UserReputation.MIN_POINTS_PER_VOTE;
    }
}
