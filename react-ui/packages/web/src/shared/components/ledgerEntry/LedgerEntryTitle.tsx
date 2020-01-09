import * as React from 'react';
import { LedgerEntryProps } from './LedgerEntryListItem';
import { LedgerEntryMessages } from '../../i18n/LedgerEntryMessages';
import { processLedgerEntryMessage } from '../../utils/ledgerEntryTitleUtil';
import { LedgerEntryType } from '@narrative/shared';
import { EnhancedLedgerEntryType } from '../../enhancedEnums/ledgerEntryType';

export const LedgerEntryTitle: React.SFC<LedgerEntryProps> = (props) => {
  const { forProfilePage, ledgerEntry } = props;

  const entryType = EnhancedLedgerEntryType.get(ledgerEntry.type);

  // jw: The keys for the messages are setup so that we should be able to dynamically resolve the relevant message
  //     based on the state of the issue (its type, the related objects types / states). With that, we will need to
  //     build a key to lookup the appropriate message.

  // jw: let's start easy, are we rendering for the profile page.
  let messageKey = forProfilePage ? 'titleForProfile' : 'titleForChannel';

  // jw: still easy, just add the ledgerEntry.type to it now.
  messageKey += `.${ledgerEntry.type}`;

  // jw: let's get references to all the other types we are going to need
  const { referendum, invoice } = ledgerEntry;

  // jw: let's make sure we have a referendum before we do any of the checks for suffix's that require it.
  if (referendum) {
    // jw: should we add the referendum type for this ledger entry type?
    if (entryType.isAddReferendumTypeForLedgerEntryTitle()) {
      messageKey += `.${referendum.type}`;
    }

    // jw: do we need to add the affirmed/noAffirmed suffix?
    if (ledgerEntry.type === LedgerEntryType.ISSUE_REFERENDUM_RESULT) {
      messageKey += parseFloat(referendum.votePointsFor) > parseFloat(referendum.votePointsAgainst)
        ? '.affirmed'
        : '.notAffirmed';
    }
  }

  // jw: if we have an invoice, include its type so we can properly describe the invoice
  if (invoice) {
    messageKey += `.${invoice.type}`;
  }

  // jw: if we expect a post and we do not have one, then flag the message as withDeletedPost.
  if (entryType.isExpectPostForLedgerEntryTitle() && !ledgerEntry.post) {
    messageKey += `.withDeletedPost`;
  }

  // jw: if we have a commentOid, add a suffix so that the message can branch for it (Necessary for AUP messages)
  if (ledgerEntry.commentOid) {
    messageKey += `.withCommentOid`;
  }

  if (entryType.isCanHaveSecurityDepositValue() && ledgerEntry.securityDepositValue) {
    messageKey += '.withSecurityDeposit';
  }

  if (entryType.isAddPublicationPaymentTypeForLedgerEntryTitle() && ledgerEntry.publicationPaymentType) {
    messageKey += '.' + ledgerEntry.publicationPaymentType;
  }

  const message = LedgerEntryMessages[messageKey];
  if (!message) {
    // todo:error-handling: we should log an error with the server here so that we can track when this happens.
    return null;
  }

  return (
    <React.Fragment>
      {processLedgerEntryMessage(message, ledgerEntry)}
    </React.Fragment>
  );
};
