import {
  UpdateCurrentUserProfileMutation,
  UpdateCurrentUserProfileMutation_updateCurrentUserProfile,
  UpdateCurrentUserProfileMutationVariables
} from '../../types';
import { buildMutationImplFunction } from '../../utils';
import { updateCurrentUserProfileMutation } from '../graphql/user/updateCurrentUserProfileMutation';

export interface WithUpdateCurrentUserProfileProps {
  updateCurrentUserProfile: (input: UpdateCurrentUserProfileMutationVariables) =>
    Promise<UpdateCurrentUserProfileMutation_updateCurrentUserProfile>;
}

export const withUpdateCurrentUserProfile =
  buildMutationImplFunction<UpdateCurrentUserProfileMutationVariables, UpdateCurrentUserProfileMutation>(
    updateCurrentUserProfileMutation,
    'updateCurrentUserProfile'
  );
