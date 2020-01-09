import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { userNotificationSettingsQuery } from '../graphql/user/userNotificationSettingsQuery';
import { UserNotificationSettingsQuery } from '../../types';

export type WithUserNotificationSettingsProps =
  NamedProps<{userNotificationSettingsData: GraphqlQueryControls & UserNotificationSettingsQuery}, {}>;

export const withUserNotificationSettings =
  graphql<{}, UserNotificationSettingsQuery>(userNotificationSettingsQuery, {
    options: () => ({}),
    name: 'userNotificationSettingsData'
  });
