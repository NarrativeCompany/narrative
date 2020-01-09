import * as React from 'react';
import { compose } from 'recompose';
import { SEO } from '../../../shared/components/SEO';
import { MemberNotificationSettingsMessages } from '../../../shared/i18n/MemberNotificationSettingsMessages';
import { MemberNotificationSettingsFormAjax } from './MemberNotificationSettingsFormAjax';
import { FormWrapperDiv } from '../settingsStyles';

const MemberNotificationSettings: React.SFC<{}> = () => {
  return (
    <React.Fragment>
      <SEO title={MemberNotificationSettingsMessages.SEOTitle} />

      <FormWrapperDiv>
        <MemberNotificationSettingsFormAjax />
      </FormWrapperDiv>
    </React.Fragment>
  );
};

export default compose(
)(MemberNotificationSettings);
