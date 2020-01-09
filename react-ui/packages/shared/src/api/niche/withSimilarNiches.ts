import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { similarNichesMutation } from '../graphql/niche/similarNichesMutation';
import {
  SimilarNichesInput,
  SimilarNichesMutation,
  SimilarNichesMutation_findSimilarNiches,
  SimilarNichesMutationVariables
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithSimilarNichesProps {
  findSimilarNiches: (input: SimilarNichesInput) => Promise<SimilarNichesMutation_findSimilarNiches[]>;
}

export const withSimilarNiches =
  graphql<
    {},
    SimilarNichesMutation,
    SimilarNichesMutationVariables,
    WithSimilarNichesProps
    >(similarNichesMutation, {
    props: ({mutate}) => ({
      findSimilarNiches: async (input: SimilarNichesInput) => {
        const variables: SimilarNichesMutationVariables = {input};
        const options = {
          variables,
        };

        if (!mutate) {
          throw new Error ('withSimilarNiches: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<SimilarNichesMutation>) => {
            const niches =
              response &&
              response.data &&
              response.data.findSimilarNiches;

            if (!niches) {
              throw new Error('withSimilarNiches: no return value from mutation');
            }

            return niches;
          })
          .catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
