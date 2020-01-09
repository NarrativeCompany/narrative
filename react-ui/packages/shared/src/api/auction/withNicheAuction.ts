import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { nicheAuctionQuery } from '../graphql/auction/nicheAuctionQuery';
import { NicheAuctionQuery, NicheAuctionQueryVariables } from '../../types';

export interface AuctionOidProps {
  auctionOid: string;
}

export type WithNicheAuctionProps =
  NamedProps<{nicheAuctionData: GraphqlQueryControls & NicheAuctionQuery}, AuctionOidProps>;

export const withNicheAuction =
  graphql<
    AuctionOidProps,
    NicheAuctionQuery,
    NicheAuctionQueryVariables
  >(nicheAuctionQuery, {
    skip: ({auctionOid}) => !auctionOid,
    options: ({auctionOid}) => ({
      variables: {auctionOid}
    }),
    name: 'nicheAuctionData'
  });
