import * as React from 'react';
import { RewardsMessages } from '../../i18n/RewardsMessages';
import { WithExtractedAllTimeRewardsProps } from '@narrative/shared';
import { RewardsHeader } from './RewardsHeader';
import { compose, withProps } from 'recompose';
import { getRewardsHeaderTitle } from '../../utils/rewardsUtils';

export interface AllTimeRewardsHeaderParentProps {
  description: React.ReactNode;
}

type ParentProps = AllTimeRewardsHeaderParentProps & WithExtractedAllTimeRewardsProps;

export const AllTimeRewardsHeader = compose(
  withProps((props: ParentProps) => {
    const { loading, allTimeRewards } = props;

    return {
      title: getRewardsHeaderTitle(RewardsMessages.AllTimeRewardPointsPayout, loading, allTimeRewards)
    };
  })
)(RewardsHeader) as React.ComponentClass<ParentProps>;
