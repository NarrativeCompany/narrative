import {
  UploadCurrentUserAvatarMutation,
  UploadCurrentUserAvatarMutation_uploadCurrentUserAvatar,
  UploadCurrentUserAvatarMutationVariables
} from '../../types';
import { buildMutationImplFunction } from '../../utils';
import { uploadCurrentUserAvatarMutation } from '../graphql/user/uploadCurrentUserAvatarMutation';

export interface WithUploadCurrentUserAvatarProps {
  uploadCurrentUserAvatar: (input: UploadCurrentUserAvatarMutationVariables)
    => Promise<UploadCurrentUserAvatarMutation_uploadCurrentUserAvatar>;
}

export const withUploadCurrentUserAvatar =
  buildMutationImplFunction<UploadCurrentUserAvatarMutationVariables, UploadCurrentUserAvatarMutation>(
    uploadCurrentUserAvatarMutation,
    'uploadCurrentUserAvatar'
  );
