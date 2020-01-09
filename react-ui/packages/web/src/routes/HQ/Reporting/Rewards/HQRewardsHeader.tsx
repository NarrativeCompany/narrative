import * as React from 'react';
import { compose, withProps } from 'recompose';
import { withAllTimeRewards } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { RewardsMessages } from '../../../../shared/i18n/RewardsMessages';
import {
  AllTimeRewardsHeader,
  AllTimeRewardsHeaderParentProps
} from '../../../../shared/components/rewards/AllTimeRewardsHeader';

export const HQRewardsHeader = compose(
  withAllTimeRewards,
  withProps<AllTimeRewardsHeaderParentProps, {}>((): AllTimeRewardsHeaderParentProps => ({
    description: <FormattedMessage {...RewardsMessages.RevenuePayoutDescription}/>
  }))
)(AllTimeRewardsHeader) as React.ComponentClass<{}>;
