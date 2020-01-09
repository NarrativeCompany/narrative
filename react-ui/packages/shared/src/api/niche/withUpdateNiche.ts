import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { updateNicheMutation } from '../graphql/niche/updateNicheMutation';
import {
  UpdateNicheInput,
  UpdateNicheMutation,
  UpdateNicheMutation_updateNiche,
  UpdateNicheMutationVariables
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithUpdateNicheProps {
  updateNiche: (input: UpdateNicheInput, nicheOid: string) => Promise<UpdateNicheMutation_updateNiche>;
}

export const withUpdateNiche =
  graphql<
    {},
    UpdateNicheMutation,
    UpdateNicheMutationVariables,
    WithUpdateNicheProps
  >(updateNicheMutation, {
    props: ({mutate}) => ({
      updateNiche: async (input: UpdateNicheInput, nicheOid: string) => {
        const variables: UpdateNicheMutationVariables = {
          input,
          nicheOid
        };
        const options = {
          variables,
        };

        if (!mutate) {
          throw new Error ('withUpdateNiche: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<UpdateNicheMutation>) => {
            const tribunalIssueDetail =
              response &&
              response.data &&
              response.data.updateNiche;

            if (!tribunalIssueDetail) {
              throw new Error('withUpdateNiche: no return value from mutation');
            }

            return tribunalIssueDetail;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
