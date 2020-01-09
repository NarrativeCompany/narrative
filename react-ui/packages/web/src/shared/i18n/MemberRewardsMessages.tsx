import { defineMessages } from 'react-intl';

export const MemberRewardsMessages = defineMessages({
  OverviewSeoTitle: {
    id: 'memberRewards.overviewSeoTitle',
    defaultMessage: '{displayName} - Rewards'
  },
  TransactionsSeoTitle: {
    id: 'memberRewards.transactionsSeoTitle',
    defaultMessage: '{displayName} - Reward Transactions'
  },
  NoRewards: {
    id: 'memberRewards.noRewards',
    defaultMessage: 'This member has not earned any {narrativeRewardsLink}.'
  },
  Overview: {
    id: 'memberRewards.overview',
    defaultMessage: 'Overview'
  },
  Transactions: {
    id: 'memberRewards.transactions',
    defaultMessage: 'Transactions'
  },
  Redeem: {
    id: 'memberRewards.redeem',
    defaultMessage: 'Redeem'
  },
  NarrativePointBalance: {
    id: 'memberRewards.narrativePointBalance',
    defaultMessage: 'Narrative Point Balance: {points}'
  },
  ContentCreation: {
    id: 'memberRewards.contentCreation',
    defaultMessage: 'Content Creation'
  },
  NicheOwnership: {
    id: 'memberRewards.nicheOwnership',
    defaultMessage: 'Niche Ownership'
  },
  NicheModeration: {
    id: 'memberRewards.nicheModeration',
    defaultMessage: 'Niche Moderation'
  },
  ActivityRewards: {
    id: 'memberRewards.activityRewards',
    defaultMessage: 'Activity Rewards'
  },
  ActivityRewardsHighRepBonus: {
    id: 'memberRewards.ActivityRewardsHighRepBonus',
    defaultMessage: 'This includes a {bonusPercentage}% High Reputation bonus.'
  },
  ActivityRewardsFounderBonus: {
    id: 'memberRewards.ActivityRewardsFounderBonus',
    defaultMessage: 'This includes a 10% Founding Member bonus.'
  },
  Tribunal: {
    id: 'memberRewards.tribunal',
    defaultMessage: 'Tribunal'
  },
  TotalRewardPointsEarned: {
    id: 'memberRewards.totalRewardPointsEarned',
    defaultMessage: 'Total Reward Points Earned'
  },
  TotalNarrativeRewardsPayout: {
    id: 'memberRewards.totalNarrativeRewardsPayout',
    defaultMessage: 'total Narrative Rewards payout'
  },
  EarningsPercentageOfTotal: {
    id: 'memberRewards.earningsPercentageOfTotal',
    defaultMessage: 'Your earnings represent {earningsPercentage}% of the {totalPayout} for this period.'
  },
  NeoWallet: {
    id: 'memberRequestRedemptionModal.neoWallet',
    defaultMessage: 'NEO Wallet'
  },
  HowToRedeemRewardPoints: {
    id: 'memberRequestRedemptionModal.howToRedeemRewardPoints',
    defaultMessage: 'How To Redeem Your Reward Points'
  },
  NEONWallet: {
    id: 'memberRequestRedemptionModal.neonWallet',
    defaultMessage: 'NEON Wallet'
  },
  ReadOurFaq: {
    id: 'memberRequestRedemptionModal.readOurFaq',
    defaultMessage: 'read our FAQ'
  },
  PointRedemptionExplanationIntro: {
    id: 'memberRequestRedemptionModal.pointRedemptionExplanationIntro',
    defaultMessage: 'You may redeem your Reward Points for Narrative tokens, which are called {nrveLink}. '
  },
  PointRedemptionExplanation: {
    id: 'memberRequestRedemptionModal.pointRedemptionExplanation',
    defaultMessage: 'NRVE is a cryptocurrency that you can take ownership of and store via a NEO cryptocurrency ' +
      'wallet. We recommend {neonWalletLink}. If you do not already have a NEO wallet, {readOurFaqLink} for setting ' +
      'up your own wallet.'
  },
  ClickButtonToAddYourWallet: {
    id: 'memberRequestRedemptionModal.clickButtonToAddYourWallet',
    defaultMessage: 'Once you have your own wallet, click on the button below to add it to your Narrative account. ' +
      'Once the wallet is linked to your Narrative account, you will be able to request redemptions.'
  },
  IfYouHaveQuestions: {
    id: 'memberRequestRedemptionModal.ifYouHaveQuestions',
    defaultMessage: 'If you have any questions about setting up a wallet or the redemption process in general, visit ' +
      'our support site at {supportSiteLink}.'
  },
  SetYourNEOWalletAddress: {
    id: 'memberRequestRedemptionModal.setYourNEOWalletAddress',
    defaultMessage: 'Set Your NEO Wallet Address'
  },
  YourNeoWalletIsInWaitingPeriod: {
    id: 'memberRequestRedemptionModal.yourNeoWalletIsInWaitingPeriod',
    defaultMessage: 'You must wait two days after setting your {neoWalletLink} address to request a Redemption.' +
      ' You will be able to request a Redemption {availableDatetime}.'
  },
  PendingTransaction: {
    id: 'memberRequestRedemptionModal.pendingTransaction',
    defaultMessage: 'pending Redemption'
  },
  PendingTransactionWarning: {
    id: 'memberRequestRedemptionModal.pendingTransactionWarning',
    defaultMessage: 'You currently have a {pendingTransactionText}, so you cannot request another Redemption.'
  },
  RequestRedemption: {
    id: 'memberRequestRedemptionForm.requestRedemption',
    defaultMessage: 'Request Redemption'
  },
  RequestRedemptionDescription: {
    id: 'memberRequestRedemptionForm.requestRedemptionDescription',
    defaultMessage: 'You may redeem your Reward Points as {nrveLink}. Each Reward Point is equal to 1 {nrveLink}.' +
      ' Redemption requests will be processed within ten days and will be sent to the {neoWalletLink} that you' +
      ' listed on your account.'
  },
  RequestRedemptionDescriptionNeoWalletLink: {
    id: 'memberRequestRedemptionForm.requestRedemptionDescriptionNeoWalletLink',
    defaultMessage: 'NEO Wallet'
  },
  CurrentBalance: {
    id: 'memberRequestRedemptionForm.currentBalance',
    defaultMessage: 'Narrative Point Balance: {currentBalance}'
  },
  RedemptionAmount: {
    id: 'memberRequestRedemptionForm.redemptionAmount',
    defaultMessage: 'Redemption Amount'
  },
  SubmitRequestBtnText: {
    id: 'memberRequestRedemptionForm.submitRequestBtnText',
    defaultMessage: 'Submit'
  },
  UsdEstimateDisclaimer: {
    id: 'memberRequestRedemptionForm.usdEstimateDisclaimer',
    defaultMessage: '* USD value is an estimate and is not guaranteed.'
  },
  RedemptionRequestedTitle: {
    id: 'memberRequestRedemptionForm.redemptionRequestedTitle',
    defaultMessage: 'Redemption Requested'
  },
  RedemptionRequestedDescription: {
    id: 'memberRequestRedemptionForm.redemptionRequestedDescription',
    defaultMessage: 'Your Redemption request has been submitted. Redemptions are typically processed within 10' +
      ' days. You will receive an email confirmation once it has been processed.'
  },
  CancelRedemptionRequestConfirmation: {
    id: 'memberRewards.cancelRedemptionRequestConfirmation',
    defaultMessage: 'Are you sure you want to cancel this Redemption request?'
  },
  CancelRedemptionRequestYesText: {
    id: 'memberRewards.cancelRedemptionRequestYesText',
    defaultMessage: 'Yes'
  },
  CancelRedemptionRequestNoText: {
    id: 'memberRewards.cancelRedemptionRequestNoText',
    defaultMessage: 'No'
  },
  CancelLinkText: {
    id: 'memberRewards.cancelLinkText',
    defaultMessage: 'Cancel'
  },
});
