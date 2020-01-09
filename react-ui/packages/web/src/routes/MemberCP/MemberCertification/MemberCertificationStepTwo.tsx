import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { MemberCertificationStep } from './MemberCertificationStep';
import { Button } from '../../../shared/components/Button';
import { WebRoute } from '../../../shared/constants/routes';
import { MemberCertificationMessages } from '../../../shared/i18n/MemberCertificationMessages';
import { UserKyc } from '@narrative/shared';
import { EnhancedUserKycStatus } from '../../../shared/enhancedEnums/userKycStatus';
import { Link } from '../../../shared/components/Link';
import { externalUrls } from '../../../shared/constants/externalUrls';
import { Paragraph } from '../../../shared/components/Paragraph';
import styled from '../../../shared/styled';

interface ParentProps {
  userKyc: UserKyc;
}

const CertificationDescriptionList = styled.ul`
  margin-top: 10px;
`;

export const MemberCertificationStepTwo: React.SFC<ParentProps> = (props) => {
  const content = getCertificationStepTwoContent(props);

  const title = <FormattedMessage {...MemberCertificationMessages.CertificationStepTwoTitle}/>;

  const mustHaveDateOfBirth = (
    <strong>
      <FormattedMessage {...MemberCertificationMessages.CertificationStepTwoMustHaveDateOfBirth}/>
    </strong>
  );

  const yearsOfAgeUS = (
    <strong>
      <FormattedMessage {...MemberCertificationMessages.CertificationStepYearsOfAgeUS}/>
    </strong>
  );

  const yearsOfAgeNonUS = (
    <strong>
      <FormattedMessage {...MemberCertificationMessages.CertificationStepYearsOfAgeNonUS}/>
    </strong>
  );

  const faqLink = (
    <Link.Anchor href={externalUrls.certificationFaq} target="_blank">
      <FormattedMessage {...MemberCertificationMessages.CertificationFAQLink}/>
    </Link.Anchor>
  );

  return (
    <MemberCertificationStep
      title={title}
      description={
        <React.Fragment>
          <Paragraph>
            <FormattedMessage {...MemberCertificationMessages.CertificationStepTwoDescription}/>
          </Paragraph>
          <CertificationDescriptionList>
            <li>
              <FormattedMessage
                {...MemberCertificationMessages.CertificationStepTwoDateOfBirthRequired}
                values={{mustHaveDateOfBirth}}
              />
            </li>
            <li>
              <FormattedMessage
                {...MemberCertificationMessages.CertificationStepTwoDescriptionExtraUS}
                values={{yearsOfAge: yearsOfAgeUS}}
              />
            </li>
            <li>
              <FormattedMessage
                {...MemberCertificationMessages.CertificationStepTwoDescriptionExtraNonUS}
                values={{yearsOfAge: yearsOfAgeNonUS}}
              />
            </li>
          </CertificationDescriptionList>
          <Paragraph>
            <FormattedMessage {...MemberCertificationMessages.CertificationFAQDetails} values={{ faqLink }}/>
          </Paragraph>
        </React.Fragment>
      }
      {...props}
    >
      {content}
    </MemberCertificationStep>
  );
};

function getCertificationStepTwoContent (props: ParentProps) {
  const { userKyc } = props;

  const status = EnhancedUserKycStatus.get(userKyc.kycStatus);

  if (!status.isReadyForVerification()) {
    return;
  }

  return (
    <div>
      <Button type="primary" size="large" href={WebRoute.MemberCertificationForm}>
        <FormattedMessage {...MemberCertificationMessages.ProceedToDocumentSubmission}/>
      </Button>
    </div>
  );
}
