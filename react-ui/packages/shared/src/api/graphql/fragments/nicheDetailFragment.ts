import gql from 'graphql-tag';
import { NicheFragment } from './nicheFragment';

export const NicheDetailFragment = gql`
  fragment NicheDetail on NicheDetail {
    niche @type(name: "Niche") {
      ...Niche
    }
    activeAuctionOid
    activeModeratorElectionOid
    currentBallotBoxReferendumOid
    currentTribunalAppealOids
    availableTribunalIssueTypes
    currentUserActiveInvoiceOid
  }
  ${NicheFragment}
`;
