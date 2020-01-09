import {
  SuspendEmailAddressForUserMutation,
  SuspendEmailAddressForUserMutation_suspendEmailAddressForUser,
  SuspendEmailAddressForUserMutationVariables
} from '../../types';
import { buildMutationImplFunction } from '../../utils';
import { suspendEmailAddressForUserMutation } from '../graphql/user/suspendEmailAddressForUserMutation';

export interface WithSuspendEmailAddressForUserProps {
  suspendEmailAddressForUser: (input: SuspendEmailAddressForUserMutationVariables) =>
    Promise<SuspendEmailAddressForUserMutation_suspendEmailAddressForUser>;
}

export const withSuspendEmailAddressForUser =
  buildMutationImplFunction<SuspendEmailAddressForUserMutationVariables, SuspendEmailAddressForUserMutation>(
    suspendEmailAddressForUserMutation,
    'suspendEmailAddressForUser'
  );
