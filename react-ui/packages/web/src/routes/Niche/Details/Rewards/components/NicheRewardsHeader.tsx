import * as React from 'react';
import { compose, withProps } from 'recompose';
import { NicheQueryParentProps, withNicheAllTimeRewards } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { NicheRewardsMessages } from '../../../../../shared/i18n/NicheRewardsMessages';
import {
  AllTimeRewardsHeader,
  AllTimeRewardsHeaderParentProps
} from '../../../../../shared/components/rewards/AllTimeRewardsHeader';

export const NicheRewardsHeader = compose(
  withNicheAllTimeRewards,
  withProps<AllTimeRewardsHeaderParentProps, {}>((): AllTimeRewardsHeaderParentProps => ({
    description: <FormattedMessage {...NicheRewardsMessages.RevenuePayoutDescription}/>
  }))
)(AllTimeRewardsHeader) as React.ComponentClass<NicheQueryParentProps>;
