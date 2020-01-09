import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { currentUserOwnedChannelsQuery } from '../graphql/user/currentUserOwnedChannels';
import { CurrentUserOwnedChannelsQuery } from '../../types';

export type WithCurrentUserOwnedChannelsProps =
  NamedProps<{currentUserOwnedChannelsData: GraphqlQueryControls & CurrentUserOwnedChannelsQuery}, {}>;

export const withCurrentUserOwnedChannels =
  graphql<{}, CurrentUserOwnedChannelsQuery, {}>(currentUserOwnedChannelsQuery, {
    name: 'currentUserOwnedChannelsData'
  });
