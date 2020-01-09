import { UserWalletTransactionStatus } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { FormattedMessage } from 'react-intl';
import { MemberRewardsTransactionMessages } from '../i18n/MemberRewardsTransactionMessages';

export class UserWalletTransactionStatusHelper {
  status: UserWalletTransactionStatus;
  message: FormattedMessage.MessageDescriptor;
  opacity: number;

  constructor(status: UserWalletTransactionStatus, message: FormattedMessage.MessageDescriptor, opacity: number) {
    this.status = status;
    this.message = message;
    this.opacity = opacity;
  }

  isPending(): boolean {
    return this.status === UserWalletTransactionStatus.PENDING;
  }

  isProcessing(): boolean {
    return this.status === UserWalletTransactionStatus.PROCESSING;
  }

  isCompleted(): boolean {
    return this.status === UserWalletTransactionStatus.COMPLETED;
  }
}

const statusHelpers: {[key: number]: UserWalletTransactionStatusHelper} = [];
statusHelpers[UserWalletTransactionStatus.PENDING] = new UserWalletTransactionStatusHelper(
  UserWalletTransactionStatus.PENDING,
  MemberRewardsTransactionMessages.TransactionStatusPending,
  0.5
);
statusHelpers[UserWalletTransactionStatus.PROCESSING] = new UserWalletTransactionStatusHelper(
  UserWalletTransactionStatus.PROCESSING,
  MemberRewardsTransactionMessages.TransactionStatusProcessing,
  0.75
);
statusHelpers[UserWalletTransactionStatus.COMPLETED] = new UserWalletTransactionStatusHelper(
  UserWalletTransactionStatus.COMPLETED,
  MemberRewardsTransactionMessages.TransactionStatusCompleted,
  1
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedUserWalletTransactionStatus =
  new EnumEnhancer<UserWalletTransactionStatus, UserWalletTransactionStatusHelper>(
  statusHelpers
);
