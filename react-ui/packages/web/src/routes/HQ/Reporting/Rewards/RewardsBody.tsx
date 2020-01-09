import * as React from 'react';
import { compose, withProps } from 'recompose';
import { RewardPeriodStats, WithExtractedRewardPeriodStatsProps, withRewardPeriodStats } from '@narrative/shared';
import {
  fullPlaceholder,
  withLoadingPlaceholder,
  WithLoadingPlaceholderProps
} from '../../../../shared/utils/withLoadingPlaceholder';
import { RewardsCreditedTable } from './RewardsCreditedTable';
import { SourceOfRewardsFundTable } from './SourceOfRewardsFundTable';
import { Block } from '../../../../shared/components/Block';

interface ParentProps {
  month: string;
}

export interface WithRewardPeriodStatsProps {
  rewardPeriodStats: RewardPeriodStats;
}

const RewardsBodyComponent: React.SFC<WithRewardPeriodStatsProps> = (props) => {
  const { rewardPeriodStats } = props;

  return (
    <Block style={{marginBottom: 20, borderBottom: '1px solid #dfdfdf', paddingBottom: 20}}>
      <RewardsCreditedTable rewardPeriodStats={rewardPeriodStats}/>
      <SourceOfRewardsFundTable rewardPeriodStats={rewardPeriodStats}/>
    </Block>
  );
};

export const RewardsBody = compose(
  withRewardPeriodStats,
  withProps<WithLoadingPlaceholderProps, WithExtractedRewardPeriodStatsProps>((props) => {
    const { loading } = props;

    return { loading };
  }),
  withLoadingPlaceholder(fullPlaceholder)
)(RewardsBodyComponent) as React.ComponentClass<ParentProps>;
