import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import { submitKycApplicantMutation } from '../graphql/user/submitKycApplicantMutation';
import {
  KycApplicationInput,
  SubmitKycApplicantMutation,
  SubmitKycApplicantMutation_submitKycApplicant,
  SubmitKycApplicantMutationVariables
} from '../../types';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithSubmitKycApplicantProps {
  // tslint:disable-next-line no-any
  submitKycApplicant: (input: KycApplicationInput, bodySerializer: any) =>
    Promise<SubmitKycApplicantMutation_submitKycApplicant>;
}

export const withSubmitKycApplicant =
  graphql<
    {},
    SubmitKycApplicantMutation,
    SubmitKycApplicantMutationVariables,
    WithSubmitKycApplicantProps
  >(submitKycApplicantMutation, {
    props: ({mutate}) => ({
      // tslint:disable-next-line no-any
      submitKycApplicant: async (input: KycApplicationInput, bodySerializer: any) => {
        const variables: SubmitKycApplicantMutationVariables = { input, bodySerializer };
        const options = { variables };

        if (!mutate) {
          throw new Error ('withSubmitKycApplicant: missing mutate');
        }

        return mutate(options)
          .then((response: FetchResult<SubmitKycApplicantMutation>) => {

            const userKyc =
              response &&
              response.data &&
              response.data.submitKycApplicant;

            if (!userKyc) {
              throw new Error('withSubmitKycApplicant: no return value from mutation');
            }

            return userKyc;
          }).catch((error) => {
            throw resolveExceptionFromApolloError(error);
          });
      }
    })
  });
