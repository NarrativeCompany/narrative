import { FormattedMessage } from 'react-intl';
import { PublicationContentRewardWriterShare } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { PublicationDetailsMessages } from '../i18n/PublicationDetailsMessages';

export class PublicationContentRewardWriterShareHelper {
  share: PublicationContentRewardWriterShare;
  writerPercentage: number;
  name: FormattedMessage.MessageDescriptor;

  constructor(
    share: PublicationContentRewardWriterShare,
    writerPercentage: number,
    name: FormattedMessage.MessageDescriptor
  ) {
    this.share = share;
    this.writerPercentage = writerPercentage;
    this.name = name;
  }

  isOneHundredPercent(): boolean {
    return this.share === PublicationContentRewardWriterShare.ONE_HUNDRED_PERCENT;
  }

  getWriterPercentageString(): string {
    return `${this.writerPercentage}%`;
  }

  getPublicationPercentageString(): string {
    return `${100 - this.writerPercentage}%`;
  }
}

const publicationContentRewardWriterShareHelpers: {[key: number]: PublicationContentRewardWriterShareHelper} = [];
publicationContentRewardWriterShareHelpers[PublicationContentRewardWriterShare.ONE_HUNDRED_PERCENT]
  = new PublicationContentRewardWriterShareHelper(
  PublicationContentRewardWriterShare.ONE_HUNDRED_PERCENT,
  100,
  PublicationDetailsMessages.OneHundredPercent
);
publicationContentRewardWriterShareHelpers[PublicationContentRewardWriterShare.NINETY_PERCENT]
  = new PublicationContentRewardWriterShareHelper(
  PublicationContentRewardWriterShare.NINETY_PERCENT,
  90,
  PublicationDetailsMessages.NinetyPercent
);
publicationContentRewardWriterShareHelpers[PublicationContentRewardWriterShare.SEVENTY_FIVE_PERCENT]
  = new PublicationContentRewardWriterShareHelper(
  PublicationContentRewardWriterShare.SEVENTY_FIVE_PERCENT,
  75,
  PublicationDetailsMessages.SeventyFivePercent
);
publicationContentRewardWriterShareHelpers[PublicationContentRewardWriterShare.FIFTY_PERCENT]
  = new PublicationContentRewardWriterShareHelper(
  PublicationContentRewardWriterShare.FIFTY_PERCENT,
  50,
  PublicationDetailsMessages.FiftyPercent
);
publicationContentRewardWriterShareHelpers[PublicationContentRewardWriterShare.TWENTY_FIVE_PERCENT]
  = new PublicationContentRewardWriterShareHelper(
  PublicationContentRewardWriterShare.TWENTY_FIVE_PERCENT,
  25,
  PublicationDetailsMessages.TwentyFivePercent
);
publicationContentRewardWriterShareHelpers[PublicationContentRewardWriterShare.TEN_PERCENT]
  = new PublicationContentRewardWriterShareHelper(
  PublicationContentRewardWriterShare.TEN_PERCENT,
  10,
  PublicationDetailsMessages.TenPercent
);
publicationContentRewardWriterShareHelpers[PublicationContentRewardWriterShare.ZERO_PERCENT]
  = new PublicationContentRewardWriterShareHelper(
  PublicationContentRewardWriterShare.ZERO_PERCENT,
  0,
  PublicationDetailsMessages.ZeroPercent
);

export const EnhancedPublicationContentRewardWriterShare =
  new EnumEnhancer<PublicationContentRewardWriterShare, PublicationContentRewardWriterShareHelper>(
  publicationContentRewardWriterShareHelpers
);
