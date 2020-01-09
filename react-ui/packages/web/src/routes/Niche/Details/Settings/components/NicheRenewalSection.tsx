import * as React from 'react';
import { WithNicheDetailsContextProps } from '../../components/NicheDetailsContext';
import { FormattedMessage } from 'react-intl';
import { NicheSettingsMessages } from '../../../../../shared/i18n/NicheSettingsMessages';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { LocalizedTime } from '../../../../../shared/components/LocalizedTime';
import { ChannelDetailsSection } from '../../../../../shared/components/channel/ChannelDetailsSection';

export const NicheRenewalSection: React.SFC<WithNicheDetailsContextProps> = (props) => {
  const { nicheDetail: { niche: { renewalDatetime } } } = props;

  // jw: nothing to output if there is no renewal datetime.
  if (!renewalDatetime) {
    return null;
  }

  return (
    <ChannelDetailsSection title={<FormattedMessage {...NicheSettingsMessages.NicheRenewalDate}/>}>
      <Paragraph>
        <FormattedMessage {...NicheSettingsMessages.NicheWillRenewAt}/>
        <strong>
          <LocalizedTime time={renewalDatetime} />
        </strong>
      </Paragraph>
      <Paragraph style={{marginBottom: 35}}>
        <FormattedMessage {...NicheSettingsMessages.NicheRenewalInfo}/>
      </Paragraph>
    </ChannelDetailsSection>
  );
};
