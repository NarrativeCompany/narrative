import * as React from 'react';
import { List } from 'antd';
import { LedgerEntry, LedgerEntryType } from '@narrative/shared';
import { LedgerEntryIcon } from './LedgerEntryIcon';
import { LedgerEntryTitle } from './LedgerEntryTitle';
import { LedgerEntryDescription } from './LedgerEntryDescription';
import { LedgerEntryProps } from './LedgerEntryListItem';
import { NicheLink } from '../niche/NicheLink';
import { LedgerEntryMessages } from '../../i18n/LedgerEntryMessages';
import { FormattedMessage } from 'react-intl';
import { AuctionLink } from '../auction/AuctionLink';
import { TribunalIssueLink } from '../tribunal/TribunalIssueLink';
import { ApprovalLink } from '../approval/ApprovalLink';
import { Link } from '../Link';
import { generatePath } from 'react-router';
import { WebRoute } from '../../constants/routes';
import { PostLink } from '../post/PostLink';

const getLedgerEntryListItemActions = (props: LedgerEntryProps): React.ReactNode[] => {
  const { ledgerEntry, forProfilePage } = props;

  // jw: if we have an election, then that is the primary focus of this entry
  if (ledgerEntry.election) {
    const electionOid = ledgerEntry.election.oid;

    // jw: traditionally I would want to have a single ElectionLink here, but at this point we are not linking to
    //     elections in many places, and each of them is significantly different.
    return [(
      <Link to={generatePath(WebRoute.ModeratorElectionDetails, {electionOid})}>
        <FormattedMessage {...LedgerEntryMessages.ViewElection}/>
      </Link>
    )];
  }

  // jw: if we have a post, then that should be the primary focus of this entry.
  if (ledgerEntry.post) {
    return [(
      <PostLink post={ledgerEntry.post}>
        <FormattedMessage {...LedgerEntryMessages.ViewPost}/>
      </PostLink>
    )];
  }

  // jw: if we have a auction, then that is the primary focus of this entry
  if (ledgerEntry.auction) {
    return [(
      <AuctionLink auction={ledgerEntry.auction}>
        <FormattedMessage {...LedgerEntryMessages.ViewAuction}/>
      </AuctionLink>
    )];
  }

  // jw: if we have an issue, then that is the primary focus of this entry
  if (ledgerEntry.tribunalIssue) {
    return [(
      <TribunalIssueLink issue={ledgerEntry.tribunalIssue}>
        <FormattedMessage {...LedgerEntryMessages.ViewAppeal}/>
      </TribunalIssueLink>
    )];
  }

  // jw: if we have a referendum, then that is the primary focus of this entry
  if (ledgerEntry.referendum) {
    return [(
      <ApprovalLink referendum={ledgerEntry.referendum}>
        <FormattedMessage {...LedgerEntryMessages.ViewApproval}/>
      </ApprovalLink>
    )];
  }

  // jw: If this is a niche, let's link to it.
  if (ledgerEntry.niche && forProfilePage) {
    return [(
      <NicheLink niche={ledgerEntry.niche} color="default">
        <FormattedMessage {...LedgerEntryMessages.ViewNiche}/>
      </NicheLink>
    )];
  }

  // If this is a KYC approval, link to the user's profile.
  if ( (ledgerEntry.type === LedgerEntryType.KYC_CERTIFICATION_APPROVED ||
        ledgerEntry.type === LedgerEntryType.KYC_REFUND ||
        ledgerEntry.type === LedgerEntryType.KYC_CERTIFICATION_REVOKED ) &&
       ledgerEntry.actor && ledgerEntry.actor.username) {
    return [(
      <Link to={generatePath(WebRoute.UserProfileReputation, { username: ledgerEntry.actor.username })}>
        <FormattedMessage {...LedgerEntryMessages.ViewReputation}/>
      </Link>
    )];
  }

  return [];
};

export interface LedgerEntryProps {
  ledgerEntry: LedgerEntry;
  forProfilePage?: boolean;
}

export const LedgerEntryListItem: React.SFC<LedgerEntryProps> = (props) => {
  return (
    <List.Item
      actions={getLedgerEntryListItemActions(props)}
      className="ledger-entry-list-item"
      style={{lineHeight: 15}}
    >
      <List.Item.Meta
        avatar={<LedgerEntryIcon {...props} />}
        title={<LedgerEntryTitle {...props}/>}
        description={<LedgerEntryDescription {...props}/>}
      />
    </List.Item>
  );
};
