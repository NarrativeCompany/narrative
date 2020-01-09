package org.narrative.network.customizations.narrative.niches.niche;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.content.base.SEOObject;
import org.narrative.network.core.content.base.services.CreateContentTask;
import org.narrative.network.core.search.IndexOperation;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.niches.elections.NicheModeratorElection;
import org.narrative.network.customizations.narrative.niches.niche.dao.NicheDAO;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheUserAssociation;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.customizations.narrative.services.ReactRoute;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.util.NetworkConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(
        name="niche_reservedName_uidx",
        columnNames = {Niche.FIELD__RESERVED_NAME__COLUMN, Niche.FIELD__PORTFOLIO__COLUMN}
        ),
        @UniqueConstraint(columnNames = {Niche.FIELD__PRETTY_URL_STRING__COLUMN, Niche.FIELD__PORTFOLIO__COLUMN})})
public class Niche implements DAOObject<NicheDAO>, SEOObject, ChannelConsumer {
    // jw: Limit the number of moderators a niche can have.
    public static final int MAXIMUM_MODERATOR_SLOTS = 30;

    public static final int DEFAULT_MODERATOR_SLOT_COUNT = 2;

    private OID oid;
    private Portfolio portfolio;
    private String name;
    private String reservedName;
    private String description;
    private NicheStatus status;
    private Timestamp suggestedDatetime;
    private Timestamp lastStatusChangeDatetime;
    private Instant renewalDatetime;
    private AreaUserRlm suggester;
    private AreaUserRlm owner;
    private String prettyUrlString;
    private NicheAuction activeAuction;
    private NicheModeratorElection activeModeratorElection;

    private Channel channel;

    private int moderatorSlots;

    private List<NicheUserAssociation> userAssociations;

    public static final String FIELD__PORTFOLIO__NAME = "portfolio";
    public static final String FIELD__NAME__NAME = "name";
    public static final String FIELD__RESERVED_NAME__NAME = "reservedName";
    public static final String FIELD__STATUS__NAME = "status";
    public static final String FIELD__LAST_STATUS_CHANGE_DATETIME__NAME = "lastStatusChangeDatetime";
    public static final String FIELD__ACTIVE_AUCTION__NAME = "activeAuction";

    public static final String FIELD__PRETTY_URL_STRING__COLUMN = "prettyUrlString";
    public static final String FIELD__RESERVED_NAME__COLUMN = FIELD__RESERVED_NAME__NAME;
    public static final String FIELD__PORTFOLIO__COLUMN = FIELD__PORTFOLIO__NAME + "_" + Portfolio.FIELD__OID__NAME;

    public static final long HOURS_BETWEEN_NICHE_SUGGESTIONS = 24;

    public static final int LEGACY_MAX_NICHE_NAME_LENGTH = 100;

    @Deprecated
    public Niche() { }

