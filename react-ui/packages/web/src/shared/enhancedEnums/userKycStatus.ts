import { UserKycStatus } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { FormattedMessage } from 'react-intl';
import { MemberCertificationMessages } from '../i18n/MemberCertificationMessages';
import { TextColor } from '../components/Text';

// jw: first, we need to define the helper object for UserKycStatus
class UserKycStatusHelper {
  type: UserKycStatus;
  statusMessage: FormattedMessage.MessageDescriptor;
  statusColor: TextColor;

  constructor(
    type: UserKycStatus,
    statusMessage: FormattedMessage.MessageDescriptor,
    statusColor: TextColor
  ) {
    this.type = type;
    this.statusMessage = statusMessage;
    this.statusColor = statusColor;
  }

  isNone() {
    return this.type === UserKycStatus.NONE;
  }

  isReadyForVerification() {
    return this.type === UserKycStatus.READY_FOR_VERIFICATION;
  }

  isAwaitingMetadata() {
    return this.type === UserKycStatus.AWAITING_METADATA;
  }

  isInReview() {
    return this.type === UserKycStatus.IN_REVIEW;
  }

  isApproved() {
    return this.type === UserKycStatus.APPROVED;
  }

  isRejected() {
    return this.type === UserKycStatus.REJECTED;
  }

  isRevoked() {
    return this.type === UserKycStatus.REVOKED;
  }

  isSupportsPayments() {
    return this.isNone() || this.isRejected();
  }
}

// jw: next: lets create the lookup of UserKycStatus to helper object
const userKycStatusHelpers: {[key: number]: UserKycStatusHelper} = [];
userKycStatusHelpers[UserKycStatus.NONE] = new UserKycStatusHelper(
  UserKycStatus.NONE,
  MemberCertificationMessages.KycStatusNone,
  'light'
);
userKycStatusHelpers[UserKycStatus.READY_FOR_VERIFICATION] = new UserKycStatusHelper(
  UserKycStatus.READY_FOR_VERIFICATION,
  MemberCertificationMessages.KycStatusReadyForVerification,
  'dark'
);
userKycStatusHelpers[UserKycStatus.AWAITING_METADATA] = new UserKycStatusHelper(
  UserKycStatus.AWAITING_METADATA,
  MemberCertificationMessages.KycStatusPending,
  'warning'
);
userKycStatusHelpers[UserKycStatus.IN_REVIEW] = new UserKycStatusHelper(
  UserKycStatus.IN_REVIEW,
  MemberCertificationMessages.KycStatusPending,
  'warning'
);
userKycStatusHelpers[UserKycStatus.APPROVED] = new UserKycStatusHelper(
  UserKycStatus.APPROVED,
  MemberCertificationMessages.KycStatusApproved,
  'success'
);
userKycStatusHelpers[UserKycStatus.REJECTED] = new UserKycStatusHelper(
  UserKycStatus.REJECTED,
  MemberCertificationMessages.KycStatusRejected,
  'error'
);
userKycStatusHelpers[UserKycStatus.REVOKED] = new UserKycStatusHelper(
  UserKycStatus.REVOKED,
  MemberCertificationMessages.KycStatusRevoked,
  'error'
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedUserKycStatus = new EnumEnhancer<UserKycStatus, UserKycStatusHelper>(
  userKycStatusHelpers
);
