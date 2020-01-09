import {
  VerifyEmailAddressForUserMutation,
  VerifyEmailAddressForUserMutation_verifyEmailAddressForUser,
  VerifyEmailAddressForUserMutationVariables
} from '../../types';
import { buildMutationImplFunction } from '../../utils';
import { verifyEmailAddressForUserMutation } from '../graphql/user/verifyEmailAddressForUserMutation';

export interface WithVerifyEmailAddressForUserProps {
  verifyEmailAddressForUser: (input: VerifyEmailAddressForUserMutationVariables) =>
    Promise<VerifyEmailAddressForUserMutation_verifyEmailAddressForUser>;
}

export const withVerifyEmailAddressForUser =
  buildMutationImplFunction<VerifyEmailAddressForUserMutationVariables, VerifyEmailAddressForUserMutation>(
    verifyEmailAddressForUserMutation,
    'verifyEmailAddressForUser'
  );
