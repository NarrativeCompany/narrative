import { LedgerEntryType } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { IconType } from '../components/CustomIcon';

// jw: let's define the LedgerEntryTypeHelper that will provide all the extra helper logic for LedgerEntryTypes
export class LedgerEntryTypeHelper {
  type: LedgerEntryType;
  ledgerEntryIcon: IconType;

  constructor(
    type: LedgerEntryType,
    ledgerEntryIcon: IconType
  ) {
    this.type = type;
    this.ledgerEntryIcon = ledgerEntryIcon;
  }

  isCanHaveSecurityDepositValue() {
    return this.type === LedgerEntryType.NICHE_INVOICE_FAILED;
  }

  isAddPublicationPaymentTypeForLedgerEntryTitle(): boolean {
    return this.type === LedgerEntryType.PUBLICATION_PAYMENT;
  }

  isExpectPostForLedgerEntryTitle() {
    switch (this.type) {
      case LedgerEntryType.POST_REMOVED_FROM_CHANNEL:
      case LedgerEntryType.USER_PUBLISHED_POST:
        return true;
      default:
        return false;
    }
  }

  isAddReferendumTypeForLedgerEntryTitle() {
    switch (this.type) {
      case LedgerEntryType.NICHE_REFERENDUM_VOTE:
      case LedgerEntryType.ISSUE_REFERENDUM_VOTE:
      case LedgerEntryType.ISSUE_REPORT:
      case LedgerEntryType.ISSUE_REFERENDUM_RESULT:
        return true;
      default:
        return false;
    }
  }
}

// jw: next: lets create the lookup of LedgerEntryType to helper object

const ledgerEntryTypeHelpers: {[key: number]: LedgerEntryTypeHelper} = [];
// jw: make sure to register these in the order you want them to display.
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_SUGGESTED] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_SUGGESTED,
  'suggested'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_REFERENDUM_VOTE] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_REFERENDUM_VOTE,
  'review'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_REFERENDUM_RESULT] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_REFERENDUM_RESULT,
  'complete'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_AUCTION_RESTARTED] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_AUCTION_RESTARTED,
  'auction'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_AUCTION_STARTED] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_AUCTION_STARTED,
  'auction'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_BID] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_BID,
  'auction'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_INVOICE_PAID] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_INVOICE_PAID,
  'payment'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_INVOICE_FAILED] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_INVOICE_FAILED,
  'failure'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_EDIT] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_EDIT,
  'edit'
);
ledgerEntryTypeHelpers[LedgerEntryType.ISSUE_REFERENDUM_VOTE] = new LedgerEntryTypeHelper(
  LedgerEntryType.ISSUE_REFERENDUM_VOTE,
  'appeals'
);
ledgerEntryTypeHelpers[LedgerEntryType.ISSUE_REFERENDUM_RESULT] = new LedgerEntryTypeHelper(
  LedgerEntryType.ISSUE_REFERENDUM_RESULT,
  'complete'
);
ledgerEntryTypeHelpers[LedgerEntryType.ISSUE_REPORT] = new LedgerEntryTypeHelper(
  LedgerEntryType.ISSUE_REPORT,
  'appeals'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_AUCTION_ENDED] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_AUCTION_ENDED,
  'auction'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_AUCTION_WON] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_AUCTION_WON,
  'auction'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_AUCTION_FALLBACK_WON] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_AUCTION_FALLBACK_WON,
  'auction'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_MODERATOR_NOMINATING_STARTED] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_MODERATOR_NOMINATING_STARTED,
  'election'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_MODERATOR_NOMINATED] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_MODERATOR_NOMINATED,
  'election'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_MODERATOR_NOMINEE_WITHDRAWN] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_MODERATOR_NOMINEE_WITHDRAWN,
  'election'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_MODERATOR_VOTING_STARTED] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_MODERATOR_VOTING_STARTED,
  'election'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_MODERATOR_VOTING_COMPLETED] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_MODERATOR_VOTING_COMPLETED,
  'election'
);
ledgerEntryTypeHelpers[LedgerEntryType.KYC_CERTIFICATION_APPROVED] = new LedgerEntryTypeHelper(
  LedgerEntryType.KYC_CERTIFICATION_APPROVED,
  'complete'
);
ledgerEntryTypeHelpers[LedgerEntryType.KYC_CERTIFICATION_REVOKED] = new LedgerEntryTypeHelper(
  LedgerEntryType.KYC_CERTIFICATION_REVOKED,
  'failure'
);
ledgerEntryTypeHelpers[LedgerEntryType.NICHE_OWNER_REMOVED] = new LedgerEntryTypeHelper(
  LedgerEntryType.NICHE_OWNER_REMOVED,
  'auction'
);
ledgerEntryTypeHelpers[LedgerEntryType.PAYMENT_CHARGEBACK] = new LedgerEntryTypeHelper(
  LedgerEntryType.PAYMENT_CHARGEBACK,
  'auction'
);
ledgerEntryTypeHelpers[LedgerEntryType.KYC_REFUND] = new LedgerEntryTypeHelper(
  LedgerEntryType.KYC_REFUND,
  'failure'
);
ledgerEntryTypeHelpers[LedgerEntryType.POST_REMOVED_FROM_CHANNEL] = new LedgerEntryTypeHelper(
  LedgerEntryType.POST_REMOVED_FROM_CHANNEL,
  'remove'
);
ledgerEntryTypeHelpers[LedgerEntryType.USER_PUBLISHED_POST] = new LedgerEntryTypeHelper(
  LedgerEntryType.USER_PUBLISHED_POST,
  'edit'
);
ledgerEntryTypeHelpers[LedgerEntryType.USER_DELETED_POST] = new LedgerEntryTypeHelper(
  LedgerEntryType.USER_DELETED_POST,
  'delete'
);
ledgerEntryTypeHelpers[LedgerEntryType.TRIBUNAL_USER_DELETED_POST_OR_COMMENT_AUP_VIOLATION] = new LedgerEntryTypeHelper(
  LedgerEntryType.TRIBUNAL_USER_DELETED_POST_OR_COMMENT_AUP_VIOLATION,
  'delete'
);
ledgerEntryTypeHelpers[LedgerEntryType.USER_HAD_POST_OR_COMMENT_DELETED_FOR_AUP_VIOLATION] = new LedgerEntryTypeHelper(
  LedgerEntryType.USER_HAD_POST_OR_COMMENT_DELETED_FOR_AUP_VIOLATION,
  'delete'
);
ledgerEntryTypeHelpers[LedgerEntryType.PUBLICATION_CREATED] = new LedgerEntryTypeHelper(
  LedgerEntryType.PUBLICATION_CREATED,
  'launch'
);
ledgerEntryTypeHelpers[LedgerEntryType.PUBLICATION_PAYMENT] = new LedgerEntryTypeHelper(
  LedgerEntryType.PUBLICATION_PAYMENT,
  'payment'
);
ledgerEntryTypeHelpers[LedgerEntryType.PUBLICATION_EDITOR_DELETED_COMMENT] = new LedgerEntryTypeHelper(
  LedgerEntryType.PUBLICATION_EDITOR_DELETED_COMMENT,
  'payment'
);
ledgerEntryTypeHelpers[LedgerEntryType.USER_HAD_COMMENT_DELETED_BY_PUBLICATION_EDITOR] = new LedgerEntryTypeHelper(
  LedgerEntryType.USER_HAD_COMMENT_DELETED_BY_PUBLICATION_EDITOR,
  'payment'
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedLedgerEntryType = new EnumEnhancer<LedgerEntryType, LedgerEntryTypeHelper>(
  ledgerEntryTypeHelpers
);
