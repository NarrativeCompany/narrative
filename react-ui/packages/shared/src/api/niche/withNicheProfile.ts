import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { NicheProfileQuery, NicheProfileQueryVariables } from '../../types';
import { nicheProfileQuery } from '../graphql/niche/nicheProfileQuery';

interface ParentProps {
  nicheId: string;
}

export type WithNicheProfileProps =
  NamedProps<{nicheProfileData: GraphqlQueryControls & NicheProfileQuery}, ParentProps>;

export const withNicheProfile =
  graphql<
    ParentProps,
    NicheProfileQuery,
    NicheProfileQueryVariables
    >(nicheProfileQuery, {
    options: ({nicheId}) => ({
      variables: {
        nicheId
      }
    }),
    name: 'nicheProfileData'
  });
