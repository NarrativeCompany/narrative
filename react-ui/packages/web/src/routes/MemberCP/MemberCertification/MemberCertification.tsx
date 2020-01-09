import * as React from 'react';
import { compose } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { SEO } from '../../../shared/components/SEO';
import { SEOMessages } from '../../../shared/i18n/SEOMessages';
import { MemberCertificationMessages } from '../../../shared/i18n/MemberCertificationMessages';
import { MemberCertificationDetails } from './MemberCertificationDetails';
import { Link } from '../../../shared/components/Link';
import { MemberProfileHeaderText } from '../../MemberProfile/MemberProfile';

const MemberCertification: React.SFC<{}> = () => {
  const learnMore = (
    <Link.About type="certification" size="inherit" target="_self">
      <FormattedMessage {...MemberCertificationMessages.PageHeaderDescriptionLink}/>
    </Link.About>
  );

  return (
    <React.Fragment>
      <SEO title={SEOMessages.MemberCertificationTitle} />

      <MemberProfileHeaderText>
        <FormattedMessage {...MemberCertificationMessages.PageHeaderDescription} values={{learnMore}}/>
      </MemberProfileHeaderText>

      <MemberCertificationDetails/>
    </React.Fragment>
  );
};

export default compose(
)(MemberCertification) as React.ComponentClass<{}>;
