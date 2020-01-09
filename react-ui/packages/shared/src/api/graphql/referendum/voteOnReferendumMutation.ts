import gql from 'graphql-tag';
import { ReferendumFragment } from '../fragments/referendumFragment';

export const voteOnReferendumMutation = gql`
  mutation VoteOnReferendumMutation ($input: VoteOnReferendumInput!, $referendumId: String!) {
    voteOnReferendum (input: $input, referendumId: $referendumId) 
    @rest(
      type: "Referendum", 
      path: "/referendums/{args.referendumId}/votes", 
      method: "POST"
    ) {
        ...Referendum
    }
  }
  ${ReferendumFragment}
`;
