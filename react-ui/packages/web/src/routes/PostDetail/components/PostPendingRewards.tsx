import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { PostDetailMessages } from '../../../shared/i18n/PostDetailMessages';
import { Block } from '../../../shared/components/Block';
import { Paragraph } from '../../../shared/components/Paragraph';
import { Link } from '../../../shared/components/Link';

export const PostPendingRewards: React.SFC<{}> = () => {
  const narrativeRewardsLink = <Link.About type="rewards"/>;
  return (
    <Block>
      <Paragraph style={{marginBottom: 10}}>
        <FormattedMessage {...PostDetailMessages.InitialPayoutPending}/>
      </Paragraph>
      <Paragraph>
        <FormattedMessage {...PostDetailMessages.ContentCreatorsEarn} values={{narrativeRewardsLink}}/>
      </Paragraph>
    </Block>
  );
};