    public Niche(String name, String description, String prettyUrlString, NicheStatus status, AreaUserRlm suggester, Portfolio portfolio) {
        this.name = name;
        this.reservedName = name;
        this.description = description;
        this.status = status;
        this.suggester = suggester;
        this.portfolio = portfolio;
        this.suggestedDatetime = now();
        this.lastStatusChangeDatetime = now();
        this.prettyUrlString = prettyUrlString;
        this.moderatorSlots = DEFAULT_MODERATOR_SLOT_COUNT;
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    @Override
    public void setOid(OID oid) {
        this.oid = oid;
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_niche_portfolio")
    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    @Transient
    public Area getArea() {
        return getPortfolio().getArea();
    }

    @NotNull
    @Length(min = ChannelConsumer.MIN_NAME_LENGTH, max = LEGACY_MAX_NICHE_NAME_LENGTH)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Length(min = ChannelConsumer.MIN_NAME_LENGTH, max = LEGACY_MAX_NICHE_NAME_LENGTH)
    public String getReservedName() {
        return reservedName;
    }

    public void setReservedName(String reservedName) {
        this.reservedName = reservedName;
    }

    @Transient
    public boolean isNicheNameReserved() {
        return !isEmpty(getReservedName());
    }

    @Transient
    public String getNameForHtml() {
        return HtmlTextMassager.disableHtml(getName());
    }

    public void updateName(String name) {
        setName(name);
        setReservedName(name);
        // generate a new prettyUrlString for this niche based on its name. note that the current ID can be re-used
        // if the new name results in the same ID.
        String prettyUrlString = CreateContentTask.getPrettyUrlStringValue(Niche.dao(), this, getPortfolio().getAreaRlm(), getPortfolio(), null, name);
        setPrettyUrlString(prettyUrlString);
    }

    public void releaseReservedNameForPermanentlyRejectedNiche() {
        assert getStatus().isRejected() : "Should only call this method when the Niche is rejected!";
        // bl: clear out both the reservedName and the prettyUrlString so that both can be used again
        setReservedName(null);
        setPrettyUrlString(null);
    }

    @Length(min = ChannelConsumer.MIN_DESCRIPTION_LENGTH, max = ChannelConsumer.MAX_DESCRIPTION_LENGTH)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Transient
    public String getDescriptionForHtml() {
        return HtmlTextMassager.disableHtml(getDescription());
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    public NicheStatus getStatus() {
        return status;
    }

    public void setStatus(NicheStatus status) {
        this.status = status;
    }

    public void updateStatus(NicheStatus status) {
        // jw: short out if the status did not change, since we should not update the datetime.
        if (status == getStatus()) {
            return;
        }
        setStatus(status);
        setLastStatusChangeDatetime(now());

        // bl: set the renewal date if the niche is going active!
        if(status.isActive()) {
            // bl: we just updated the lastStatusChangeDatetime above, so simply add a year to that amount to determine
            // when the niche will be up for renewal.
            setRenewalDatetime(getLastStatusChangeDatetime().toInstant().atOffset(ZoneOffset.UTC).plus(1, ChronoUnit.YEARS).toInstant());
        } else if(getRenewalDatetime()!=null) {
            // if the niche is no longer active, then we should clear out the renewal datetime
            setRenewalDatetime(null);
        }

        updateSolrIndex();
    }

    public void updateSolrIndex() {
        IndexType.NICHE.getIndexHandler().performOperation(IndexOperation.update(getOid(), getArea().getOid()));
    }

    @NotNull
    public Timestamp getSuggestedDatetime() {
        return suggestedDatetime;
    }

    public void setSuggestedDatetime(Timestamp suggestedTimestamp) {
        this.suggestedDatetime = suggestedTimestamp;
    }

    @NotNull
    public Timestamp getLastStatusChangeDatetime() {
        return lastStatusChangeDatetime;
    }

    public void setLastStatusChangeDatetime(Timestamp lastStatusChangeDatetime) {
        this.lastStatusChangeDatetime = lastStatusChangeDatetime;
    }

    @Type(type = HibernateInstantType.TYPE)
    public Instant getRenewalDatetime() {
        return renewalDatetime;
    }

    public void setRenewalDatetime(Instant renewalDatetime) {
        this.renewalDatetime = renewalDatetime;
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_niche_suggester")
    public AreaUserRlm getSuggester() {
        return suggester;
    }

    public void setSuggester(AreaUserRlm suggesterAreaUserRlm) {
        this.suggester = suggesterAreaUserRlm;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_niche_owner")
    public AreaUserRlm getOwner() {
        return owner;
    }

    public void setOwner(AreaUserRlm ownerAreaUserRlm) {
        this.owner = ownerAreaUserRlm;
    }

    @Transient
    public AreaUserRlm getOwnerResolved() {
        if (exists(getOwner())) {
            return getOwner();
        }

        NicheAuction auction = getActiveAuction();
        if (!exists(auction) || auction.isOpenForBidding()) {
            return null;
        }

        assert exists(auction.getLeadingBid()) : "At this point, we should have a leading bid, since we would never have set the end date without one!";

        // jw: while payment is pending, the leading bidder is considered the owner.
        return auction.getLeadingBid().getBidder();
    }

    @Override
    @Length(min = NetworkConstants.MIN_PRETTY_URL_STRING_LENGTH, max = NetworkConstants.MAX_PRETTY_URL_STRING_LENGTH)
    public String getPrettyUrlString() {
        return prettyUrlString;
    }

    public void setPrettyUrlString(String prettyUrlString) {
        this.prettyUrlString = prettyUrlString;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_niche_activeAuction")
    public NicheAuction getActiveAuction() {
        return activeAuction;
    }

    public void setActiveAuction(NicheAuction activeAuction) {
        this.activeAuction = activeAuction;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_niche_activeModeratorElection")
    public NicheModeratorElection getActiveModeratorElection() {
        return activeModeratorElection;
    }

    public void setActiveModeratorElection(NicheModeratorElection activeModeratorElection) {
        this.activeModeratorElection = activeModeratorElection;
    }

    // jw: due to how the Channel.oid is derived from the ChannelConsumer, we need to make this association optional so
    //     that we can create the consumer first, and then the Channel after. In practice, all Consumers should have a
    //     Channel associated with them.
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @Cascade({org.hibernate.annotations.CascadeType.PERSIST, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.REMOVE, org.hibernate.annotations.CascadeType.DELETE, org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.REPLICATE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN, org.hibernate.annotations.CascadeType.LOCK, org.hibernate.annotations.CascadeType.EVICT})
    @JoinColumn(name = FIELD__OID__NAME)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public int getModeratorSlots() {
        return moderatorSlots;
    }

    public void setModeratorSlots(int moderatorSlots) {
        this.moderatorSlots = moderatorSlots;
    }

    public void updateModeratorSlots(int moderatorSlots) {
        assert moderatorSlots > 0 && moderatorSlots <= MAXIMUM_MODERATOR_SLOTS : "Specified an invalid number of moderator slots!";

        setModeratorSlots(moderatorSlots);
    }

    @Transient
    public int getOpenModeratorSlots() {
        // jw: this function is just a placeholder for when we support voting and ending of elections. Until then, just
        //     use getModeratorSlots since that is technically accurate.
        return getModeratorSlots();
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = NicheUserAssociation.FIELD__NICHE__NAME)
    public List<NicheUserAssociation> getUserAssociations() {
        return userAssociations;
    }

    public void setUserAssociations(List<NicheUserAssociation> userAssociations) {
        this.userAssociations = userAssociations;
    }

    @Override
    @Transient
    public String getDisplayUrl() {
        return getDisplayUrl(networkContext().isProcessingJspEmail() || networkContext().getRequestType().isClusterCp());
    }

    public String getDisplayUrl(boolean useReactRouterUrl) {
        return ReactRoute.NICHE_DETAILS.getUrl(getIdForUrl());
    }

    @Override
    @Transient
    public String getPermalinkUrl() {
        // bl: i don't think we support OID-based lookups in react, so
        // just using the standard display URL here for now.
        //return ReactRoute.NICHE_DETAILS.getUrl(getOid().toString());
        return getDisplayUrl();
    }

    @Override
    @Transient
    public String getIdForUrl() {
        // jw: to parallel nicheUtils.ts we need to return _[niche.oid] if the niche does not have a prettyUrlString.
        if (isEmpty(getPrettyUrlString())) {
            return "_"+getOid();
        }

        return getPrettyUrlString();
    }

    @Transient
    public boolean isCanCurrentRoleEditDetails() {
        return isCanAreaRoleEditDetails(areaContext().getAreaRole());
    }

    public boolean isCanAreaRoleEditDetails(AreaRole areaRole) {
        // jw: only the owner can propose a change!
        if(!isAreaRoleOwner(areaRole)) {
            return false;
        }

        // jw: at this point, the details can only be edited if there is not already a edit details vote underway.
        return !isHasOpenDetailChangeReferendums();
    }

    @Transient
    public boolean isCurrentRoleOwner() {
        return isAreaRoleOwner(areaContext().getAreaRole());
    }

    public boolean isAreaRoleOwner(AreaRole areaRole) {
        if (!areaRole.isActiveRegisteredAreaUser()) {
            return false;
        }

        // bl: there isn't really an owner until the niche is active
        if (!getStatus().isActive()) {
            return false;
        }

        return isEqual(getOwner(), areaRole.getAreaUserRlm());
    }

    private transient Boolean hasOpenDetailChangeReferendums;

    @Transient
    public boolean isHasOpenDetailChangeReferendums() {
        return exists(getEditDetailsTribunalIssue());
    }

    private transient TribunalIssue editDetailsTribunalIssue;

    @Transient
    public TribunalIssue getEditDetailsTribunalIssue() {
        // jw: this outer check will ensure that we only ever do this process once.
        if (hasOpenDetailChangeReferendums == null) {
            List<Referendum> referendums = Referendum.dao().getOpenReferendumsOfTypesForNiche(this, EnumSet.of(ReferendumType.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE));

            hasOpenDetailChangeReferendums = !referendums.isEmpty();
            if (hasOpenDetailChangeReferendums) {
                editDetailsTribunalIssue = referendums.get(0).getTribunalIssue();
            }
        }
        return editDetailsTribunalIssue;
    }

    @Transient
    public TribunalIssueType getPossibleTribunalIssueType() {
        // jw: because the lock datetime won't be set until the issue is resolved, this works out really well.
        if (!getChannel().isCanAppealStatus()) {
            return null;
        }

        if (getStatus().isActive() || getStatus().isForSale() || getStatus().isPendingPayment()) {
            return TribunalIssueType.RATIFY_NICHE;
        }

        // bl: you can only appeal a rejected niche if the name is still reserved. once the name has been released,
        // the Niche's rejected status is permanent.
        if (getStatus().isRejected() && isNicheNameReserved()) {
            return TribunalIssueType.APPROVE_REJECTED_NICHE;
        }
        return null;
    }

    @Transient
    public OID getActiveAuctionOid() {
        NicheAuction auction = getActiveAuction();
        if (!exists(auction)) {
            return null;
        }

        // jw: we keep the active auction associated with the niche until the auction has finished its payment process.
        //     Due to that, and the fact that we only want this to be exposed for auctions open for bidding in the front
        //     end, we should filter this out if the auction is not actually open for bidding.
        if (!auction.isOpenForBidding()) {
            return null;
        }

        return auction.getOid();
    }

    @Transient
    public OID getActiveModeratorElectionOid() {
        NicheModeratorElection election = getActiveModeratorElection();

        if (!exists(election)) {
            return null;
        }

        return election.getOid();
    }

    @Transient
    public OID getCurrentUserActiveInvoiceOid() {
        NicheAuction auction = getActiveAuction();

        // jw: if there is no auction, then short out.
        if (!exists(auction)) {
            return null;
        }

        // jw: if we have a auction, then delegate to that.
        return auction.getCurrentUserActiveInvoiceOid();
    }

    @Transient
    public OID getCurrentBallotBoxReferendumOid() {
        List<Referendum> referendums = Referendum.dao().getOpenReferendumsOfTypesForNiche(this, ReferendumType.NICHE_TYPES);

        if (referendums.size() == 0) {
            return null;
        } else {
            return referendums.get(0).getOid();
        }
    }

    @Transient
    public List<OID> getCurrentTribunalAppealOids() {
        List<TribunalIssue> tribunalIssues = TribunalIssue.dao().getOpenForChannel(getChannel());
        return tribunalIssues.stream().map(TribunalIssue::getOid).collect(Collectors.toList());
    }

    public static Niche getNicheForPreviewEmail(NicheStatus status) {
        return new Niche("Niche For Preview", "This is an example niche, with a description.", "niche-for-preview", status, areaContext().getAreaUserRlm(), areaContext().getPortfolio());
    }

    @Transient
    public List<TribunalIssueType> getAvailableTribunalIssueTypes() {
        List<TribunalIssueType> availableTribunalIssueTypes = new ArrayList<>();

        // bl: special handling for the owner; the owner should only be able to submit edits, but not appeal their own niche.
        if (isCurrentRoleOwner()) {
            // bl: the owner may not be able to edit details currently if there is already an edit request outstanding
            if(isCanCurrentRoleEditDetails()) {
                availableTribunalIssueTypes.add(TribunalIssueType.APPROVE_NICHE_DETAIL_CHANGE);
            }
        } else {
            TribunalIssueType possibleTribunalIssueType = this.getPossibleTribunalIssueType();

            // bl: let's now always include the appeal type so that the link will appear in the UI.
            // the front end will need to do the security check when you click the link.
            if (possibleTribunalIssueType != null) {
                availableTribunalIssueTypes.add(possibleTribunalIssueType);
            }
        }

        return availableTribunalIssueTypes;
    }

    @Transient
    public boolean isNicheModerator(PrimaryRole primaryRole) {
        // bl: for now, the only Niche moderator is the owner
        return isAreaRoleOwner(primaryRole.getLoneAreaRole());
    }

    @Override
    @Transient
    public ChannelType getChannelType() {
        return ChannelType.NICHE;
    }

    @Override
    @Transient
    public User getChannelOwner() {
        if (exists(getOwner())) {
            return getOwner().getUser();
        }

        return null;
    }

    @Transient
    @Override
    public boolean isCanCurrentRolePost() {
        // bl: anyone can post to Niches
        return true;
    }

    public static NicheDAO dao() {
        return DAOImpl.getDAO(Niche.class);
    }
}