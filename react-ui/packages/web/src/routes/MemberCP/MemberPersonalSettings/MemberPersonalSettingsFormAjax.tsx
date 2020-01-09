import * as React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { compose, withProps } from 'recompose';
import {
  UserPersonalSettings,
  withUserPersonalSettings,
  WithUserPersonalSettingsProps
} from '@narrative/shared';
import MemberPersonalSettingsForm from './MemberPersonalSettingsForm';
import { withLoadingPlaceholder, WithLoadingPlaceholderProps } from '../../../shared/utils/withLoadingPlaceholder';

interface WithProps {
  preferences: UserPersonalSettings;
}

type Props =
  RouteComponentProps<{}> &
  WithUserPersonalSettingsProps &
  WithLoadingPlaceholderProps &
  WithProps;

const MemberPersonalSettingsFormAjaxComponent: React.SFC<Props> = (props) => {
  const { preferences } = props;

  return (
    <MemberPersonalSettingsForm preferences={preferences} />
  );
};

export const MemberPersonalSettingsFormAjax = compose(
  withUserPersonalSettings,
  withProps((props: Props) => {
    const { userPersonalSettingsData } = props;

    const loading =
      userPersonalSettingsData &&
      userPersonalSettingsData.loading;
    const preferences =
      userPersonalSettingsData &&
      userPersonalSettingsData.getUserPersonalSettings || [];

    return { preferences, loading };
  }),
  withLoadingPlaceholder()
)(MemberPersonalSettingsFormAjaxComponent) as React.ComponentClass<{}>;
