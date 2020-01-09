package org.narrative.network.customizations.narrative.niches.ledgerentries;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.HibernatePropertiesType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.niches.ledgerentries.dao.LedgerEntryDAO;
import org.narrative.network.customizations.narrative.niches.ledgerentries.metadata.ReferendumVoteLedgerEntryMetadata;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.CreateReputationEventsFromLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.LedgerEntryReputationEventTaskType;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueReport;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Date;
import java.util.Properties;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class LedgerEntry implements DAOObject<LedgerEntryDAO> {

    private OID oid;
    private AreaUserRlm actor;
    private LedgerEntryType type;
    private Instant eventDatetime;

    private Channel channel;
    private TribunalIssue issue;

    private Referendum referendum;
    // jw: unlike auctions below, we do not keep all votes in the database, so we cannot reliably reference
    //     a votes state as it changes over time. Due to that, we will reference the referendum, since that
    //     is immutable, but will cache the state of the users vote in the metadata.

    private NicheAuction auction;
    private NicheAuctionBid auctionBid;
    private TribunalIssueReport issueReport;

    private Election election;

    // jw: this is necessary for generic invoice events like chargebacks
    private Invoice invoice;

    private OID contentOid;
    private OID commentOid;

    private User author;

    // jw: there are a few entries that require ancillary data, so let's use a properties object to store that, so the table does not have to grow wider to support it.
    private Properties properties;

    public static final String FIELD__EVENT_DATETIME__NAME = "eventDatetime";

    @Deprecated
    public LedgerEntry() { }

    public LedgerEntry(AreaUserRlm actor, LedgerEntryType type) {
        this.actor = actor;
        this.type = type;
        this.eventDatetime = Instant.now();

        // jw: if the type has metadata, let's assume that the properties are going to be affected right after creation!
        if (type.getMetadataClass() != null) {
            this.properties = new Properties();
        }

        // jw: because a LedgerEntry can be associated to so many different types of data, let's not assume any of
        //     them through the constructor, and trust that the caller will set it up appropriately later.
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

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_ledgerEntry_actor")
    public AreaUserRlm getActor() {
        return actor;
    }

    public void setActor(AreaUserRlm actor) {
        this.actor = actor;
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    public LedgerEntryType getType() {
        return type;
    }

    public void setType(LedgerEntryType type) {
        this.type = type;
    }

    @NotNull
    @Type(type = HibernateInstantType.TYPE)
    public Instant getEventDatetime() {
        return eventDatetime;
    }

    public void setEventDatetime(Instant eventDatetime) {
        this.eventDatetime = eventDatetime;
    }

    @Transient
    public Date getEventDatetimeForDisplay() {
        return new Date(getEventDatetime().toEpochMilli());
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_ledgerEntry_channel")
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Transient
    public void setChannelForConsumer(ChannelConsumer channelConsumer) {
        setChannel(channelConsumer.getChannel());
    }

    @Transient
    public Niche getNicheResolved() {
        return getChannelConsumer(ChannelType.NICHE);
    }

    @Transient
    public Publication getPublicationResolved() {
        return getChannelConsumer(ChannelType.PUBLICATION);
    }

    private <C extends ChannelConsumer> C getChannelConsumer(ChannelType forChannelType) {
        // jw: first, we will use the channel from the ledger entry.
        Channel channel = getChannel();

        // jw: if we don't have that, then let's see if we have a issue we can try to get the consumer from.
        if (!exists(channel) && getIssue()!=null) {
            channel = getIssue().getChannel();
        }

        // jw: finally, since this will be called for both Niches and Publications we need to only fetch the consumer if
        //     the channel matches the type.
        if (exists(channel) && channel.getType() == forChannelType) {
            return channel.getConsumer();
        }

        return null;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_ledgerEntry_issue")
    public TribunalIssue getIssue() {
        return issue;
    }

    public void setIssue(TribunalIssue issue) {
        this.issue = issue;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_ledgerEntry_referendum")
    public Referendum getReferendum() {
        return referendum;
    }

    public void setReferendum(Referendum referendum) {
        this.referendum = referendum;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_ledgerEntry_auction")
    public NicheAuction getAuction() {
        return auction;
    }

    public void setAuction(NicheAuction auction) {
        this.auction = auction;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_ledgerEntry_auctionBid")
    public NicheAuctionBid getAuctionBid() {
        return auctionBid;
    }

    public void setAuctionBid(NicheAuctionBid auctionBid) {
        this.auctionBid = auctionBid;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_ledgerEntry_issueReport")
    public TribunalIssueReport getIssueReport() {
        return issueReport;
    }

    public void setIssueReport(TribunalIssueReport issueReport) {
        this.issueReport = issueReport;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_ledgerEntry_election")
    public Election getElection() {
        return election;
    }

    public void setElection(Election election) {
        this.election = election;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_ledgerEntry_invoice")
    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    /**
     * the content OID associated with this ledger entry. since content can be deleted and we don't want to delete
     * ledger entries, this can't have a foreign key.
     * @return the content OID associated with this ledger entry
     */
    public OID getContentOid() {
        return contentOid;
    }

    public void setContentOid(OID contentOid) {
        this.contentOid = contentOid;
    }

    @Transient
    public Content getContent() {
        Content content = Content.dao().get(getContentOid());
        // bl: this is only use for DTO mapping, so we don't want a chance to return an uninitialized proxy
        // that may point to an object that doesn't exist.
        return exists(content) ? content : null;
    }

    public OID getCommentOid() {
        return commentOid;
    }

    public void setCommentOid(OID commentOid) {
        this.commentOid = commentOid;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_ledgerEntry_author")
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Type(type = HibernatePropertiesType.TYPE)
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private transient LedgerEntryMetadata metadata;

    @Transient
    public <T extends LedgerEntryMetadata> T getMetadata() {
        // jw: only try and saturate the metadata if the type has a metadata class
        if (metadata == null && getType().getMetadataClass() != null) {
            metadata = getType().getMetadata(getProperties());
        }

        return (T) metadata;
    }

    // jw: this is used by the Rest Mapper to determine if a referendum vote event was positive or negative
    @Transient
    public Boolean isWasReferendumVotedFor() {
        // jw: if it's of an expected type, then lets find out how the person voted
        if (getType().isIssueReferendumVote() || getType().isNicheReferendumVote()) {
            ReferendumVoteLedgerEntryMetadata metadata = getMetadata();

            return metadata.isVoteForReferendum();
        }

        // jw: otherwise, just return null which means that this does not apply.
        return null;
    }

    @Transient
    public CreateReputationEventsFromLedgerEntryTask getCreateReputationEventTask() {
        return LedgerEntryReputationEventTaskType.getCreateReputationEventTask(this);
    }

    // jw: this is necessary for events which could have had a security deposit associated with them.
    @Transient
    public UsdValue getSecurityDepositValue() {
        if (getType().isNicheInvoiceFailed()) {
            // jw: first, let's look to see if they had a Security Deposit for this auction.
            NicheAuctionSecurityDeposit securityDeposit = NicheAuctionSecurityDeposit.dao().getSecurityDeposit(getAuction(), getActor().getUser());
            if (exists(securityDeposit) && securityDeposit.getInvoice().getFiatPayment().getStatus().isPaid()) {
                return securityDeposit.getInvoice().getUsdValue();
            }
        }

        return null;
    }

    public static LedgerEntryDAO dao() {
        return DAOImpl.getDAO(LedgerEntry.class);
    }
}