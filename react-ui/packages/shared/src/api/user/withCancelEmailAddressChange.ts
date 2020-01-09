import { graphql } from 'react-apollo';
import { cancelEmailAddressChangeMutation } from '../graphql/user/cancelEmailAddressChangeMutation';
import {
  CancelEmailAddressChangeMutation,
  CancelEmailAddressChangeMutation_cancelEmailAddressChange,
  CancelEmailAddressChangeMutationVariables,
  VerifyPendingEmailAddressInput,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'cancelEmailAddressChange';

export interface WithCancelEmailAddressChangeProps {
  [functionName]: (input: VerifyPendingEmailAddressInput, userOid: string) =>
    Promise<CancelEmailAddressChangeMutation_cancelEmailAddressChange>;
}

export const withCancelEmailAddressChange =
  graphql<
    {},
    CancelEmailAddressChangeMutation,
    CancelEmailAddressChangeMutationVariables,
    WithCancelEmailAddressChangeProps
  >(cancelEmailAddressChangeMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: VerifyPendingEmailAddressInput, userOid: string) => {
        return await mutationResolver<CancelEmailAddressChangeMutation>(mutate, {
          variables: { input, userOid }
        }, functionName);
      }
    })
  });
