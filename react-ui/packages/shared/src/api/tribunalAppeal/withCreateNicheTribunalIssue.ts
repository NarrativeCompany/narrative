import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { createNicheTribunalIssueMutation } from '../graphql/tribunalAppeal/createNicheTribunalIssueMutation';
import {
  CreateNicheTribunalIssueInput,
  CreateNicheTribunalIssueMutation,
  CreateNicheTribunalIssueMutation_createNicheTribunalIssue,
  CreateNicheTribunalIssueMutationVariables
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithCreateNicheTribunalIssueProps {
  createNicheTribunalIssue: (input: CreateNicheTribunalIssueInput, nicheOid: string) =>
    Promise<CreateNicheTribunalIssueMutation_createNicheTribunalIssue>;
}

export const withCreateNicheTribunalIssue =
  graphql<
    {},
    CreateNicheTribunalIssueMutation,
    CreateNicheTribunalIssueMutationVariables,
    WithCreateNicheTribunalIssueProps
  >(createNicheTribunalIssueMutation, {
    props: ({mutate}) => ({
      createNicheTribunalIssue: async (input: CreateNicheTribunalIssueInput, nicheOid: string) => {
        const variables: CreateNicheTribunalIssueMutationVariables = {
          input,
          nicheOid
        };
        const options = {
          variables,
        };

        if (!mutate) {
          throw new Error ('withCreateNicheTribunalIssue: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<CreateNicheTribunalIssueMutation>) => {
            const tribunalIssueDetail =
              response &&
              response.data &&
              response.data.createNicheTribunalIssue;

            if (!tribunalIssueDetail) {
              throw new Error('withCreateNicheTribunalIssue: no return value from mutation');
            }

            return tribunalIssueDetail;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
