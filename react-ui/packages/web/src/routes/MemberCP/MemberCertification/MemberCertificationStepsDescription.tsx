import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import { MemberCertificationMessages } from '../../../shared/i18n/MemberCertificationMessages';
import { UserKyc } from '@narrative/shared';
import { Link } from '../../../shared/components/Link';
import styled from '../../../shared/styled';

const DescriptionWrapper = styled.span`
  display: flex;
  flex-direction: column;
  
  span:first-child {
    margin-bottom: 5px;
  }
  
  ol {
    padding-inline-start: 15px;
  }
  
  li {
    margin-bottom: 5px;
  }
`;

interface ParentProps {
  userKyc: UserKyc;
}

export const MemberCertificationStepsDescription: React.SFC<ParentProps> = () => {
  const privacyPolicyLink = <Link.Legal type="privacy"/>;

  const description = (
    <DescriptionWrapper>
      <ol>
        <li>
          <FormattedMessage {...MemberCertificationMessages.CertificationStepsSectionHeaderStepOne}/>
        </li>
        <li>
          <FormattedMessage
            {...MemberCertificationMessages.CertificationStepsSectionHeaderStepTwo}
            values={{ privacyPolicyLink }}
          />
        </li>
      </ol>
    </DescriptionWrapper>
  );

  return (
    <SectionHeader
      title={<FormattedMessage {...MemberCertificationMessages.CertificationStepsSectionHeaderTitle}/>}
      description={description}
      style={{ marginBottom: 50 }}
    />
  );
};
