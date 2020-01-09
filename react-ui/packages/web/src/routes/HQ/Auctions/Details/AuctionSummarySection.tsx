import * as React from 'react';
import { AuctionDetailsMessages } from '../../../../shared/i18n/AuctionDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { SummaryGrid } from '../../../../shared/components/SummaryGrid';
import { SummaryGridRow } from '../../../../shared/components/SummaryGridRow';
import { LocalizedTime } from '../../../../shared/components/LocalizedTime';
import { CountDown } from '../../../../shared/components/CountDown';
import { NRVE } from '../../../../shared/components/NRVE';
import { AuctionDetailProps } from './AuctionDetails';
import { BidStatus } from '@narrative/shared';

export const AuctionSummarySection: React.SFC<AuctionDetailProps> = (props) => {
  const { auction, currentUserLatestBidStatus, currentUserLatestMaxNrveBid, auction: { leadingBid } } = props;

  const isCurrentUserLeadingBidder = currentUserLatestBidStatus === BidStatus.LEADING;

  const leadingBidMessage = auction.openForBidding
    ? AuctionDetailsMessages.CurrentLeadingBid
    : AuctionDetailsMessages.WinningBid;

  return (
    <SummaryGrid title={<FormattedMessage {...AuctionDetailsMessages.Summary} />}>
      <SummaryGridRow title={<FormattedMessage {...AuctionDetailsMessages.Status} />}>
        {auction.openForBidding
          ? <FormattedMessage {...AuctionDetailsMessages.SaleOngoing} />
          : <FormattedMessage {...AuctionDetailsMessages.SaleEnded} />
        }
      </SummaryGridRow>

      <SummaryGridRow title={<FormattedMessage {...AuctionDetailsMessages.BidPeriodStart} />}>
        <LocalizedTime time={auction.startDatetime} />
      </SummaryGridRow>

      {auction.endDatetime &&
        <SummaryGridRow title={<FormattedMessage {...AuctionDetailsMessages.BidPeriodEnd} />}>
          {auction.openForBidding
            ? <CountDown endTime={auction.endDatetime} />
            : <LocalizedTime time={auction.endDatetime} />
          }
        </SummaryGridRow>
      }

      {currentUserLatestMaxNrveBid && isCurrentUserLeadingBidder &&
        <SummaryGridRow title={<FormattedMessage {...AuctionDetailsMessages.YourMaxBid} />}>
          <NRVE amount={currentUserLatestMaxNrveBid.nrve} />
        </SummaryGridRow>
      }

      {currentUserLatestMaxNrveBid && !isCurrentUserLeadingBidder &&
        <SummaryGridRow title={<FormattedMessage {...AuctionDetailsMessages.YourHighestBid} />}>
          <NRVE amount={currentUserLatestMaxNrveBid.nrve} />
        </SummaryGridRow>
      }

      {leadingBid &&
        <SummaryGridRow title={<FormattedMessage {...leadingBidMessage} />}>
          <NRVE amount={leadingBid.bidAmount.nrve} />
        </SummaryGridRow>
      }
    </SummaryGrid>
  );
};
