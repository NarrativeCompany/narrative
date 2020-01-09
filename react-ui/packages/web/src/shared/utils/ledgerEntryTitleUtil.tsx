import * as React from 'react';
import { LedgerEntry, Referendum } from '@narrative/shared';
import { Link } from '../components/Link';
import { MemberLink } from '../components/user/MemberLink';
import { FormattedMessage } from 'react-intl';
import { NicheLink } from '../components/niche/NicheLink';
import { LedgerEntryMessages } from '../i18n/LedgerEntryMessages';
import styled from '../styled';
import { LocalizedNumber } from '../components/LocalizedNumber';
import { NRVE } from '../components/NRVE';
import { PostLink } from '../components/post/PostLink';
import { USD } from '../components/USD';
import { PublicationLink } from '../components/publication/PublicationLink';
import { EnhancedPublicationPlanType } from '../enhancedEnums/publicationPlanType';
import { ChannelLink } from '../components/channel/ChannelLink';

type ValueProviderFunc = (entry: LedgerEntry) => string | React.ReactNode | number;

// jw: this regex needs to match any variable that is defined, even ones with specified types: {variable[, }]
const VARIABLE_REGEXP = /{([\w]*)[, }]/g;

const MessageWrapper = styled.div`
  > span {
    display: -webkit-inline-box;
  }
`;

const ValueProviders = {
  actorLink: (entry: LedgerEntry): React.ReactNode => {
    if (entry.actor === null) {
      return null;
    }

    return <MemberLink user={entry.actor}/>;
  },
  authorLink: (entry: LedgerEntry): React.ReactNode => {
    if (!entry.author) {
      return null;
    }

    return <MemberLink user={entry.author}/>;
  },
  postLink: (entry: LedgerEntry): React.ReactNode => {
    if (!entry.postOid) {
      return null;
    }

    if (entry.post) {
      return <PostLink post={entry.post}/>;
    }
    return <FormattedMessage {...LedgerEntryMessages.DeletedPost}/>;
  },
  aupLink: (_entry: LedgerEntry): React.ReactNode => {
    return <Link.Legal type="aup"/>;
  },
  nicheLink: (entry: LedgerEntry): React.ReactNode => {
    return <NicheLink niche={entry.niche} color="default"/>;
  },
  publicationLink: (entry: LedgerEntry): React.ReactNode => {
    return <PublicationLink publication={entry.publication}/>;
  },
  channelLink: (entry: LedgerEntry): React.ReactNode => {
    return <ChannelLink channel={entry.niche || entry.publication || undefined} color="default"/>;
  },
  publicationPlanName: (entry: LedgerEntry): React.ReactNode => {
    if (!entry.publicationPlan) {
      // todo:error-handler: we should never have used this function unless the ledger entry has a plan stored on it
      return null;
    }

    const planType = EnhancedPublicationPlanType.get(entry.publicationPlan);
    return <FormattedMessage {...planType.name} />;
  },
  approvalOrRejection: (entry: LedgerEntry): React.ReactNode | null => {
    const { referendum } = entry;

    const message = wasReferendumApproved(referendum) ? LedgerEntryMessages.Approval : LedgerEntryMessages.Rejection;

    return (
      <FormattedMessage {...message}/>
    );
  },
  referendumVotePercentage: (entry: LedgerEntry): React.ReactNode => {
    const { referendum } = entry;

    if (!referendum) {
      return '0';
    }

    const wasApproved = wasReferendumApproved(referendum);

    const pointsFor = parseFloat(referendum.votePointsFor);
    const pointsAgainst = parseFloat(referendum.votePointsAgainst);

    // jw: we want to divide the highest vote count by the total votes and then multiply by 100 to get to a percentage.
    const dividend = wasApproved ? pointsFor : pointsAgainst;
    const divisor = pointsFor + pointsAgainst;

    return <LocalizedNumber value={(dividend / divisor) * 100} />;
  },
  bidCount: (entry: LedgerEntry): number => {
    const { auction } = entry;

    if (!auction) {
      return 0;
    }

    return !auction ? 0 : auction.totalBidCount;
  },
  bidNrveValue: (entry: LedgerEntry): React.ReactNode => {
    const { auctionBid } = entry;

    return <NRVE amount={auctionBid && auctionBid.bidAmount.nrve} />;
  },
  bidderLink: (entry: LedgerEntry): React.ReactNode | null => {
    const { auctionBid } = entry;
    if (!auctionBid) {
      return null;
    }

    return (
      <MemberLink user={auctionBid.bidder}/>
    );
  },
  approvedOrRejectedFromReferendum: (entry: LedgerEntry): React.ReactNode | null => {
    const { referendum } = entry;

    const message = wasReferendumApproved(referendum) ? LedgerEntryMessages.Approved : LedgerEntryMessages.Rejected;

    return (
      <FormattedMessage {...message}/>
    );
  },
  approveOrRejectFromVote: (entry: LedgerEntry): React.ReactNode | null => {
    if (entry.wasReferendumVotedFor === null) {
      return null;
    }

    const message = entry.wasReferendumVotedFor ? LedgerEntryMessages.Approve : LedgerEntryMessages.Reject;

    return (
      <FormattedMessage {...message}/>
    );
  },
  approveOrKeepRejectedWithNicheLink: (entry: LedgerEntry): React.ReactNode | null => {
    if (entry.wasReferendumVotedFor === null) {
      return null;
    }

    const nicheLink = <NicheLink niche={entry.niche} />;

    const message = entry.wasReferendumVotedFor
      ? LedgerEntryMessages.ApproveWithNicheLink
      : LedgerEntryMessages.KeepRejectedWithNicheLink;

    return (
      <FormattedMessage {...message} values={{nicheLink}}/>
    );
  },
  keepActiveOrRejectWithNicheLink: (entry: LedgerEntry): React.ReactNode | null => {
    if (entry.wasReferendumVotedFor === null) {
      return null;
    }

    const channelLink = <NicheLink niche={entry.niche} />;

    const message = entry.wasReferendumVotedFor
      ? LedgerEntryMessages.KeepActiveWithChannelLink
      : LedgerEntryMessages.RejectWithChannelLink;

    return (
      <FormattedMessage {...message} values={{channelLink}}/>
    );
  },
  keepActiveOrRejectWithPublicationLink: (entry: LedgerEntry): React.ReactNode | null => {
    if (entry.wasReferendumVotedFor === null) {
      return null;
    }

    const channelLink = <PublicationLink publication={entry.publication} />;

    const message = entry.wasReferendumVotedFor
      ? LedgerEntryMessages.KeepActiveWithChannelLink
      : LedgerEntryMessages.RejectWithChannelLink;

    return (
      <FormattedMessage {...message} values={{channelLink}}/>
    );
  },
  securityDepositValue: (entry: LedgerEntry): React.ReactNode | null => {
    if (entry.securityDepositValue === null) {
      return null;
    }

    return (
      <USD value={entry.securityDepositValue} />
    );
  }
};

