import * as React from 'react';
import { SEO } from '../../../shared/components/SEO';
import { MemberPersonalSettingsMessages } from '../../../shared/i18n/MemberPersonalSettingsMessages';
import { MemberPersonalSettingsFormAjax } from './MemberPersonalSettingsFormAjax';
import { FormWrapperDiv } from '../settingsStyles';
import { compose } from 'recompose';

const MemberPersonalSettings: React.SFC<{}> = () => {
  return (
    <React.Fragment>
      <SEO title={MemberPersonalSettingsMessages.SEOTitle} />

      <FormWrapperDiv>
        <MemberPersonalSettingsFormAjax />
      </FormWrapperDiv>
    </React.Fragment>
  );
};

export default compose(
)(MemberPersonalSettings) as React.ComponentClass<{}>;
