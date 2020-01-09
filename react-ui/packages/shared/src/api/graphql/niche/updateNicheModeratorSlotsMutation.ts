import gql from 'graphql-tag';
import { ElectionFragment } from '../fragments/electionFragment';
import { NicheFragment } from '../fragments/nicheFragment';

export const updateNicheModeratorSlotsMutation = gql`
  mutation UpdateNicheModeratorSlotsMutation ($input: UpdateNicheModeratorSlotsInput!, $nicheOid: String!) {
    updateNicheModeratorSlots (input: $input, nicheOid: $nicheOid) @rest(
      type: "NicheModeratorSlots",
      path: "/niches/{args.nicheOid}/moderator-slots"
      method: "PUT"
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
