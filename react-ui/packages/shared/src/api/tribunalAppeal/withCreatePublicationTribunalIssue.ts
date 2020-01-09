import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import {
  createPublicationTribunalIssueMutation
} from '../graphql/tribunalAppeal/createPublicationTribunalIssueMutation';
import {
  CreatePublicationTribunalIssueInput,
  CreatePublicationTribunalIssueMutation,
  CreatePublicationTribunalIssueMutation_createPublicationTribunalIssue,
  CreatePublicationTribunalIssueMutationVariables
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithCreatePublicationTribunalIssueProps {
  createPublicationTribunalIssue: (input: CreatePublicationTribunalIssueInput, publicationOid: string) =>
    Promise<CreatePublicationTribunalIssueMutation_createPublicationTribunalIssue>;
}

export const withCreatePublicationTribunalIssue =
  graphql<
    {},
    CreatePublicationTribunalIssueMutation,
    CreatePublicationTribunalIssueMutationVariables,
    WithCreatePublicationTribunalIssueProps
  >(createPublicationTribunalIssueMutation, {
    props: ({mutate}) => ({
      createPublicationTribunalIssue: async (input: CreatePublicationTribunalIssueInput, publicationOid: string) => {
        const variables: CreatePublicationTribunalIssueMutationVariables = {
          input,
          publicationOid
        };
        const options = {
          variables,
        };

        if (!mutate) {
          throw new Error ('withCreatePublicationTribunalIssue: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<CreatePublicationTribunalIssueMutation>) => {
            const tribunalIssueDetail =
              response &&
              response.data &&
              response.data.createPublicationTribunalIssue;

            if (!tribunalIssueDetail) {
              throw new Error('withCreatePublicationTribunalIssue: no return value from mutation');
            }

            return tribunalIssueDetail;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
