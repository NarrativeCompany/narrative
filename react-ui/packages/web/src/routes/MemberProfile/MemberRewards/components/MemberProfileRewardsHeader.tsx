import * as React from 'react';
import { compose, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { RewardsMessages } from '../../../../shared/i18n/RewardsMessages';
import { RewardsHeader } from '../../../../shared/components/rewards/RewardsHeader';

export const MemberProfileRewardsHeader = compose(
  withProps(() => {
    return {
      description: <FormattedMessage {...RewardsMessages.RevenuePayoutDescription}/>
    };
  })
)(RewardsHeader) as React.ComponentClass<{}>;
