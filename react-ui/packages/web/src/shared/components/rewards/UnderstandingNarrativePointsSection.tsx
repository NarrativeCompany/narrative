import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { Heading } from '../Heading';
import { Link } from '../Link';
import { Block } from '../Block';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { Paragraph } from '../Paragraph';

export const UnderstandingNarrativePointsSection: React.SFC<{}> = () => {
  const rewardPointsLink = (
    <Link.About type="rewards" size="inherit">
      <FormattedMessage {...SharedComponentMessages.RewardPoints}/>
    </Link.About>
  );
  const nrveTokensLink = (
    <Link.About type="nrve" size="inherit">
      <FormattedMessage {...SharedComponentMessages.NrveTokens}/>
    </Link.About>
  );
  return (
    <Block style={{marginBottom: 30}} size="large">
      <Heading size={4}>
        <FormattedMessage {...SharedComponentMessages.UnderstandingNarrativePoints}/>
      </Heading>
      <Paragraph size="large">
        <FormattedMessage
          {...SharedComponentMessages.UnderstandingNarrativePointsBody}
          values={{rewardPointsLink, nrveTokensLink}}
        />
      </Paragraph>
    </Block>
  );
};
