import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { WithUserKycProps } from './MemberCertificationDetails';
import { EnhancedUserKycStatus } from '../../../shared/enhancedEnums/userKycStatus';
import { MemberCertificationMessages } from '../../../shared/i18n/MemberCertificationMessages';
import { Heading } from '../../../shared/components/Heading';
import { Paragraph } from '../../../shared/components/Paragraph';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { EnhancedUserKycEventType } from '../../../shared/enhancedEnums/userKycEventType';

export const MemberCertificationDescription: React.SFC<WithUserKycProps> = (props) => {
  const description = getDescriptionMessages(props);

  if (!description) {
    return null;
  }

  return (
    <FlexContainer column={true} style={{ marginBottom: 50 }}>
      {description}
    </FlexContainer>
  );
};

function getDescriptionMessages (props: WithUserKycProps) {
  const { userKyc } = props;

  const status = EnhancedUserKycStatus.get(userKyc.kycStatus);
  let message;
  let info;
  let rejectReason;

  if (status.isNone() || status.isReadyForVerification()) {
    return null;
  } else if (status.isApproved()) {
    // show approved message
    message = MemberCertificationMessages.CertificationApprovedDescription;
  } else if (status.isRevoked()) {
    // show banned from submitting payments message
    message = MemberCertificationMessages.CertificationRevokedDescription;
    info = MemberCertificationMessages.CertificationRevokedInfo;
  } else if (status.isSupportsPayments() && !userKyc.payPalCheckoutDetails) {
    // show certifications disabled
    message = MemberCertificationMessages.CertificationsDisabledDescription;
    info = MemberCertificationMessages.CertificationsDisabledReason;
  } else if (status.isAwaitingMetadata() || status.isInReview()) {
    // show pending certification message
    message = MemberCertificationMessages.CertificationPendingDescription;
    info = MemberCertificationMessages.CertificationPendingInfo;
  } else if (status.isRejected()) {
    const { rejectedReasonEventType } = userKyc;
    const rejectionReasonType =
      rejectedReasonEventType &&
      EnhancedUserKycEventType.get(rejectedReasonEventType);

    message = MemberCertificationMessages.CertificationRejectedDescription;
    info = MemberCertificationMessages.CertificationRejectedInfo;
    rejectReason = rejectionReasonType && <FormattedMessage {...rejectionReasonType.dueToMessage}/>;
  }

  if (!message && !info && !rejectReason) {
    return null;
  }

  return (
    <React.Fragment>
      <Heading size={5} uppercase={true}>
        {message &&
        <FormattedMessage {...message} values={{ reason: rejectReason }}/>}
      </Heading>

      {info &&
      <Paragraph>
        {rejectReason &&
        <FormattedMessage
          {...MemberCertificationMessages.CertificationRejectedReason}
          values={{ reason: rejectReason }}
        />}

        <FormattedMessage {...info}/>
      </Paragraph>}
    </React.Fragment>
  );
}
