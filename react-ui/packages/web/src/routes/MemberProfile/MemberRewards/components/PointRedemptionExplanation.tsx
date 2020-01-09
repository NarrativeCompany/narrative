import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { MemberRewardsMessages } from '../../../../shared/i18n/MemberRewardsMessages';
import { Link } from '../../../../shared/components/Link';
import { externalUrls } from '../../../../shared/constants/externalUrls';

export const PointRedemptionExplanation: React.SFC<{}> = () => {
  const neonWalletLink = (
    <Link.Anchor href={externalUrls.neonWallet} target="_blank">
      <FormattedMessage {...MemberRewardsMessages.NEONWallet}/>
    </Link.Anchor>
  );
  const readOurFaqLink = (
    <Link.Anchor href={externalUrls.narrativeHowDoIGetAWallet} target="_blank">
      <FormattedMessage {...MemberRewardsMessages.ReadOurFaq}/>
    </Link.Anchor>
  );

  return (
    <FormattedMessage
      {...MemberRewardsMessages.PointRedemptionExplanation}
      values={{neonWalletLink, readOurFaqLink}}
    />
  );
};
