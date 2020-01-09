import gql from 'graphql-tag';
import { NicheFragment } from '../fragments/nicheFragment';
import { TribunalIssueFragment } from '../fragments/tribunalIssueFragment';

export const nicheProfileQuery = gql`
  query NicheProfileQuery ($nicheId: String!) {
    getNicheProfile (nicheId: $nicheId)
    @rest(
      type: "NicheProfile",
      path: "/niches/{args.nicheId}/profile"
    ) {
      niche @type(name: "Niche") {
        ...Niche
      }
      editDetailsTribunalIssue @type(name: "TribunalIssue") {
        ...TribunalIssue
      }
    }
  }
  ${NicheFragment}
  ${TribunalIssueFragment}
`;
