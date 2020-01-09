import * as React from 'react';
import { compose } from 'recompose';
import { UserKycStatus, withCurrentUserKyc, WithCurrentUserKycProps } from '@narrative/shared';
import { SectionHeader } from '../SectionHeader';
import { FormattedMessage } from 'react-intl';
import { SidebarMessages } from '../../i18n/SidebarMessages';
import { Link } from '../Link';
import { WebRoute } from '../../constants/routes';

const GetCertifiedSidebarCardComponent: React.SFC<WithCurrentUserKycProps> = (props) => {
  const { userKyc, loading } = props;

  // similar to the GetCertifiedCta, let's not show a loading spinner here since nothing will end up
  // rendering if the user is already Certified.
  if (loading) {
    return null;
  }

  let certifiedCard = null;
  if (userKyc.kycStatus !== UserKycStatus.APPROVED) {

    const certifyingLink = (
      <Link to={WebRoute.CertificationExplainer}>
        <FormattedMessage {...SidebarMessages.GetCertifiedCertifying} />
      </Link>
    );

    certifiedCard = (
      <React.Fragment>
        <SectionHeader
          title={<FormattedMessage {...SidebarMessages.GetCertifiedTitle} />}
          description={
            <FormattedMessage
              {...SidebarMessages.GetCertifiedDescription}
              values={{certifying: certifyingLink}}
            />
          }
        />
      </React.Fragment>
    );
  }

  return (certifiedCard);
};

export const GetCertifiedSidebarCard = compose(
  withCurrentUserKyc,
)(GetCertifiedSidebarCardComponent) as React.ComponentClass<{}>;
