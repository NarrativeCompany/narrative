import * as React from 'react';
import { MemberReferralDetailsMessages } from '../../../shared/i18n/MemberReferralDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { Paragraph } from '../../../shared/components/Paragraph';
import { Link } from '../../../shared/components/Link';
import { externalUrls } from '../../../shared/constants/externalUrls';

export const MemberReferralDetailsIntro: React.SFC<{}> = () => {
  const viewTheRulesLink = (
    <Link.Anchor
      target="_blank"
      href={externalUrls.narrativeReferralRewards}
    >
      <FormattedMessage {...MemberReferralDetailsMessages.ViewTheRules} />
    </Link.Anchor>
  );

  const nrveLink = <Link.About type="nrve"/>;

  return (
    <React.Fragment>
      <Paragraph marginBottom="large">
        <FormattedMessage {...MemberReferralDetailsMessages.HelpGrowMessage} values={{nrveLink}}/>
      </Paragraph>

      <Paragraph>
        <FormattedMessage {...MemberReferralDetailsMessages.TopTenDescription} values={{viewTheRulesLink, nrveLink}}/>
      </Paragraph>
    </React.Fragment>
  );
};
