package org.narrative.network.core.area.user;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.user.dao.AreaUserRlmDAO;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.NarrativeCircleType;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheAssociationSlot;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheUserAssociation;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.shared.security.PrimaryRole;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Proxy;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 13, 2005
 * Time: 11:55:24 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AreaUserRlm extends AreaRole implements DAOObject<AreaUserRlmDAO> {
    public static final String FIELD__AREA_USER__NAME = "areaUser";

    private OID oid;
    private AreaRlm areaRlm;
    private AreaUser areaUser;

    private Set<SandboxedAreaUser> sandboxedAreaUsers;

    private Map<NicheAssociationSlot, NicheUserAssociation> nicheUserAssociations;

    /**
     * @deprecated for hibernate use only
     */
    public AreaUserRlm() {}

    public AreaUserRlm(AreaRlm areaRlm, AreaUser areaUser) {
        this.oid = areaUser.getOid();
        this.areaUser = areaUser;
        this.areaRlm = areaRlm;
        nicheUserAssociations = new HashMap<>();

        this.sandboxedAreaUsers = newHashSet();
        SandboxedAreaUser sandboxedAreaUser = new SandboxedAreaUser(this, areaUser.getDisplayName());
        sandboxedAreaUsers.add(sandboxedAreaUser);
    }

    @Id
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "FKCC8AE43B2603C9A5")
    public AreaRlm getAreaRlm() {
        return areaRlm;
    }

    public void setAreaRlm(AreaRlm areaRlm) {
        this.areaRlm = areaRlm;
    }

    /**
     * @deprecated for hibernate use only
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = SandboxedAreaUser.FIELD__OID__NAME, cascade = javax.persistence.CascadeType.ALL)
    public Set<SandboxedAreaUser> getSandboxedAreaUsers() {
        return sandboxedAreaUsers;
    }

    public void setSandboxedAreaUsers(Set<SandboxedAreaUser> sandboxedAreaUsers) {
        this.sandboxedAreaUsers = sandboxedAreaUsers;
    }

    @Transient
    public String getDisplayNameResolved() {
        return getAreaUser().getDisplayNameResolved();
    }

    @Transient
    public Area getArea() {
        return AreaRlm.getArea(areaRlm);
    }

    @OneToOne(fetch = FetchType.LAZY)
    @Immutable
    @JoinColumn(name = AreaUser.FIELD__OID__COLUMN)
    public AreaUser getAreaUser() {
        return areaUser;
    }

    public void setAreaUser(AreaUser areaUser) {
        this.areaUser = areaUser;
    }

    @OneToMany(mappedBy = NicheUserAssociation.FIELD__AREA_USER_RLM__NAME, cascade = javax.persistence.CascadeType.ALL)
    @MapKey(name = NicheUserAssociation.FIELD__ASSOCIATION_SLOT__NAME)
    public Map<NicheAssociationSlot, NicheUserAssociation> getNicheUserAssociations() {
        return nicheUserAssociations;
    }

    public void setNicheUserAssociations(Map<NicheAssociationSlot, NicheUserAssociation> nicheUserAssociations) {
        this.nicheUserAssociations = nicheUserAssociations;
    }

    private transient Set<NicheAssociationSlot> availableNicheAssociationSlots;

    @Transient
    public Set<NicheAssociationSlot> getAvailableNicheAssociationSlots() {
        if (availableNicheAssociationSlots == null) {
            availableNicheAssociationSlots = complementOfEnumSet(NicheAssociationSlot.class, getNicheUserAssociations().keySet());
        }

        return availableNicheAssociationSlots;
    }

    @Transient
    public NicheUserAssociation getNicheAssociation(Niche niche) {
        for (NicheUserAssociation association : getNicheUserAssociations().values()) {
            if (isEqual(niche, association.getNiche())) {
                return association;
            }
        }

        return null;
    }

    public void removeExpectedNicheAssociation(Niche niche, boolean skipDelete) {
        NicheUserAssociation association = getNicheAssociation(niche);
        assert exists(association) : "Should always have an association at this point!";

        getNicheUserAssociations().remove(association.getAssociationSlot());

        if (association.getType().isOwner()) {
            boolean stillOwnsNiche = false;
            for (NicheUserAssociation otherAssociation : getNicheUserAssociations().values()) {
                if (otherAssociation.getType().isOwner()) {
                    stillOwnsNiche = true;
                    break;
                }
            }

            // jw: if the previous owner no longer owns any niches, then let's remove them from the niche owners circle.
            if (!stillOwnsNiche) {
                NarrativeCircleType.NICHE_OWNERS.removeUserFromCircle(getUser());
            }
        }

        if (!skipDelete) {
            NicheUserAssociation.dao().delete(association);
        }
    }

    private transient Set<Niche> ownedNiches;
    private transient Set<Niche> biddingOnNiches;
    private transient Set<Niche> activelyBiddingOnNiches;
    private transient Map<Niche, NicheAuctionInvoice> outstandingInvoiceLookup;

    private void setupNicheAssociationDetails() {
        if (biddingOnNiches != null || ownedNiches != null) {
            return;
        }

        Set<Niche> owned = new TreeSet<>(Niche.NAME_COMPARATOR);
        Set<Niche> bidding = new TreeSet<>(Niche.NAME_COMPARATOR);
        Set<Niche> activelyBidding = new TreeSet<>(Niche.NAME_COMPARATOR);
        Map<Niche, NicheAuctionInvoice> invoiceLookup = new TreeMap<>(Niche.NAME_COMPARATOR);
        for (NicheUserAssociation association : getNicheUserAssociations().values()) {
            Niche niche = association.getNiche();
            if (association.getType().isOwner()) {
                owned.add(niche);
                continue;
            }

            bidding.add(niche);

            NicheAuctionInvoice invoice = getOutstandingInvoice(niche);
            if (exists(invoice)) {
                invoiceLookup.put(niche, invoice);
            } else {
                activelyBidding.add(niche);
            }
        }

        ownedNiches = Collections.unmodifiableSet(owned);
        biddingOnNiches = Collections.unmodifiableSet(bidding);
        activelyBiddingOnNiches = Collections.unmodifiableSet(activelyBidding);
        outstandingInvoiceLookup = Collections.unmodifiableMap(invoiceLookup);
    }

    @Transient
    public Set<Niche> getOwnedNiches() {
        setupNicheAssociationDetails();
        return ownedNiches;
    }

    @Transient
    public Set<Niche> getBiddingOnNiches() {
        setupNicheAssociationDetails();
        return biddingOnNiches;
    }

    @Transient
    public Set<Niche> getActivelyBiddingOnNiches() {
        setupNicheAssociationDetails();
        return activelyBiddingOnNiches;
    }

    @Transient
    public Map<Niche, NicheAuctionInvoice> getOutstandingInvoiceLookup() {
        setupNicheAssociationDetails();
        return outstandingInvoiceLookup;
    }

    private NicheAuctionInvoice getOutstandingInvoice(Niche niche) {
        NicheAuction auction = niche.getActiveAuction();
        assert exists(auction) : "There should always be an active auction for niches associated to a user with a bidder relationship!";

        if (!exists(auction.getActiveInvoice())) {
            return null;
        }

        if (isEqual(getUser(), auction.getActiveInvoice().getInvoice().getUser())) {
            return auction.getActiveInvoice();
        }

        return null;
    }

    private transient List<NicheAuction> leadingBidderForAuctions;

    @Transient
    public List<NicheAuction> getLeadingBidderForAuctions() {
        if (leadingBidderForAuctions == null) {
            List<NicheAuction> auctions = new LinkedList<>();
            for (Niche niche : getActivelyBiddingOnNiches()) {
                NicheAuction auction = niche.getActiveAuction();
                if (exists(auction.getLeadingBid()) && isEqual(auction.getLeadingBid().getBidder(), this)) {
                    auctions.add(niche.getActiveAuction());
                }
            }

            leadingBidderForAuctions = Collections.unmodifiableList(auctions);
        }
        return leadingBidderForAuctions;
    }

    @Transient
    public boolean isOwnsOrIsBiddingOnAnyNiches() {
        if (!getOwnedNiches().isEmpty()) {
            return true;
        }
        if (!getBiddingOnNiches().isEmpty()) {
            return true;
        }

        return false;
    }

    @Transient
    public boolean isJoinable() {
        return false;
    }

    @Transient
    public User getUser() {
        return getAreaUser().getUser();
    }

    @Transient
    public PrimaryRole getPrimaryRole() {
        return getUser();
    }

    @Transient
    @Nullable
    public AreaUserRlm getAreaUserRlm() {
        return this;
    }

    @Transient
    public boolean isAnAreaUser() {
        return true;
    }

    @Transient
    public boolean isActiveRegisteredAreaUser() {
        return getAreaUser().isActiveRegisteredAreaUser();
    }

    @Override
    @Transient
    public Set<AreaCircle> getEffectiveAreaCircles() {
        return getAreaUser().getEffectiveAreaCircles();
    }

    @Transient
    public boolean isRegisteredUser() {
        return true;
    }

    public static AreaUser getAreaUser(AreaUserRlm areaUserRlm) {
        return areaUserRlm == null ? null : AreaUser.dao().get(areaUserRlm.getOid());
    }

    @Transient
    public SandboxedAreaUser getSandboxedAreaUser() {
        return SandboxedAreaUser.dao().get(getOid());
    }

    public static final Comparator<AreaUserRlm> DISPLAYNAME_COMPARATOR = (o1, o2) -> {
        int ret = o1.getDisplayNameResolved().compareToIgnoreCase(o2.getDisplayNameResolved());
        if (ret != 0) {
            return ret;
        }
        return OID.compareOids(o1.getOid(), o2.getOid());
    };

    @Transient
    public static AreaUserRlmDAO dao() {
        return DAOImpl.getDAO(AreaUserRlm.class);
    }
}
