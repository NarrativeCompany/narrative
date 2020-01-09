import * as React from 'react';
import { PublicationProfile } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { ChannelDetailsSection } from '../../../../../../shared/components/channel/ChannelDetailsSection';
import {
  EnhancedPublicationContentRewardWriterShare
} from '../../../../../../shared/enhancedEnums/publicationContentRewardWriterShare';
import {
  EnhancedPublicationContentRewardRecipientType
} from '../../../../../../shared/enhancedEnums/publicationContentRewardRecipientType';
import { PublicationDetailsMessages } from '../../../../../../shared/i18n/PublicationDetailsMessages';

interface Props {
  profile: PublicationProfile;
}

export const WriterRevenueShareSection: React.SFC<Props> = (props) => {
  const { profile: { contentRewardWriterShare, contentRewardRecipient } } = props;

  const writerShare = EnhancedPublicationContentRewardWriterShare.get(contentRewardWriterShare);
  const writerPercentage = writerShare.getWriterPercentageString();

  let description: React.ReactNode | undefined;
  if (writerShare.isOneHundredPercent()) {
    description = <FormattedMessage {...PublicationDetailsMessages.WriterRevenueShareOneHundredPercentDescription}/>;

  } else {
    if (!contentRewardRecipient) {
      // todo:error-handling: We should ALWAYS have a content reward recipient if the writer share is less than 100%
      return null;
    }
    const recipient = EnhancedPublicationContentRewardRecipientType.get(contentRewardRecipient);
    const recipientName = <FormattedMessage {...recipient.shortName}/>;

    description = (
      <FormattedMessage
        {...PublicationDetailsMessages.WriterRevenueShareDescription}
        values={{writerPercentage, recipientName}}/>
    );
  }

  return (
    <ChannelDetailsSection
      title={<FormattedMessage {...PublicationDetailsMessages.WriterRevenueShareTitle} values={{writerPercentage}}/>}
    >
      {description}
    </ChannelDetailsSection>
  );
};
