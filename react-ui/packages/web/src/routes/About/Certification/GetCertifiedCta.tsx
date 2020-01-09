import * as React from 'react';
import { branch, compose, renderComponent } from 'recompose';
import { withExtractedAuthState, WithExtractedAuthStateProps } from '../../../shared/containers/withExtractedAuthState';
import { withCurrentUserKyc, WithCurrentUserKycProps } from '@narrative/shared';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';
import { Button } from '../../../shared/components/Button';
import { WebRoute } from '../../../shared/constants/routes';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { CertificationExplainerMessages } from '../../../shared/i18n/CertificationExplainerMessages';
import { FormattedMessage } from 'react-intl';

const GetCertifiedCtaComponent: React.SFC<WithCurrentUserKycProps> = (props) => {
  // jw: if the user does not have the ability to pay, short out.
  if (!props.userKyc.payPalCheckoutDetails) {
    return null;
  }

  return (
    <AboutSectionParagraph style={{marginBottom: '60px'}} asBlock={true}>
      <FlexContainer centerAll={true}>
        <Button type="primary" size="large" href={WebRoute.MemberCertification}>
          <FormattedMessage {...CertificationExplainerMessages.GetCertifiedNow}/>
        </Button>
      </FlexContainer>
    </AboutSectionParagraph>
  );
};

export const GetCertifiedCta = compose(
  withExtractedAuthState,
  // jw: if the user is not authenticated there is no reason to pull their KYC information.
  branch((props: WithExtractedAuthStateProps) => !props.userAuthenticated,
   renderComponent(() => null)
  ),
  withCurrentUserKyc,
  // jw: I am a huge advocate for loading placeholders, but since this will just not render if the user cannot purchase
  //     certification I think it makes more sense to show nothing until we have something to show.
  branch((props: WithCurrentUserKycProps) => props.loading,
   renderComponent(() => null)
  ),
)(GetCertifiedCtaComponent) as React.ComponentClass<{}>;
