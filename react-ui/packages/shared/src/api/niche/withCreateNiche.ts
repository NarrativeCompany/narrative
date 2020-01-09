import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { createNicheMutation } from '../graphql/niche/createNicheMutation';
import {
  CreateNicheInput,
  CreateNicheMutation,
  CreateNicheMutation_createNiche,
  CreateNicheMutationVariables
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithCreateNicheProps {
  createNiche: (input: CreateNicheInput) => Promise<CreateNicheMutation_createNiche>;
}

export const withCreateNiche =
  graphql<
    {},
    CreateNicheMutation,
    CreateNicheMutationVariables,
    WithCreateNicheProps
  >(createNicheMutation, {
    props: ({mutate}) => ({
      createNiche: async (input: CreateNicheInput) => {
        const variables: CreateNicheMutationVariables = {input};
        const options = {
          variables,
        };

        if (!mutate) {
          throw new Error ('withLoginUser: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<CreateNicheMutation>) => {
            const referendum =
              response &&
              response.data &&
              response.data.createNiche;

            if (!referendum) {
              throw new Error('withCreateNiche: no return value from mutation');
            }

            return referendum;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
});
