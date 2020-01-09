import {
  DeleteCurrentUserMutation,
  DeleteCurrentUserMutation_deleteCurrentUser,
  DeleteCurrentUserMutationVariables
} from '../../types';
import { buildMutationImplFunction } from '../../utils';
import { deleteCurrentUserMutation } from '../graphql/user/deleteCurrentUserMutation';

export interface WithDeleteCurrentUserProps {
  deleteCurrentUser: (input: DeleteCurrentUserMutationVariables) =>
    Promise<DeleteCurrentUserMutation_deleteCurrentUser>;
}

export const withDeleteCurrentUser =
  buildMutationImplFunction<DeleteCurrentUserMutationVariables, DeleteCurrentUserMutation>(
    deleteCurrentUserMutation,
    'deleteCurrentUser'
  );
