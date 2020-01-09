import * as React from 'react';
import { compose, withProps } from 'recompose';
import { CardProps } from 'antd/lib/card';
import { NicheCard } from '../components/NicheCard';
import { ChannelCardTitleAndDesc } from '../components/ChannelCardTitleAndDesc';
import { AuctionCardInfoRow } from './AuctionCardInfoRow';
import { NicheCardUser } from '../components/NicheCardUser';
import { AuctionCardProgressBar } from './AuctionCardProgressBar';
import { AuctionCardButton } from './AuctionCardButton';
import { ModalConnect, ModalName, ModalStoreProps } from '../../../shared/stores/ModalStore';
import { NicheAuction, Niche, User } from '@narrative/shared';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';

interface WithProps {
  niche: Niche;
  winningBidder: User;
}

interface ParentProps {
  auction: NicheAuction;
  pendingPayment?: boolean;
}

type Props =
  WithExtractedCurrentUserProps &
  ModalStoreProps &
  WithProps &
  ParentProps &
  CardProps;

export const AuctionCardComponent: React.SFC<Props> = (props) => {
  const { niche, auction, pendingPayment, winningBidder, currentUser } = props;

  const CardCover = (
    <AuctionCardProgressBar
      auction={auction}
      pendingPayment={pendingPayment}
      currentUserOid={currentUser && currentUser.oid}
    />
  );

  // jw: due to the follow length and potential length of niche names/description, we need to use a 335 height. Might
  //     grow if more details are added.
  return (
    <NicheCard height={335} cover={CardCover}>
      {pendingPayment &&
      <NicheCardUser user={winningBidder} forAuctionPendingPayment={true} />}

      <ChannelCardTitleAndDesc
        channel={niche}
        center={true}
        forListCard={true}
      />

      <AuctionCardInfoRow
        endTime={auction.endDatetime}
        currentBid={auction.leadingBid && auction.leadingBid.bidAmount.nrve || auction.startingBid.nrve}
        pendingPayment={pendingPayment}
        totalBidCount={auction.totalBidCount}
      />

      <AuctionCardButton auctionOid={auction.oid} pendingPayment={pendingPayment}/>
    </NicheCard>
  );
};

export const AuctionCard = compose(
  withExtractedCurrentUser,
  ModalConnect(ModalName.login),
  withProps((props: Props) => {
    const { auction } = props;

    const niche =
      auction &&
      auction.niche;
    const winningBidder =
      auction &&
      auction.leadingBid &&
      auction.leadingBid.bidder;

    return { niche, winningBidder };
  })
)(AuctionCardComponent) as React.ComponentClass<ParentProps>;
