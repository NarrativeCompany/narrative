package org.narrative.network.customizations.narrative.niches.nicheauction;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.util.IPDateUtil;
import org.narrative.config.StaticConfig;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.invoices.InvoiceType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.nicheauction.dao.NicheAuctionDAO;
import org.narrative.network.customizations.narrative.niches.services.DatetimeCountdownProvider;
import org.narrative.network.customizations.narrative.paypal.services.PayPalCheckoutDetails;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionRevokedError;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.service.api.model.NrveUsdPriceDTO;
import org.narrative.network.customizations.narrative.service.api.model.NrveUsdPriceFields;
import org.narrative.network.customizations.narrative.service.api.model.permissions.BidOnNichesRevokeReason;
import org.narrative.network.customizations.narrative.services.ReactRoute;
import org.narrative.network.shared.security.AccessViolation;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Date;
import java.util.List;

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
public class NicheAuction implements DAOObject<NicheAuctionDAO>, DatetimeCountdownProvider {
    private OID oid;
    private Niche niche;
    private Timestamp startDatetime;
    private Timestamp endDatetime;

    private NicheAuctionBid leadingBid;
    private NicheAuctionInvoice activeInvoice;

    private BigDecimal nrveUsdPrice;

    private List<NicheAuctionBid> auctionBids;

    public static final String FIELD__END_DATETIME__NAME = "endDatetime";
    public static final String FIELD__START_DATETIME__NAME = "startDatetime";
    public static final String FIELD__NICHE__NAME = "niche";
    public static final String FIELD__ACTIVE_INVOICE__NAME = "activeInvoice";

    public static final long MS_FROM_FIRST_BID_TO_END_AUCTION = IPDateUtil.HOUR_IN_MS * 72L;

    public static final BigDecimal MINIMUM_BID_USD = BigDecimal.valueOf(75);

    @Deprecated
    public NicheAuction() { }

