import { graphql } from 'react-apollo';
import { FetchResult } from 'apollo-link';
import {
  UpdateEmailAddressMutation,
  UpdateEmailAddressMutation_updateEmailAddress,
  UpdateEmailAddressMutationVariables
} from '../../types';
import { updateEmailAddressMutation } from '../graphql/user/updateEmailAddressMutation';
import { resolveExceptionFromApolloError } from '../../utils';

export interface WithUpdateEmailAddressProps {
  updateEmailAddress: (input: UpdateEmailAddressMutationVariables) =>
    Promise<UpdateEmailAddressMutation_updateEmailAddress>;
}

export const withUpdateEmailAddress = graphql(updateEmailAddressMutation, {
  props: ({mutate}) => ({
    updateEmailAddress: async (input: UpdateEmailAddressMutationVariables) => {
      const variables: UpdateEmailAddressMutationVariables = input;
      const options = {
        variables
      };

      if (!mutate) {
        throw new Error ('withUpdateEmailAddress: missing mutate');
      }

      return mutate(options)
        .then((response: FetchResult<UpdateEmailAddressMutation>) => {
          const res =
            response &&
            response.data &&
            response.data.updateEmailAddress;

          if (!res) {
            throw new Error('withUpdateEmailAddress: no return value from mutation');
          }

          return res;
        }).catch((error) => {
          throw resolveExceptionFromApolloError(error);
        });
    }
  })
});
