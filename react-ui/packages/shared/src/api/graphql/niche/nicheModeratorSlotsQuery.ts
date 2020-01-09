import gql from 'graphql-tag';
import { ElectionFragment } from '../fragments/electionFragment';
import { NicheFragment } from '../fragments/nicheFragment';

export const nicheModeratorSlotsQuery = gql`
  query NicheModeratorSlotsQuery ($nicheId: String!) {
    getNicheModeratorSlots (nicheId: $nicheId)
    @rest(
      type: "NicheModeratorSlots",
      path: "/niches/{args.nicheId}/moderator-slots"
    ) 
    {
      niche @type(name: "Niche") {
        ...Niche
      }
      activeModeratorElection @type(name: "Election") {
        ...Election
      }
      moderatorSlots
    }
  }
  ${NicheFragment}
  ${ElectionFragment}
`;
