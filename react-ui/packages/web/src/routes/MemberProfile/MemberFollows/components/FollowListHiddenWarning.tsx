import * as React from 'react';
import { Link } from '../../../../shared/components/Link';
import { FormattedMessage } from 'react-intl';
import { WebRoute } from '../../../../shared/constants/routes';
import { MemberFollowsMessages } from '../../../../shared/i18n/MemberFollowsMessages';
import { Alert } from 'antd';

export const FollowListHiddenWarning: React.SFC<{}> = () => {
  const changeSetting = (
    <Link to={WebRoute.MemberPersonalSettings}>
      <FormattedMessage {...MemberFollowsMessages.ChangeSetting}/>
    </Link>
  );

  return (
    <Alert
      type="info"
      message={<FormattedMessage {...MemberFollowsMessages.RestrictingLists} values={{changeSetting}}/>}
      style={{marginBottom: 20}}
    />
  );
};
