import { defineMessages } from 'react-intl';

export const MemberNeoWalletMessages = defineMessages({
  MemberNeoWalletSeoTitle: {
    id: 'memberNeoWallet.memberNeoWalletSeoTitle',
    defaultMessage: 'Member CP - NEO Wallet'
  },
  MemberNeoWalletDescParagraphOneIntro: {
    id: 'memberNeoWallet.memberNeoWalletDescParagraphOneIntro',
    defaultMessage: 'You\'ll need a NEO wallet to redeem your {rewardPointsLink} for Narrative tokens ({nrveLink}). '
  },
  MemberNeoWalletDescParagraphTwo: {
    id: 'memberNeoWallet.memberNeoWalletDescParagraphTwo',
    defaultMessage: 'Configure your NEO wallet address below.'
  },
  RewardPointsLinkText: {
    id: 'memberNeoWallet.rewardPointsLinkText',
    defaultMessage: 'Reward Points'
  },
  NeoAddress: {
    id: 'memberNeoWallet.neoAddress',
    defaultMessage: 'NEO Address'
  },
  PendingRedemption: {
    id: 'memberNeoWalletBody.pendingRedemption',
    defaultMessage: 'Pending Redemption'
  },
  PendingRedemptionTooltip: {
    id: 'memberNeoWalletBody.pendingRedemptionTooltip',
    defaultMessage: 'You cannot update your NEO Address because you have a pending Redemption.'
  },
  YouDoNotHaveWalletSet: {
    id: 'memberNeoWalletBody.youDoNotHaveWalletSet',
    defaultMessage: 'You do not have a wallet set. {addWalletLink}.'
  },
  AddWallet: {
    id: 'memberNeoWalletBody.addWallet',
    defaultMessage: 'Add wallet'
  },
  PendingWaitingPeriod: {
    id: 'memberNeoWalletBody.pendingWaitingPeriod',
    defaultMessage: 'Pending Waiting Period (Ends in {countdown})'
  },
  WalletActive: {
    id: 'memberNeoWalletBody.walletActive',
    defaultMessage: 'Active'
  },
  WalletAddress: {
    id: 'memberNeoWalletDetails.walletAddress',
    defaultMessage: 'Wallet'
  },
  ChangeWallet: {
    id: 'memberNeoWalletDetails.changeWallet',
    defaultMessage: 'Change'
  },
  DeleteWallet: {
    id: 'memberNeoWalletDetails.deleteWallet',
    defaultMessage: 'Remove'
  },
  WalletStatus: {
    id: 'memberNeoWalletDetails.walletStatus',
    defaultMessage: 'Status'
  },
  UpdateNeoWalletTitle: {
    id: 'updateMemberNeoWalletForm.updateNeoWalletTitle',
    defaultMessage: 'Update NEO Wallet'
  },
  DeleteNeoWalletTitle: {
    id: 'deleteMemberNeoWalletForm.verifyNarrativeAccount',
    defaultMessage: 'Remove NEO Wallet'
  },
  UpdateNeoWalletDescription: {
    id: 'updateMemberNeoWalletForm.updateNeoWalletDescription',
    defaultMessage: 'You must verify your account in order to make changes to your NEO Wallet.'
  },
  NeoAddressLabel: {
    id: 'memberNeoWalletForm.neoAddressLabel',
    defaultMessage: 'NEO Address'
  },
  SubmitNeoAddressBtnText: {
    id: 'memberNeoWalletForm.submitNeoAddressBtnText',
    defaultMessage: 'Update'
  },
  DeleteNeoAddressBtnText: {
    id: 'deleteMemberNeoWalletForm.deleteNeoAddressBtnText',
    defaultMessage: 'Submit'
  },
  NeoAddressUpdated: {
    id: 'memberNeoWalletForm.neoAddressUpdated',
    defaultMessage: 'NEO Address Updated'
  },
  NeoAddressWaitingPeriod: {
    id: 'memberNeoWalletForm.neoAddressWaitingPeriod',
    defaultMessage: 'Your NEO address has been updated. You must now wait two days before requesting a Redemption ' +
      'for security purposes.'
  },
  NeoAddressDeleted: {
    id: 'memberNeoWalletForm.neoAddressDeleted',
    defaultMessage: 'NEO Address Removed'
  },
});
