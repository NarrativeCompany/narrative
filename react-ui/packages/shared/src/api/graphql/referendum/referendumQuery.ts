import gql from 'graphql-tag';
import { ReferendumFragment } from '../fragments/referendumFragment';

export const referendumQuery = gql`
  query ReferendumQuery ($referendumOid: String!) {
    getReferendum (referendumOid: $referendumOid) 
    @rest (
      type: "Referendum", 
      path: "/referendums/{args.referendumOid}"
    ) {
      ...Referendum
    }
  }
  ${ReferendumFragment}
`;
