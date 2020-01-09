import { branch, ComponentEnhancer, compose, renderComponent, withHandlers, withProps } from 'recompose';
import { LoadingProps } from '../utils/withLoadingPlaceholder';
import { withState, WithStateProps } from '@narrative/shared';
import {
  NicheAuctionDetail,
  WithNicheAuctionProps,
  withNicheAuction,
} from '@narrative/shared';
import * as React from 'react';
import { AuctionDetailProps } from '../../routes/HQ/Auctions/Details/AuctionDetails';

export interface WithUpdateFromBidProps {
  updateFromBid: boolean;
}

export interface WithUpdateAuctionDetailHandler {
  handleNewAuctionDetail: (auctionDetail: NicheAuctionDetail, updateFromBid?: boolean) => void;
}

type State = AuctionDetailProps &
  WithUpdateFromBidProps;

export type WithUpdateableAuctionDetailProps = WithUpdateAuctionDetailHandler &
  State;

function initializeState(auctionDetail: NicheAuctionDetail): State {
  return extractState(auctionDetail, false);
}

function extractState(auctionDetail: NicheAuctionDetail, updateFromBid: boolean): State {
  const auction = auctionDetail &&
    auctionDetail.auction;
  const currentUserLatestBidStatus = auctionDetail &&
    auctionDetail.currentUserLatestBidStatus || undefined;
  const currentUserLatestMaxNrveBid = auctionDetail &&
    auctionDetail.currentUserLatestMaxNrveBid || undefined;
  const currentUserActiveInvoiceOid = auctionDetail &&
    auctionDetail.currentUserActiveInvoiceOid || undefined;
  const currentUserBypassesSecurityDepositRequirement = auctionDetail &&
    auctionDetail.currentUserBypassesSecurityDepositRequirement || undefined;
  const securityDepositPayPalCheckoutDetails = auctionDetail &&
    auctionDetail.securityDepositPayPalCheckoutDetails || undefined;

  return {
    auction,
    currentUserLatestBidStatus,
    currentUserLatestMaxNrveBid,
    currentUserActiveInvoiceOid,
    updateFromBid,
    currentUserBypassesSecurityDepositRequirement,
    securityDepositPayPalCheckoutDetails
  };
}

// jw: first: let's isolate the fetching of the auction details into one stack
export function withUpdateableAuctionDetail
  // tslint:disable-next-line no-any
  (getLoadingComponent: () => React.ReactElement<any>): ComponentEnhancer<{}, {}>
{
  return compose(
    withNicheAuction,
    withProps((props: WithNicheAuctionProps) => {
      const { nicheAuctionData } = props;
      const { loading, getNicheAuction } = nicheAuctionData;

      const extractedDetails = extractState(getNicheAuction, false);

      return { loading, ...extractedDetails };
    }),
    // jw: to allow the state to work consistently, we need to eat the loading before we initialize the state.
    branch((props: LoadingProps) => (props.loading),
      renderComponent(() => getLoadingComponent())
    ),
    // jw: with that out of the way, we can now initialize the state from the auction details
    withState<State>(initializeState),
    // jw: now, to make everyones' lives more easy, let's go ahead and extract the state and spread it over the
    //     original properties so that the consumer can just use them like normal.
    withProps((props: WithStateProps<State>) => {
      const { state } = props;

      return {...state};
    }),
    // jw: and finally, we need to handle when a bid is made, and the AuctionDetail get's updated
    withHandlers({
      handleNewAuctionDetail: (props: WithStateProps<AuctionDetailProps>) =>
        (auctionDetail: NicheAuctionDetail, updateFromBid?: boolean) =>
      {
        const { setState } = props;
        const newState = extractState(auctionDetail, !!updateFromBid);

        setState(ss => ({...ss, ...newState}));
      }
    })
  );
}
