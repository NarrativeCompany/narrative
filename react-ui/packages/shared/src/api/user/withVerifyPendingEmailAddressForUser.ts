import { graphql } from 'react-apollo';
import { verifyPendingEmailAddressForUserMutation } from '../graphql/user/verifyPendingEmailAddressForUserMutation';
import {
  VerifyEmailAddressResult,
  VerifyPendingEmailAddressForUserMutation,
  VerifyPendingEmailAddressForUserMutationVariables,
  VerifyPendingEmailAddressInput,
} from '../../types';
import { mutationResolver } from '../../utils';

const functionName = 'verifyPendingEmailAddressForUser';

export interface WithVerifyPendingEmailAddressForUserProps {
  [functionName]: (input: VerifyPendingEmailAddressInput, userOid: string) => Promise<VerifyEmailAddressResult>;
}

export const withVerifyPendingEmailAddressForUser =
  graphql<
    {},
    VerifyPendingEmailAddressForUserMutation,
    VerifyPendingEmailAddressForUserMutationVariables,
    WithVerifyPendingEmailAddressForUserProps
  >(verifyPendingEmailAddressForUserMutation, {
    props: ({mutate}) => ({
      [functionName]: async (input: VerifyPendingEmailAddressInput, userOid: string) => {
        return await mutationResolver<VerifyPendingEmailAddressForUserMutation>(mutate, {
          variables: { input, userOid }
        }, functionName);
      }
    })
  });
