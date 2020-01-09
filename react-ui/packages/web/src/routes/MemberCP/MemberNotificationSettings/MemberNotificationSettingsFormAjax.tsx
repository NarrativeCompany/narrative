import * as React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { compose, withProps } from 'recompose';
import {
  UserNotificationSettings,
  withUserNotificationSettings,
  WithUserNotificationSettingsProps
} from '@narrative/shared';
import MemberNotificationSettingsForm from './MemberNotificationSettingsForm';
import { withLoadingPlaceholder, WithLoadingPlaceholderProps } from '../../../shared/utils/withLoadingPlaceholder';

interface WithProps {
  preferences: UserNotificationSettings;
}

type Props =
  RouteComponentProps<{}> &
  WithUserNotificationSettingsProps &
  WithLoadingPlaceholderProps &
  WithProps;

const MemberNotificationSettingsFormAjaxComponent: React.SFC<Props> = (props) => {
  const { preferences } = props;

  return (
    <MemberNotificationSettingsForm preferences={preferences} />
  );
};

export const MemberNotificationSettingsFormAjax = compose(
  withUserNotificationSettings,
  withProps((props: Props) => {
    const { userNotificationSettingsData } = props;

    const loading =
      userNotificationSettingsData &&
      userNotificationSettingsData.loading;
    const preferences =
      userNotificationSettingsData &&
      userNotificationSettingsData.getUserNotificationSettings || [];

    return { preferences, loading };
  }),
  withLoadingPlaceholder()
)(MemberNotificationSettingsFormAjaxComponent) as React.ComponentClass<{}>;
