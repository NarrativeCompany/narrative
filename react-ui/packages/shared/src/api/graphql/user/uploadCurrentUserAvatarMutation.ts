import gql from 'graphql-tag';
import { UserFragment } from '../fragments/userFragment';

/**
 * Utilize buildMultipartFileArrayBodySerializerFn to build the appropriate form for the multipart upload
 * for this mutation and pass as an input param when executing
 */
export const uploadCurrentUserAvatarMutation = gql`
  mutation UploadCurrentUserAvatarMutation (
    $input: FileInput
    $bodySerializer: Any
  ) {
    uploadCurrentUserAvatar (
      input: $input
    ) @rest(
      type: "User",
      path: "/users/current/avatar",
      method: "POST",
      bodySerializer: $bodySerializer
    ) {
      ...User
    }
  }
  ${UserFragment}
`;
