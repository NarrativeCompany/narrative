import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { userPersonalSettingsQuery } from '../graphql/user/userPersonalSettingsQuery';
import { UserPersonalSettingsQuery } from '../../types';

export type WithUserPersonalSettingsProps =
  NamedProps<{userPersonalSettingsData: GraphqlQueryControls & UserPersonalSettingsQuery}, {}>;

export const withUserPersonalSettings =
  graphql<{}, UserPersonalSettingsQuery>(userPersonalSettingsQuery, {
    options: () => ({}),
    name: 'userPersonalSettingsData'
  });
