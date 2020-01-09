import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { referendumVotesQuery } from '../graphql/referendum/referendumVotesQuery';
import { ReferendumVotesQuery, ReferendumVotesQueryVariables } from '../../types';

export interface ParentProps {
  referendumOid: string;
  uniqueQueryStrValue?: number;
}

export type WithReferendumVotesProps =
  NamedProps<{referendumVotesData: GraphqlQueryControls & ReferendumVotesQuery}, ParentProps>;

export const withReferendumVotes =
  graphql<
    ParentProps,
    ReferendumVotesQuery,
    ReferendumVotesQueryVariables
   >(referendumVotesQuery, {
    skip: ({referendumOid}) => !referendumOid,
    options: ({referendumOid, uniqueQueryStrValue}) => ({
      variables: { referendumOid, uniqueQueryStrValue }
    }),
    name: 'referendumVotesData'
  });
