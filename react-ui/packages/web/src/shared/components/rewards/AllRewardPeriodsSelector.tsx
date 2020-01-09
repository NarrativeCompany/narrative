import * as React from 'react';
import { compose, withProps } from 'recompose';
import { withRewardPeriods } from '@narrative/shared';
import { RewardPeriodSelector, RewardPeriodSelectorProps } from './RewardPeriodSelector';
import { FormattedMessage } from 'react-intl';
import { RewardsMessages } from '../../i18n/RewardsMessages';

export const AllRewardPeriodsSelector = compose(
  withRewardPeriods,
  withProps(() => ({
    description: <FormattedMessage {...RewardsMessages.RevenuePayoutDescription}/>
  }))
)(RewardPeriodSelector) as React.ComponentClass<RewardPeriodSelectorProps>;
