import * as React from 'react';
import { DetailsActionCard } from '../detailAction/DetailsActionCard';
import { FormattedMessage } from 'react-intl';
import { CountDown } from '../CountDown';
import { generatePath } from 'react-router';
import { WebRoute } from '../../constants/routes';
import { AuctionDetailsMessages } from '../../i18n/AuctionDetailsMessages';
import { AuctionDetailProps } from '../../../routes/HQ/Auctions/Details/AuctionDetails';
import { AuctionBiddingForm } from './AuctionBiddingForm';
import { AuctionCurrentUserStatusCard } from '../../../routes/HQ/Auctions/Details/AuctionCurrentUserStatusCard';
import { WithUpdateAuctionDetailHandler, WithUpdateFromBidProps } from '../../containers/withUpdateableAuctionDetail';

interface Props extends AuctionDetailProps, WithUpdateFromBidProps, WithUpdateAuctionDetailHandler {
  footerText?: React.ReactNode;
}

export const AuctionActionCard: React.SFC<Props> = (props) => {
  const { auction, footerText, ...formProps } = props;

  // jw: if the auction is not accepting new bids, do not include the action card.
  if (!auction.openForBidding) {
    return null;
  }

  const { endDatetime } = auction;
  const auctionOid = auction.oid;

  return (
    <React.Fragment>

      {/* jw: place the users current status first */}
      <AuctionCurrentUserStatusCard {...props} />

      <DetailsActionCard
        icon={footerText ? 'bid' : undefined}
        title={<FormattedMessage {...AuctionDetailsMessages.Bid} />}
        sideColor="gold"
        countDown={endDatetime
          ? <CountDown endTime={endDatetime}/>
          : null
        }
        toDetails={footerText ? generatePath(WebRoute.AuctionDetails, {auctionOid}) : undefined}
        footerText={footerText}
      >

        <AuctionBiddingForm
          auction={auction}
          {...formProps}
        />

      </DetailsActionCard>
    </React.Fragment>
  );
};
