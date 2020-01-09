import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { nicheAuctionBidsQuery } from '../graphql/auction/nicheAuctionBidsQuery';
import { NicheAuctionBidsQuery, NicheAuctionBidsQueryVariables } from '../../types';
import { AuctionOidProps } from './withNicheAuction';

interface Props extends AuctionOidProps {
  leadingBidOid?: string;
}

export type WithNicheAuctionBidsProps =
  NamedProps<{nicheAuctionBidsData: GraphqlQueryControls & NicheAuctionBidsQuery}, Props>;

export const withNicheAuctionBids =
  graphql<
    Props,
    NicheAuctionBidsQuery,
    NicheAuctionBidsQueryVariables
  >(nicheAuctionBidsQuery, {
    skip: ({auctionOid}) => !auctionOid,
    options: ({auctionOid, leadingBidOid}) => ({
      variables: {auctionOid, leadingBidOid}
    }),
    name: 'nicheAuctionBidsData'
  });
