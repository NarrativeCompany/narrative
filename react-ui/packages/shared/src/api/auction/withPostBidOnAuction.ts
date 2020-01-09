import { NicheAuctionBidInput, PostBidOnAuctionMutation, PostBidOnAuctionMutation_postBidOnAuction } from '../../types';
import { buildMutationImplFunction } from '../../utils';
import { postBidOnAuctionMutation } from '../graphql/auction/postBidOnAuctionMutation';

// jw: traditionally I would pass these two as separate properties into handler function, but since I am using the
//     buildMutationImplFunction builder function, I need to consolidate it down into a single object.
export interface WithPostBidOnAuctionVariables {
  input: NicheAuctionBidInput;
  auctionOid: string;
}

export interface WithPostBidOnAuctionProps {
  postBidOnAuction: (variables: WithPostBidOnAuctionVariables) =>
    Promise<PostBidOnAuctionMutation_postBidOnAuction>;
}

export const withPostBidOnAuction =
  buildMutationImplFunction<WithPostBidOnAuctionVariables, PostBidOnAuctionMutation>(
    postBidOnAuctionMutation,
    'postBidOnAuction'
  );