function wasReferendumApproved(referendum: Referendum | null): boolean {
  if (!referendum) {
    return false;
  }

  return parseFloat(referendum.votePointsFor) > parseFloat(referendum.votePointsAgainst);
}

function processRegex(regex: RegExp, value: string): string[] {
  const results: string[] = [];
  const matcher = new RegExp(regex.source, regex.flags);

  let match;
  do {
      match = matcher.exec(value);
      if (match) {
        results.push(match[1]);
      }
  } while (match);

  return results;
}

export const processLedgerEntryMessage = (
  message: FormattedMessage.MessageDescriptor,
  entry: LedgerEntry
): React.ReactNode => {
  const { defaultMessage } = message;
  // jw: first, ensure we have a default message. This is nullable, so we have to do this.
  if (!defaultMessage) {
    return;
  }

  // jw: next, let's parse all of the variable names out of the defaultMessage
  const variableNames: string[] = processRegex(VARIABLE_REGEXP, defaultMessage);

  // jw: now that we have the names, let's turn those into values that we can replace into the message
  const messageValues = {};
  variableNames.forEach((variable: string) => {
    // jw: let's see if we have a provider for this variable's value.
    const provider: ValueProviderFunc = ValueProviders[variable];
    if (provider) {
      // jw: since we have a value provider, let's get the value from the ledger entry
      const value = provider(entry);

      // jw: if we got a value then let's include it for replacement. Since the variable could have any type we
      //     cannot set a default value if one is not parsed. Each provider will have to return the appropriate
      //     default since it should know best what value to provide
      if (value != null) {
        messageValues[variable] = value;
      }
    }
  });

  // jw: finally, we have parsed all of the variables and created values for them, lets resolve this message.
  return (
    <MessageWrapper>
      <FormattedMessage {...message} values={messageValues}/>
    </MessageWrapper>
  );
};
