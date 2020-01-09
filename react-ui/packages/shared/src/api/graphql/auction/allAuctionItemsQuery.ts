import gql from 'graphql-tag';
import { NicheAuctionFragment } from '../fragments/nicheAuctionFragment';
import { PageInfoFragment } from '../fragments/pageInfoFragment';

export const allAuctionItemsQuery = gql`
  query AllAuctionItemsQuery ($input: AllAuctionsInput) {
    getAllNicheAuctions (input: $input) @rest(type: "AllAuctionsPayload", path: "/auctions?{args.input}") {
      items @type(name: "NicheAuction") {
        ...NicheAuction
      }
      info @type(name: "PageInfo") {
        ...PageInfo
      }
    }
  }
  ${NicheAuctionFragment}
  ${PageInfoFragment}
`;
