package org.narrative.network.customizations.narrative.niches.nicheauction;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.HibernateNrveValueType;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.IPDateUtil;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.niches.nicheauction.dao.NicheAuctionBidDAO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

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
public class NicheAuctionBid implements DAOObject<NicheAuctionBidDAO> {

    private OID oid;
    private NicheAuction auction;
    private AreaUserRlm bidder;
    private BidStatus status;
    private NrveValue nrveBid;
    private NrveValue maxNrveBid;
    private Instant bidDatetime;

    private NicheAuctionBid createdFromBid;

    public final static String FIELD__AUCTION__NAME = "auction";
    public final static String FIELD__BIDDER__NAME = "bidder";
    public final static String FIELD__STATUS__NAME = "status";
    public final static String FIELD__BID_DATETIME__NAME = "bidDatetime";

    public static final NrveValue MIN_BID_INCREMENT_NRVE = NrveValue.ONE;

    @Deprecated
    public NicheAuctionBid() { }

    public NicheAuctionBid(NicheAuction auction, AreaUserRlm bidder, BidStatus status, NrveValue nrveBid, NrveValue maxNrveBid, Instant bidDatetime) {
        this.auction = auction;
        this.bidder = bidder;
        this.status = status;
        this.nrveBid = nrveBid;
        this.maxNrveBid = maxNrveBid;
        this.bidDatetime = bidDatetime;
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
    @ForeignKey(name = "fk_nicheAuctionBid_auction")
    public NicheAuction getAuction() {
        return auction;
    }

    public void setAuction(NicheAuction auction) {
        this.auction = auction;
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_nicheAuctionBid_bidder")
    public AreaUserRlm getBidder() {
        return bidder;
    }

    public void setBidder(AreaUserRlm bidderAreaUserRlm) {
        this.bidder = bidderAreaUserRlm;
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    public BidStatus getStatus() {
        return status;
    }

    public void setStatus(BidStatus status) {
        this.status = status;
    }

    @NotNull
    @Type(type = HibernateNrveValueType.TYPE)
    public NrveValue getNrveBid() {
        return nrveBid;
    }

    public void setNrveBid(NrveValue nrveBid) {
        this.nrveBid = nrveBid;
    }

    @Transient
    public NrveUsdValue getBidAmount() {
        // bl: make sure the bid USD amount is calculated based on the fixed NRVE price of the auction!
        return new NrveUsdValue(getNrveBid(), getAuction().getNrveUsdPriceResolved());
    }

    @NotNull
    @Type(type = HibernateNrveValueType.TYPE)
    public NrveValue getMaxNrveBid() {
        return maxNrveBid;
    }

    public void setMaxNrveBid(NrveValue maxNrveBid) {
        this.maxNrveBid = maxNrveBid;
    }

    @Transient
    public NrveUsdValue getMaxBidAmount() {
        // bl: make sure the max bid USD amount is calculated based on the fixed NRVE price of the auction!
        return new NrveUsdValue(getMaxNrveBid(), getAuction().getNrveUsdPriceResolved());
    }

    @NotNull
    @Type(type = HibernateInstantType.TYPE)
    public Instant getBidDatetime() {
        return bidDatetime;
    }

    public void setBidDatetime(Instant bidDatetime) {
        this.bidDatetime = bidDatetime;
    }

    @Transient
    public Date getBidDatetimeForDisplay() {
        return new Date(getBidDatetime().toEpochMilli());
    }

    @OneToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    public NicheAuctionBid getCreatedFromBid() {
        return createdFromBid;
    }

    public void setCreatedFromBid(NicheAuctionBid autoBidFrom) {
        this.createdFromBid = autoBidFrom;
    }

    private static final NrveValue LEADING_BID_NRVE_VALUE_FOR_PREVIEW = new NrveValue(10L * NrveValue.NEURONS_PER_NRVE.longValue());
    private static final NrveValue OUTBID_NRVE_VALUE_FOR_PREVIEW = new NrveValue(9L * NrveValue.NEURONS_PER_NRVE.longValue());

    public static NicheAuctionBid getLeadingBidForPreviewEmail(NicheAuction auctionForPreview) {
        NicheAuctionBid bid = getBidForPreviewEmail(auctionForPreview, BidStatus.LEADING, LEADING_BID_NRVE_VALUE_FOR_PREVIEW);
        auctionForPreview.setLeadingBid(bid);
        auctionForPreview.setEndDatetime(new Timestamp(System.currentTimeMillis() + (IPDateUtil.HOUR_IN_MS * 22L) + (IPDateUtil.MINUTE_IN_MS * 23L)));

        return bid;
    }

    public static NicheAuctionBid getOutbidForPreviewEmail(NicheAuction auctionForPreview) {
        return getBidForPreviewEmail(auctionForPreview, BidStatus.OUTBID, OUTBID_NRVE_VALUE_FOR_PREVIEW);
    }

    private static NicheAuctionBid getBidForPreviewEmail(NicheAuction auctionForPreview, BidStatus status, NrveValue nrveBid) {
        return new NicheAuctionBid(auctionForPreview, areaContext().getAreaUserRlm(), status, nrveBid, nrveBid, Instant.now());
    }

    public static NicheAuctionBidDAO dao() {
        return DAOImpl.getDAO(NicheAuctionBid.class);
    }
}