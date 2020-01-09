package org.narrative.network.customizations.narrative.niches.ledgerentries;

import org.narrative.common.util.NameForDisplayProvider;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.propertyset.base.services.PropertiesPropertyMap;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeUtil;
import org.narrative.network.customizations.narrative.niches.ledgerentries.metadata.NicheSuggestedLedgerEntryMetadata;
import org.narrative.network.customizations.narrative.niches.ledgerentries.metadata.PublicationPaymentLedgerEntryMetadata;
import org.narrative.network.customizations.narrative.niches.ledgerentries.metadata.ReferendumVoteLedgerEntryMetadata;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/12/18
 * Time: 7:34 PM
 */
public enum LedgerEntryType implements IntegerEnum, NameForDisplayProvider {
    NICHE_SUGGESTED(0, NicheSuggestedLedgerEntryMetadata.class),
    // jw: because all votes can be changed, and unlike bids we will not keep a full history in the DB, lets track each vote here
    NICHE_REFERENDUM_VOTE(1, ReferendumVoteLedgerEntryMetadata.class),
    NICHE_REFERENDUM_RESULT(2),
    NICHE_AUCTION_RESTARTED(3),
    NICHE_AUCTION_STARTED(4),
    NICHE_BID(5),
    NICHE_INVOICE_PAID(6),
    NICHE_INVOICE_FAILED(7),
    ISSUE_REFERENDUM_VOTE(8, ReferendumVoteLedgerEntryMetadata.class),
    ISSUE_REFERENDUM_RESULT(9),
    ISSUE_REPORT(10),
    NICHE_AUCTION_ENDED(11),
    NICHE_AUCTION_WON(12),
    NICHE_AUCTION_FALLBACK_WON(13),
    NICHE_EDIT(15),
    NICHE_MODERATOR_NOMINATING_STARTED(16),
    NICHE_MODERATOR_NOMINATED(17),
    NICHE_MODERATOR_NOMINEE_WITHDRAWN(18),
    NICHE_MODERATOR_VOTING_STARTED(19),
    NICHE_MODERATOR_VOTING_COMPLETED(20),
    KYC_CERTIFICATION_APPROVED(21),
    KYC_CERTIFICATION_REVOKED(22),
    NICHE_OWNER_REMOVED(23),
    PAYMENT_CHARGEBACK(24),
    KYC_REFUND(25),
    POST_REMOVED_FROM_CHANNEL(26),
    USER_PUBLISHED_POST(27),
    USER_DELETED_POST(28),
    TRIBUNAL_USER_DELETED_POST_OR_COMMENT_AUP_VIOLATION(29),
    USER_HAD_POST_OR_COMMENT_DELETED_FOR_AUP_VIOLATION(30),
    PUBLICATION_CREATED(31),
    PUBLICATION_PAYMENT(32, PublicationPaymentLedgerEntryMetadata.class),
    PUBLICATION_EDITOR_DELETED_COMMENT(33),
    USER_HAD_COMMENT_DELETED_BY_PUBLICATION_EDITOR(34),
    ;

    private static final int MAX_LEGACY_LEDGER_ENTRY_ID = NICHE_EDIT.getId();

    public static final Set<LedgerEntryType> NICHE_TYPES = Collections.unmodifiableSet(EnumSet.of(
            NICHE_SUGGESTED,
            NICHE_AUCTION_STARTED,
            NICHE_INVOICE_PAID,
            NICHE_INVOICE_FAILED,
            NICHE_EDIT,
            ISSUE_REPORT,
            NICHE_REFERENDUM_RESULT,
            ISSUE_REFERENDUM_RESULT,
            NICHE_AUCTION_RESTARTED,
            NICHE_AUCTION_ENDED,
            NICHE_AUCTION_WON,
            NICHE_AUCTION_FALLBACK_WON,
            NICHE_MODERATOR_NOMINATING_STARTED,
            NICHE_MODERATOR_NOMINATED,
            NICHE_MODERATOR_NOMINEE_WITHDRAWN,
            NICHE_MODERATOR_VOTING_STARTED,
            NICHE_MODERATOR_VOTING_COMPLETED,
            NICHE_OWNER_REMOVED,
            POST_REMOVED_FROM_CHANNEL
    ));

