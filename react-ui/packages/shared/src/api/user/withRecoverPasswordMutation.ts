import {
  RecoverPasswordMutation,
  RecoverPasswordMutation_recoverPassword,
  RecoverPasswordMutationVariables
} from '../../types';
import { buildMutationImplFunction } from '../../utils';

import { recoverPasswordMutation } from '../graphql/user/recoverPasswordMutation';

export interface WithRecoverPasswordProps {
  recoverPassword: (input: RecoverPasswordMutationVariables) =>
    Promise<RecoverPasswordMutation_recoverPassword>;
}

export const withRecoverPassword =
  buildMutationImplFunction<RecoverPasswordMutationVariables, RecoverPasswordMutation>(
    recoverPasswordMutation,
    'recoverPassword'
  );
