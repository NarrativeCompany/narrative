import {
  ResetPasswordMutation,
  ResetPasswordMutation_resetPassword,
  ResetPasswordMutationVariables
} from '../../types';
import { buildMutationImplFunction } from '../../utils';

import { resetPasswordMutation } from '../graphql/user/resetPasswordMutation';

export interface WithResetPasswordProps {
  resetPassword: (input: ResetPasswordMutationVariables) =>
    Promise<ResetPasswordMutation_resetPassword>;
}

export const withResetPassword =
  buildMutationImplFunction<ResetPasswordMutationVariables, ResetPasswordMutation>(
    resetPasswordMutation,
    'resetPassword'
  );
