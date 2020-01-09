import gql from 'graphql-tag';
import { UserOwnedChannelsFragment } from '../fragments/userOwnedChannelsFragment';

export const currentUserOwnedChannelsQuery = gql`
  query CurrentUserOwnedChannelsQuery {
    getCurrentUserOwnedChannels @rest(type: "UserOwnedChannels", path: "/users/current/owned-channels") {
      ...UserOwnedChannels
    }
  }
  ${UserOwnedChannelsFragment}
`;
