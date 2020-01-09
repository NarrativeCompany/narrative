import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { similarNichesByNicheIdQuery } from '../graphql/niche/similarNichesByNicheIdQuery';
import { SimilarNichesByNicheIdQuery, SimilarNichesByNicheIdQueryVariables } from '../../types';

interface ParentProps {
  nicheId: string;
}

export type WithSimilarNichesByNicheIdProps =
  NamedProps<{similarNichesByNicheIdData: GraphqlQueryControls & SimilarNichesByNicheIdQuery}, ParentProps>;

export const withSimilarNichesByNicheId =
  graphql<
    ParentProps,
    SimilarNichesByNicheIdQuery,
    SimilarNichesByNicheIdQueryVariables
  >(similarNichesByNicheIdQuery, {
    skip: ({nicheId}) => !nicheId,
    options: ({nicheId}) => ({
      variables: {
        input: {nicheId}
      }
    }),
    name: 'similarNichesByNicheIdData'
  });
