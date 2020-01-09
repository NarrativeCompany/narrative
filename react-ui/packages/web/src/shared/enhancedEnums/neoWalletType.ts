import { FormattedMessage } from 'react-intl';
import { NeoWalletType } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { NeoWalletsExplainerMessages } from '../i18n/NeoWalletsExplainerMessages';

export class NeoWalletTypeHelper {
  type: NeoWalletType;
  name: FormattedMessage.MessageDescriptor;
  description: FormattedMessage.MessageDescriptor;

  constructor(
    type: NeoWalletType,
    name: FormattedMessage.MessageDescriptor,
    description: FormattedMessage.MessageDescriptor
  ) {
    this.type = type;
    this.name = name;
    this.description = description;
  }

  isNrveSmartContract(): boolean {
    return this.type === NeoWalletType.NRVE_SMART_CONTRACT;
  }

  isNarrativeCompany(): boolean {
    return this.type === NeoWalletType.NARRATIVE_COMPANY;
  }
}

const neoWalletTypeHelpers: {[key: number]: NeoWalletTypeHelper} = [];
neoWalletTypeHelpers[NeoWalletType.NRVE_SMART_CONTRACT] = new NeoWalletTypeHelper(
  NeoWalletType.NRVE_SMART_CONTRACT,
  NeoWalletsExplainerMessages.NRVESmartContract,
  NeoWalletsExplainerMessages.NRVESmartContractDescription
);
neoWalletTypeHelpers[NeoWalletType.NARRATIVE_COMPANY] = new NeoWalletTypeHelper(
  NeoWalletType.NARRATIVE_COMPANY,
  NeoWalletsExplainerMessages.NarrativeCompany,
  NeoWalletsExplainerMessages.NarrativeCompanyDescription
);
neoWalletTypeHelpers[NeoWalletType.TEAM_TOKEN] = new NeoWalletTypeHelper(
  NeoWalletType.TEAM_TOKEN,
  NeoWalletsExplainerMessages.TeamToken,
  NeoWalletsExplainerMessages.TeamTokenDescription
);
neoWalletTypeHelpers[NeoWalletType.REFERRALS_AND_INCENTIVES] = new NeoWalletTypeHelper(
  NeoWalletType.REFERRALS_AND_INCENTIVES,
  NeoWalletsExplainerMessages.ReferralsAndIncentives,
  NeoWalletsExplainerMessages.ReferralsAndIncentivesDescription
);
neoWalletTypeHelpers[NeoWalletType.TOKEN_MINT] = new NeoWalletTypeHelper(
  NeoWalletType.TOKEN_MINT,
  NeoWalletsExplainerMessages.TokenMint,
  NeoWalletsExplainerMessages.TokenMintDescription
);
neoWalletTypeHelpers[NeoWalletType.NICHE_PAYMENT] = new NeoWalletTypeHelper(
  NeoWalletType.NICHE_PAYMENT,
  NeoWalletsExplainerMessages.NichePayment,
  NeoWalletsExplainerMessages.NichePaymentDescription
);
neoWalletTypeHelpers[NeoWalletType.PUBLICATION_PAYMENT] = new NeoWalletTypeHelper(
  NeoWalletType.PUBLICATION_PAYMENT,
  NeoWalletsExplainerMessages.PublicationPayment,
  NeoWalletsExplainerMessages.PublicationPaymentDescription
);
neoWalletTypeHelpers[NeoWalletType.CHANNEL_FIAT_HOLDING] = new NeoWalletTypeHelper(
  NeoWalletType.CHANNEL_FIAT_HOLDING,
  NeoWalletsExplainerMessages.ChannelFiatHolding,
  NeoWalletsExplainerMessages.ChannelFiatHoldingDescription
);
neoWalletTypeHelpers[NeoWalletType.MONTHLY_REWARDS] = new NeoWalletTypeHelper(
  NeoWalletType.MONTHLY_REWARDS,
  NeoWalletsExplainerMessages.MonthlyRewards,
  NeoWalletsExplainerMessages.MonthlyRewardsDescription
);
neoWalletTypeHelpers[NeoWalletType.MEMBER_CREDITS] = new NeoWalletTypeHelper(
  NeoWalletType.MEMBER_CREDITS,
  NeoWalletsExplainerMessages.MemberCredits,
  NeoWalletsExplainerMessages.MemberCreditsDescription
);
neoWalletTypeHelpers[NeoWalletType.PRORATED_NICHE_REVENUE] = new NeoWalletTypeHelper(
  NeoWalletType.PRORATED_NICHE_REVENUE,
  NeoWalletsExplainerMessages.ProratedNicheRevenue,
  NeoWalletsExplainerMessages.ProratedNicheRevenueDescription
);
neoWalletTypeHelpers[NeoWalletType.PRORATED_PUBLICATION_REVENUE] = new NeoWalletTypeHelper(
  NeoWalletType.PRORATED_PUBLICATION_REVENUE,
  NeoWalletsExplainerMessages.ProratedPublicationRevenue,
  NeoWalletsExplainerMessages.ProratedPublicationRevenueDescription
);

export const EnhancedNeoWalletType =
  new EnumEnhancer<NeoWalletType, NeoWalletTypeHelper>(
  neoWalletTypeHelpers
);
