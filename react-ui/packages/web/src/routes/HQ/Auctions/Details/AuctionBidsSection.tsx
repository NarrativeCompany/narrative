import * as React from 'react';
import { AuctionDetailsMessages } from '../../../../shared/i18n/AuctionDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { Card } from '../../../../shared/components/Card';
import { AuctionBid } from './AuctionBid';
import styled from 'styled-components';
import { compose, withProps } from 'recompose';
import { AuctionProps } from './AuctionDetails';
import { LoadingProps } from '../../../../shared/utils/withLoadingPlaceholder';
import { NicheAuctionBid, withNicheAuctionBids, WithNicheAuctionBidsProps } from '@narrative/shared';
import { SectionHeader } from '../../../../shared/components/SectionHeader';

const BidsContainer = styled.div`
  .auction-bid-container:nth-child(even) {
    background-color: #F9FAFB;
  }
`;

type Props = AuctionProps &
  LoadingProps &
  {
    bids: NicheAuctionBid[];
  };

const AuctionBidsSectionComponent: React.SFC<Props> = (props) => {
  const { loading, auction, bids } = props;

  // jw: if there is no leading bid, then let's short out and not include this section at all.
  if (!auction.leadingBid) {
    return null;
  }

  if (loading) {
    return (
      <React.Fragment>
        <SectionHeader title={<FormattedMessage {...AuctionDetailsMessages.BidHistory} />}/>
        <Card loading={true}/>
      </React.Fragment>
    );
  }

  const bidCount = bids.length;

  return (
    <React.Fragment>
      <SectionHeader title={<FormattedMessage {...AuctionDetailsMessages.BidHistory} />}/>

      <BidsContainer>
        {bids.map((bid, index) =>
          <AuctionBid
            key={bid.oid}
            bidNumber={bidCount - index}
            auction={auction}
            bid={bid}
          />
        )}
      </BidsContainer>
    </React.Fragment>
  );
};

export const AuctionBidsSection = compose(
  withProps((props: AuctionProps) => {
    const { auction } = props;

    // jw: if there is no leading bid, then let's not add the auctionOid parameter so that we do not make a request
    //     against the server. There is no reason to get bids that we know do not exist.
    if (!auction.leadingBid) {
      return null;
    }

    // jw: Guess we have at least one bid, let's make a request against the server so that we can be sure to get them
    //     all. Including the leading bid oid to ensure that caching works in our favor.
    return { auctionOid: auction.oid, leadingBidOid: auction.leadingBid.oid };
  }),
  withNicheAuctionBids,
  withProps((props: WithNicheAuctionBidsProps) => {
    const { nicheAuctionBidsData } = props;

    // jw: since the query could be skipped, we need to extract these explicitly.
    const loading = nicheAuctionBidsData &&
      nicheAuctionBidsData.loading;
    const bids = nicheAuctionBidsData &&
      nicheAuctionBidsData.getNicheAuctionBids || [];

    return { loading, bids };
  })
)(AuctionBidsSectionComponent) as React.ComponentClass<AuctionProps>;