    public static final Set<LedgerEntryType> TYPES_WITH_ACTOR = Collections.unmodifiableSet(EnumSet.of(
            NICHE_SUGGESTED,
            NICHE_EDIT,
            NICHE_REFERENDUM_VOTE,
            NICHE_BID,
            NICHE_INVOICE_PAID,
            NICHE_INVOICE_FAILED,
            ISSUE_REFERENDUM_VOTE,
            ISSUE_REPORT,
            NICHE_AUCTION_WON,
            NICHE_AUCTION_FALLBACK_WON,
            NICHE_MODERATOR_NOMINATED,
            NICHE_MODERATOR_NOMINEE_WITHDRAWN,
            KYC_CERTIFICATION_APPROVED,
            KYC_CERTIFICATION_REVOKED,
            NICHE_OWNER_REMOVED,
            PAYMENT_CHARGEBACK,
            KYC_REFUND,
            POST_REMOVED_FROM_CHANNEL,
            USER_PUBLISHED_POST,
            USER_DELETED_POST,
            TRIBUNAL_USER_DELETED_POST_OR_COMMENT_AUP_VIOLATION,
            USER_HAD_POST_OR_COMMENT_DELETED_FOR_AUP_VIOLATION,
            PUBLICATION_CREATED,
            PUBLICATION_PAYMENT,
            PUBLICATION_EDITOR_DELETED_COMMENT,
            USER_HAD_COMMENT_DELETED_BY_PUBLICATION_EDITOR
    ));

    public static final Set<LedgerEntryType> PUBLICATION_TYPES = Collections.unmodifiableSet(EnumSet.of(
            PUBLICATION_CREATED,
            ISSUE_REPORT,
            ISSUE_REFERENDUM_RESULT,
            POST_REMOVED_FROM_CHANNEL,
            PUBLICATION_PAYMENT,
            PUBLICATION_EDITOR_DELETED_COMMENT
    ));

    private final int id;
    private final Class<? extends LedgerEntryMetadata> metadataClass;

    LedgerEntryType(int id) {
        this(id, null);
    }

    LedgerEntryType(int id, Class<? extends LedgerEntryMetadata> metadataClass) {
        this.id = id;
        this.metadataClass = metadataClass;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getNameForDisplay() {
        // jw: so that we don't need to keep adding legacy wordlets let's ignore anything past 15
        if (getId() > MAX_LEGACY_LEDGER_ENTRY_ID) {
            return name();
        }
        return wordlet("ledgerEntryType." + this);
    }

    public boolean isForNicheDetailsPage() {
        return NICHE_TYPES.contains(this);
    }

    public boolean isHasActor() {
        return TYPES_WITH_ACTOR.contains(this);
    }

    public String getDescriptionWordlet() {
        assert isForNicheDetailsPage() : "Should only be called for entries with description on niche details page";

        return "ledgerEntryType.description." + this;
    }

    public String getProfilePageDescriptionWordletKey() {
        assert isHasActor() : "Should only be called for entries with description on profile page";

        return "ledgerEntryType.descriptionForMemberProfile." + this;
    }

    public Class<? extends LedgerEntryMetadata> getMetadataClass() {
        return metadataClass;
    }

    public <T extends LedgerEntryMetadata> T getMetadata(Properties properties) {
        return (T) PropertySetTypeUtil.getPropertyWrapper(metadataClass, new PropertiesPropertyMap(properties));
    }

    public boolean isNicheSuggested() {
        return this == NICHE_SUGGESTED;
    }

    public boolean isNicheReferendumVote() {
        return this == NICHE_REFERENDUM_VOTE;
    }

    public boolean isNicheReferendumResult() {
        return this == NICHE_REFERENDUM_RESULT;
    }

    public boolean isNicheAuctionStarted() {
        return this == NICHE_AUCTION_STARTED;
    }

    public boolean isNicheAuctionRestarted() {
        return this == NICHE_AUCTION_RESTARTED;
    }

    public boolean isNicheBid() {
        return this == NICHE_BID;
    }

    public boolean isNicheInvoicePaid() {
        return this == NICHE_INVOICE_PAID;
    }

    public boolean isNicheInvoiceFailed() {
        return this == NICHE_INVOICE_FAILED;
    }

    public boolean isIssueReport() {
        return this == ISSUE_REPORT;
    }

    public boolean isIssueReferendumVote() {
        return this == ISSUE_REFERENDUM_VOTE;
    }

    public boolean isIssueReferendumResult() {
        return this == ISSUE_REFERENDUM_RESULT;
    }

    public boolean isNicheAuctionEnded() {
        return this == NICHE_AUCTION_ENDED;
    }

    public boolean isNicheAuctionWon() {
        return this == NICHE_AUCTION_WON;
    }

    public boolean isAuctionFallbackWon() {
        return this == NICHE_AUCTION_FALLBACK_WON;
    }

    public boolean isNicheEdit() {
        return this == NICHE_EDIT;
    }

    public boolean isPublicationPayment() {
        return this == PUBLICATION_PAYMENT;
    }
}
