import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { referendumQuery } from '../graphql/referendum/referendumQuery';
import { ReferendumQuery, ReferendumQueryVariables } from '../../types';

export interface ReferendumOidProps {
  referendumOid: string;
}

export type WithReferendumProps =
  NamedProps<{referendumData: GraphqlQueryControls & ReferendumQuery}, ReferendumOidProps>;

export const withReferendum =
  graphql<
    ReferendumOidProps,
    ReferendumQuery,
    ReferendumQueryVariables
   >(referendumQuery, {
    skip: ({referendumOid}) => !referendumOid,
    options: ({referendumOid}) => ({
      variables: {referendumOid}
    }),
    name: 'referendumData'
  });
