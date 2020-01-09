import { UserKycEventType } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { FormattedMessage } from 'react-intl';
import { MemberCertificationMessages } from '../i18n/MemberCertificationMessages';

// jw: first, we need to define the helper object for UserKycEventType
class UserKycEventTypeHelper {
  type: UserKycEventType;
  dueToMessage: FormattedMessage.MessageDescriptor;

  constructor(
    type: UserKycEventType,
    dueToMessage: FormattedMessage.MessageDescriptor
  ) {
    this.type = type;
    this.dueToMessage = dueToMessage;
  }
}

// jw: next: lets create the lookup of UserKycEventType to helper object
const UserKycEventTypeHelpers: {[key: number]: UserKycEventTypeHelper} = [];
UserKycEventTypeHelpers[UserKycEventType.USER_INFO_MISSING_FROM_DOCUMENT] = new UserKycEventTypeHelper(
  UserKycEventType.USER_INFO_MISSING_FROM_DOCUMENT,
  MemberCertificationMessages.DueToUserInfoMissingFromDocument
);
UserKycEventTypeHelpers[UserKycEventType.SELFIE_NOT_VALID] = new UserKycEventTypeHelper(
  UserKycEventType.SELFIE_NOT_VALID,
  MemberCertificationMessages.DueToSelfieNotValid
);
UserKycEventTypeHelpers[UserKycEventType.REJECTED_DUPLICATE] = new UserKycEventTypeHelper(
  UserKycEventType.REJECTED_DUPLICATE,
  MemberCertificationMessages.DueToRejectedDuplicate
);
UserKycEventTypeHelpers[UserKycEventType.DOCUMENT_INVALID] = new UserKycEventTypeHelper(
  UserKycEventType.DOCUMENT_INVALID,
  MemberCertificationMessages.DueToDocumentInvalid
);
UserKycEventTypeHelpers[UserKycEventType.DOCUMENT_SUSPICIOUS] = new UserKycEventTypeHelper(
  UserKycEventType.DOCUMENT_SUSPICIOUS,
  MemberCertificationMessages.DueToDocumentSuspicious
);
UserKycEventTypeHelpers[UserKycEventType.SELFIE_PAPER_MISSING] = new UserKycEventTypeHelper(
  UserKycEventType.SELFIE_PAPER_MISSING,
  MemberCertificationMessages.DueToSelfiePaperMissing
);
UserKycEventTypeHelpers[UserKycEventType.SELFIE_LOW_QUALITY] = new UserKycEventTypeHelper(
  UserKycEventType.SELFIE_LOW_QUALITY,
  MemberCertificationMessages.DueToSelfieLowQuality
);
UserKycEventTypeHelpers[UserKycEventType.SELFIE_MISMATCH] = new UserKycEventTypeHelper(
  UserKycEventType.SELFIE_MISMATCH,
  MemberCertificationMessages.DueToSelfieMismatch
);
UserKycEventTypeHelpers[UserKycEventType.USER_UNDERAGE] = new UserKycEventTypeHelper(
  UserKycEventType.USER_UNDERAGE,
  MemberCertificationMessages.DueToUserUnderage
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedUserKycEventType = new EnumEnhancer<UserKycEventType, UserKycEventTypeHelper>(
  UserKycEventTypeHelpers
);
