import * as React from 'react';
import { UserRewardTransaction, UserWalletTransactionType } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { FormattedMessage } from 'react-intl';
import { MemberRewardsTransactionMessages } from '../i18n/MemberRewardsTransactionMessages';
import { NicheLink } from '../components/niche/NicheLink';
import { PostLink } from '../components/post/PostLink';
import { MemberLink } from '../components/user/MemberLink';
import { NeoAddressLink } from '../components/neo/NeoAddressLink';
import { NeoTransactionLink } from '../components/neo/NeoTransactionLink';
import { EnhancedContentCreatorRewardRole } from './contentCreatorRewardRole';

export class UserWalletTransactionTypeHelper {
  type: UserWalletTransactionType;
  message: FormattedMessage.MessageDescriptor;

  constructor(type: UserWalletTransactionType, message: FormattedMessage.MessageDescriptor) {
    this.type = type;
    this.message = message;
  }

  isContentReward(): boolean {
    return this.type === UserWalletTransactionType.CONTENT_REWARD;
  }

  isActivityReward(): boolean {
    return this.type === UserWalletTransactionType.ACTIVITY_REWARD;
  }

  isUserTip(): boolean {
    return this.type === UserWalletTransactionType.USER_TIP;
  }

  isUserRedemption(): boolean {
    return this.type === UserWalletTransactionType.USER_REDEMPTION;
  }

  getMessage(transaction: UserRewardTransaction): React.ReactNode {
    let message: FormattedMessage.MessageDescriptor;
    // bl: special handling for sending tips
    if (this.isUserTip() && transaction.amount.nrve.startsWith('-')) {
      message = MemberRewardsTransactionMessages.UserTipSent;
    } else {
      message = this.message;
    }
    if (transaction.memo) {
      const { memo } = transaction;
      return <FormattedMessage {...message} values={{memo}}/>;
    }
    if (transaction.metadataNiche) {
      const nicheLink = <NicheLink niche={transaction.metadataNiche} color="default"/>;
      return <FormattedMessage {...message} values={{nicheLink}}/>;
    }
    if (transaction.metadataPost) {
      const role = transaction.metadataContentCreatorRewardRole;
      const enhancedRole = role ? EnhancedContentCreatorRewardRole.get(role) : undefined;
      const contentRewardRole = enhancedRole && enhancedRole.label
        ? <React.Fragment> / <FormattedMessage {...enhancedRole.label}/></React.Fragment>
        : '';
      const postLink = <PostLink post={transaction.metadataPost}/>;
      return <FormattedMessage {...message} values={{contentRewardRole, postLink}}/>;
    } else if (this.isContentReward()) {
      const postLink = <FormattedMessage {...MemberRewardsTransactionMessages.DeletedPost}/>;
      return <FormattedMessage {...message} values={{postLink}}/>;
    }
    if (transaction.metadataUser) {
      const userLink = <MemberLink user={transaction.metadataUser}/>;
      return <FormattedMessage {...message} values={{userLink}}/>;
    }
    if (this.isActivityReward() && transaction.metadataActivityBonusPercentage) {
      const activityBonusPercentage = transaction.metadataActivityBonusPercentage;
      return (
        <FormattedMessage
          {...MemberRewardsTransactionMessages.ActivityRewardWithBonus}
          values={{activityBonusPercentage}}
        />
      );
    }
    if (this.isUserRedemption() && transaction.metadataNeoWalletAddress) {
      const metadataNeoWalletAddress = transaction.metadataNeoWalletAddress;
      const metadataNeoTransactionId = transaction.metadataNeoTransactionId;
      let neoLink;
      if (metadataNeoTransactionId) {
        neoLink = <NeoTransactionLink transactionId={metadataNeoTransactionId}/>;
      } else if (metadataNeoWalletAddress) {
        neoLink = <NeoAddressLink address={metadataNeoWalletAddress}/>;
      }
      return (
        <FormattedMessage
          {...message}
          values={{neoLink}}
        />
      );
    }
    return <FormattedMessage {...message}/>;
  }
}

const typeHelpers: {[key: number]: UserWalletTransactionTypeHelper} = [];
typeHelpers[UserWalletTransactionType.REFERRAL] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.REFERRAL,
  MemberRewardsTransactionMessages.Referral
);
typeHelpers[UserWalletTransactionType.REFERRAL_TOP_10] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.REFERRAL_TOP_10,
  MemberRewardsTransactionMessages.ReferralTop10
);
typeHelpers[UserWalletTransactionType.NICHE_REFUND] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.NICHE_REFUND,
  MemberRewardsTransactionMessages.NicheRefund
);
typeHelpers[UserWalletTransactionType.PUBLICATION_REFUND] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.PUBLICATION_REFUND,
  MemberRewardsTransactionMessages.PublicationRefund
);
typeHelpers[UserWalletTransactionType.DELETED_USER_ABANDONED_BALANCES] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.DELETED_USER_ABANDONED_BALANCES,
  MemberRewardsTransactionMessages.DeletedUserAbandonedBalance
);
typeHelpers[UserWalletTransactionType.REFUND_REVERSAL] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.REFUND_REVERSAL,
  MemberRewardsTransactionMessages.NicheRefundReversal
);
typeHelpers[UserWalletTransactionType.CONTENT_REWARD] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.CONTENT_REWARD,
  MemberRewardsTransactionMessages.ContentReward
);
typeHelpers[UserWalletTransactionType.NICHE_OWNERSHIP_REWARD] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.NICHE_OWNERSHIP_REWARD,
  MemberRewardsTransactionMessages.NicheOwnershipReward
);
typeHelpers[UserWalletTransactionType.NICHE_MODERATION_REWARD] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.NICHE_MODERATION_REWARD,
  MemberRewardsTransactionMessages.NicheModeratorReward
);
typeHelpers[UserWalletTransactionType.ACTIVITY_REWARD] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.ACTIVITY_REWARD,
  MemberRewardsTransactionMessages.ActivityReward
);
typeHelpers[UserWalletTransactionType.TRIBUNAL_REWARD] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.TRIBUNAL_REWARD,
  MemberRewardsTransactionMessages.TribunalReward
);
typeHelpers[UserWalletTransactionType.ELECTORATE_REWARD] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.ELECTORATE_REWARD,
  MemberRewardsTransactionMessages.ElectorateReward
);
typeHelpers[UserWalletTransactionType.USER_TIP] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.USER_TIP,
  MemberRewardsTransactionMessages.UserTipReceived
);
typeHelpers[UserWalletTransactionType.USER_REDEMPTION] = new UserWalletTransactionTypeHelper(
  UserWalletTransactionType.USER_REDEMPTION,
  MemberRewardsTransactionMessages.UserRedemption
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedUserWalletTransactionType =
  new EnumEnhancer<UserWalletTransactionType, UserWalletTransactionTypeHelper>(
  typeHelpers
);