    public NicheAuction(Niche niche) {
        this.niche = niche;
        this.startDatetime = now();
        // jw: the endDatetime should be setup after the first bid is received.
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
    @ForeignKey(name = "fk_nicheAuction_niche")
    public Niche getNiche() {
        return niche;
    }

    public void setNiche(Niche niche) {
        this.niche = niche;
    }

    @NotNull
    public Timestamp getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(Timestamp startDatetime) {
        this.startDatetime = startDatetime;
    }

    public Timestamp getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(Timestamp endDatetime) {
        this.endDatetime = endDatetime;
    }

    @Transient
    public boolean isOpenForBidding() {
        return getEndDatetime() == null || now().before(getEndDatetime());
    }

    @ManyToOne()
    @ForeignKey(name = "fk_nicheAuction_leadingBid")
    public NicheAuctionBid getLeadingBid() {
        return leadingBid;
    }

    public void setLeadingBid(NicheAuctionBid leadingBid) {
        this.leadingBid = leadingBid;
    }

    @Column(columnDefinition = NrveUsdPriceFields.NRVE_USD_PRICE_COLUMN_DEFINITION)
    public BigDecimal getNrveUsdPrice() {
        return nrveUsdPrice;
    }

    public void setNrveUsdPrice(BigDecimal nrveUsdPrice) {
        this.nrveUsdPrice = nrveUsdPrice;
    }

    private transient BigDecimal nrveUsdPriceResolved;

    @Transient
    public BigDecimal getNrveUsdPriceResolved() {
        if(getNrveUsdPrice()!=null) {
            return getNrveUsdPrice();
        }
        // bl: just derive the value once and use it consistently so that it won't change for the same instance of a NicheAuction
        if(nrveUsdPriceResolved==null) {
            nrveUsdPriceResolved = GlobalSettingsUtil.getGlobalSettings().getNrveUsdPrice();
        }
        return nrveUsdPriceResolved;
    }

    @Transient
    public NrveUsdValue getStartingBid() {
        return new NrveUsdValue(getStartingBidNrve(getNrveUsdPriceResolved()), getNrveUsdPriceResolved());
    }

    public static NrveValue getStartingBidNrve(BigDecimal nrveUsdPrice) {
        return NrveValue.getNrveValueFromUsd(MINIMUM_BID_USD, nrveUsdPrice, 2, RoundingMode.DOWN);
    }

    @Transient
    public NrveUsdPriceDTO getNrveUsdPriceFields() {
        // bl: let's have the price expire after 30 minutes for auctions.
        return NrveUsdPriceDTO.create(getNrveUsdPriceResolved(), Duration.ofMinutes(30));
    }

    @ManyToOne()
    @ForeignKey(name = "fk_nicheAuction_activeInvoice")
    public NicheAuctionInvoice getActiveInvoice() {
        return activeInvoice;
    }

    public void setActiveInvoice(NicheAuctionInvoice activeInvoice) {
        this.activeInvoice = activeInvoice;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = NicheAuctionBid.FIELD__AUCTION__NAME, cascade = CascadeType.ALL)
    @OrderBy(NicheAuctionBid.FIELD__BID_DATETIME__NAME + " desc")
    public List<NicheAuctionBid> getAuctionBids() {
        return auctionBids;
    }

    public void setAuctionBids(List<NicheAuctionBid> auctionBids) {
        this.auctionBids = auctionBids;
    }

    private transient Long totalBidCount;

    @Transient
    public long getTotalBidCount() {
        if (totalBidCount == null) {
            totalBidCount = NicheAuctionBid.dao().getTotalBidCount(this);
        }
        return totalBidCount;
    }

    public void cacheTotalBidCount(long totalBidCount) {
        this.totalBidCount = totalBidCount;
    }

    @Transient
    public NrveValue getMinimumBidNrveForCurrentRole() {
        AreaRole areaRole = areaContext().getAreaRole();
        assert areaRole.isActiveRegisteredAreaUser() : "Should only ever call this for a logged in user!";

        // bl: if there are no bids yet, then the minimum bid for everyone is the starting bid
        if (!exists(getLeadingBid())) {
            return getStartingBid().getNrve();
        }
        // jw: for the leading bidder, we will allow them to adjust their bid down to whatever their leading bid is at.
        if (isEqual(getLeadingBid().getBidder(), areaRole.getAreaUserRlm())) {
            return getLeadingBid().getNrveBid();
        }
        // jw: for anyone else, they must bid the minimum above the current active bid.
        return getLeadingBid().getNrveBid().add(NicheAuctionBid.MIN_BID_INCREMENT_NRVE);
    }

    @Transient
    public boolean isCanCurrentRoleBidOnAuction() {
        return isCanAreaRoleBidOnAuction(areaContext().getAreaRole());
    }

    @Transient
    public void checkCanCurrentRoleBidOnAuction() {
        checkCanAreaRoleBidOnAuction(areaContext().getAreaRole());
    }

    public boolean isCanAreaRoleBidOnAuction(AreaRole areaRole) {
        try {
            checkCanAreaRoleBidOnAuction(areaRole);
            return true;

        } catch (AccessViolation ignore) {
            return false;
        }
    }

    public void checkCanAreaRoleBidOnAuction(AreaRole areaRole) {
        // jw: if the auction is not open for bidding, then they cannot bid no matter what
        if (!isOpenForBidding()) {
            throw new AccessViolation(wordlet("nicheAuction.biddingHasEnded", getNiche().getName()));
        }

        // jw: if the niche is not for sale, no bidding!
        if (!getNiche().getStatus().isForSale()) {
            throw new AccessViolation(wordlet("nicheAuction.nicheIsNoLongerForSale", getNiche().getName()));
        }

        // jw: check their ability to bid generally
        try {
            NarrativePermissionType.BID_ON_NICHES.checkRight(areaRole);
        } catch(NarrativePermissionRevokedError e) {
            // jw: we need to handle BidOnNichesRevokedReason a bit differently.
            if (e.getRevokeReason() != null && e.getRevokeReason() instanceof BidOnNichesRevokeReason) {
                BidOnNichesRevokeReason reason = (BidOnNichesRevokeReason)e.getRevokeReason();
                if(reason.isNicheSlotsFull()) {
                    // bl: if your niche slots are all full, it's possible that this niche is one of them!
                    // so, we need to check to see if you are the leading bidder on this niche, in which
                    // case you actually _can_ bid on this niche.
                    if (exists(areaRole.getAreaUserRlm().getNicheAssociation(getNiche()))) {
                        return;
                    }

                // jw: if a security deposit is required then let's allow the user through if they can participate once
                //     they have made one.
                } else if (reason.isSecurityDepositRequired()) {
                    if (canAreaRoleParticipateWhenSecurityDepositIsRequired(areaRole)) {
                        return;
                    }
                }
            }

            // jw: if we fell down to here then the error should not be ignored, and we need to use it.
            throw e;
        }
    }

    public boolean canAreaRoleParticipateWhenSecurityDepositIsRequired(AreaRole areaRole) {
        // jw: guests obviously cannot.
        if (!areaRole.isActiveRegisteredAreaUser()) {
            return false;
        }

        // jw: if they have made a security deposit then let them participate.
        NicheAuctionSecurityDeposit securityDeposit = NicheAuctionSecurityDeposit.dao().getSecurityDeposit(this, areaRole.getUser());
        if (exists(securityDeposit)) {
            // jw: at this point we will short out, but we will only allow them to participate if their invoice is still paid.
            //     If for whatever reason it is no longer paid they should not be able to participate in this auction.
            return securityDeposit.getInvoice().getFiatPayment().getStatus().isPaid();
        }

        // jw: finally, if the user has participated in this auction already then we should allow them through.
        return exists(getLatestBidForCurrentUser());
    }

    @Transient
    public String getDisplayUrl() {
        return ReactRoute.AUCTION_DETAILS.getUrl(getOid().toString());
    }

    @Override
    @Transient
    public Date getCountdownTarget() {
        return getEndDatetime();
    }

    @Override
    @Transient
    public Date getCountdownEndingSoonTarget() {
        return DatetimeCountdownProvider.getCountdownEndingSoonTarget(this);
    }

    @Override
    @Transient
    public boolean isCountdownTargetLocked() {
        return true;
    }

    @Override
    @Transient
    public String getCountdownRefreshUrl() {
        return null;
    }

    @Transient
    public NrveValue getCurrentMaxBidForUser() {
        if (exists(leadingBid) && isEqual(leadingBid.getBidder(), areaContext().getAreaUserRlm())) {
            return leadingBid.getMaxNrveBid();
        }
        return null;
    }

    private static final int AJAX_POLLING_INTERVAL_MS = 10 * IPDateUtil.SECOND_IN_MS;
    private static final int LAST_MINUTE_AJAX_POLLING_INTERVAL_MS = IPDateUtil.SECOND_IN_MS;

    @Transient
    public int getAjaxPollingIntervalMs() {
        if (getEndDatetime() != null) {
            Timestamp lastMinuteOfAuction = new Timestamp(getEndDatetime().getTime() - IPDateUtil.MINUTE_IN_MS);

            if (now().after(lastMinuteOfAuction)) {
                return LAST_MINUTE_AJAX_POLLING_INTERVAL_MS;
            }
        }

        return AJAX_POLLING_INTERVAL_MS;
    }

    @Transient
    public Boolean isCurrentRoleOutbid() {
        if (!areaContext().getAreaRole().isActiveRegisteredAreaUser()){
            return null;
        } else {
            return isCurrentUserOutbid();
        }
    }

    @Transient
    private boolean isCurrentUserOutbid() {
        // jw: if we do not have a leading bid, there is no way to be outbid
        if (!exists(getLeadingBid())) {
            return false;
        }

        // jw: if we are the leading bidder, then they are not outbid
        if (getLeadingBid().getBidder().getUser().isCurrentUserThisUser()) {
            return false;
        }
        // jw: if the user has a bid, then they have been outbid
        return exists(getLatestBidForCurrentUser());
    }

    @Transient
    public OID getCurrentUserActiveInvoiceOid() {
        // jw: if this auction is open for bidding, then there should never be a invoice.
        if (isOpenForBidding()) {
            return null;
        }

        // jw: now, let's ensure we have a invoice.
        NicheAuctionInvoice auctionInvoice = getActiveInvoice();
        if (!exists(auctionInvoice)) {
            return null;
        }

        // jw: if the invoice is no longer invoiced, then don't return it.
        if (!auctionInvoice.getInvoice().getStatus().isInvoiced()) {
            return null;
        }

        // jw: if this invoice is for a different user, don't include it.
        if (!auctionInvoice.getBid().getBidder().getUser().isCurrentUserThisUser()) {
            return null;
        }

        return auctionInvoice.getOid();
    }

    @Transient
    public Boolean getCurrentUserBypassesSecurityDepositRequirement() {
        // jw: no point doing any of this if the auction is not active.
        if (!isOpenForBidding()) {
            return null;
        }

        // finally, let's just see if they can participate even if a security deposit is generally necessary
        if (canAreaRoleParticipateWhenSecurityDepositIsRequired(areaContext().getAreaRole())) {
            return true;
        }

        // jw: let's just return null if they have not so that the json remains a bit lighter.
        return null;
    }

    @Transient
    public PayPalCheckoutDetails getSecurityDepositPayPalCheckoutDetails() {
        // jw: return null for guests so nothing is included in the json.
        if (!areaContext().getAreaRole().isActiveRegisteredAreaUser()) {
            return null;
        }

        // jw: no point doing any of this if the auction is not active.
        if (!isOpenForBidding()) {
            return null;
        }

        // jw: let's short out if the user has already made a security deposit
        NicheAuctionSecurityDeposit securityDeposit = NicheAuctionSecurityDeposit.dao().getSecurityDeposit(this, areaContext().getUser());
        if (exists(securityDeposit)) {
            return null;
        }

        // jw: we need to check their permission and only return a PayPalCheckoutDetails if the user could participate if
        //     they made a security deposit. By checkout through permissions we are guaranteeing uniformity of requirements
        AreaRole areaRole = areaContext().getAreaRole();
        try {
            NarrativePermissionType.BID_ON_NICHES.checkRight(areaRole);

        } catch(NarrativePermissionRevokedError e) {
            // jw: we need to handle BidOnNichesRevokedReason a bit differently.
            if (e.getRevokeReason() != null && e.getRevokeReason() instanceof BidOnNichesRevokeReason) {
                BidOnNichesRevokeReason reason = (BidOnNichesRevokeReason)e.getRevokeReason();
                // jw: the ability to make fiat payments is handles as part of the security check. We never would get this
                //     reason if the user could not make fiat payments.
                if (reason.isSecurityDepositRequired()) {
                    return InvoiceType.NICHE_AUCTION_SECURITY_DEPOSIT.getImmediatePaymentPayPalCheckoutDetails(
                            areaRole.getUser(),
                            StaticConfig.getBean(NarrativeProperties.class)
                    );
                }
            }
        }

        return null;
    }

    private transient Boolean hasFetchedLatestBidForCurrentUser;
    private transient NicheAuctionBid latestBidForCurrentUser;

    @Transient
    public NicheAuctionBid getLatestBidForCurrentUser() {
        if (hasFetchedLatestBidForCurrentUser==null) {
            if (areaContext().isLoggedInUser()) {
                latestBidForCurrentUser = NicheAuctionBid.dao().getLatestBidForUser(this, areaContext().getAreaUserRlm());
            }

            hasFetchedLatestBidForCurrentUser = Boolean.TRUE;
        }
        return latestBidForCurrentUser;
    }

    public void saveLedgerEntry(NicheAuctionBid bid, LedgerEntryType type) {
        saveLedgerEntry(bid, type, 0);
    }

    public void saveLedgerEntry(NicheAuctionBid bid, LedgerEntryType type, int plusMillis) {
        LedgerEntry ledgerEntry = new LedgerEntry(null, type);
        ledgerEntry.setChannelForConsumer(getNiche());
        ledgerEntry.setAuction(this);
        if (exists(bid)) {
            // jw: not all ledger entries for bids are specific to the bidder (Niche Auction Ended for example)
            if (type.isHasActor()) {
                ledgerEntry.setActor(bid.getBidder());
            }
            ledgerEntry.setAuctionBid(bid);
        }

        // jw: first, let's adjust the eventDatetime if necessary
        if (plusMillis != 0) {
            //mk: some events are saved one after another so add millis for sort order to stay correct
            ledgerEntry.setEventDatetime(ledgerEntry.getEventDatetime().plusMillis(plusMillis));
        }

        networkContext().doGlobalTask(new SaveLedgerEntryTask(ledgerEntry));
    }

    public static NicheAuctionDAO dao() {
        return DAOImpl.getDAO(NicheAuction.class);
    }
}