import * as React from 'react';
import {
  BidStatus,
  NicheAuction,
  NicheAuctionBid
} from '@narrative/shared';
import { Tag } from '../../../../shared/components/Tag';
import { FormattedMessage } from 'react-intl';
import { AuctionDetailsMessages } from '../../../../shared/i18n/AuctionDetailsMessages';

export interface AuctionBidTagProps {
  auction: NicheAuction;
  bid: NicheAuctionBid;
}

export const AuctionBidStatusTag: React.SFC<AuctionBidTagProps> = (props) => {
  const { auction, bid } = props;

  if (!bid) {
    return null;
  }

  let statusMessage;
  let tagColor: 'red' | undefined;
  switch (bid.status) {
    case BidStatus.OUTBID:
      return null;

    case BidStatus.FAILED_TO_PAY:
      statusMessage = AuctionDetailsMessages.FailedToPayBidStatus;
      tagColor = 'red';
      break;

    default:
      // todo:error-handling: We should log to the server if the type is not LEADING
      statusMessage = auction.openForBidding
        ? AuctionDetailsMessages.LeadingBidStatus
        : AuctionDetailsMessages.WonBidStatus;
  }

  return (
    <Tag size="normal" color={tagColor} style={{marginLeft: '10px'}}>
      <FormattedMessage {...statusMessage} />
    </Tag>
  );
};
