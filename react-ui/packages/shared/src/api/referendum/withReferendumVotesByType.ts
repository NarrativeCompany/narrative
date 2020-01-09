import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { referendumVotesByTypeQuery } from '../graphql/referendum/referendumVotesByTypeQuery';
import { ReferendumVotesByTypeQuery, ReferendumVotesByTypeQueryVariables } from '../../types';
import { ReferendumOidProps } from './withReferendum';

interface Props extends ReferendumOidProps {
  votedFor: boolean;
  lastVoterDisplayName: string;
  lastVoterUsername: string;
}

export type WithReferendumVotesByTypeProps =
  NamedProps<{referendumVotesData: GraphqlQueryControls & ReferendumVotesByTypeQuery}, Props>;

export const withReferendumVotesByType =
  graphql<
    Props,
    ReferendumVotesByTypeQuery,
    ReferendumVotesByTypeQueryVariables
   >(referendumVotesByTypeQuery, {
    skip: ({referendumOid}) => !referendumOid,
    options: ({referendumOid, votedFor, ...inputProps}) => ({
      variables: { 
        referendumOid,
        votedFor,
        input: {...inputProps}
      }
    }),
    name: 'referendumVotesData'
  });
