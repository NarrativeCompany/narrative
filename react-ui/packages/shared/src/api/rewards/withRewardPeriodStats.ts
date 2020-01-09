import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { RewardPeriodStats, RewardPeriodStatsQuery, RewardPeriodStatsQueryVariables } from '../../types';
import { rewardPeriodStatsQuery } from '../graphql/rewards/rewardPeriodStatsQuery';
import { LoadingProps } from '../../utils';

const queryName = 'rewardPeriodStatsData';

interface ParentProps {
  month: string;
}

export type RewardPeriodStatsParentProps = ParentProps;

type WithRewardPeriodStatsProps = NamedProps<
  {[queryName]: GraphqlQueryControls & RewardPeriodStatsQuery},
  ParentProps
>;

export interface WithExtractedRewardPeriodStatsProps extends LoadingProps {
  rewardPeriodStats: RewardPeriodStats;
}

export const withRewardPeriodStats =
  graphql<
    ParentProps,
    RewardPeriodStatsQuery,
    RewardPeriodStatsQueryVariables,
    WithExtractedRewardPeriodStatsProps
  >(rewardPeriodStatsQuery, {
    name: queryName,
    options: (props: ParentProps) => {
      const { month } = props;
      return {
        variables: {
          input: {
            month
          }
        }
      };
    },
    props: ({ rewardPeriodStatsData, ownProps }: WithRewardPeriodStatsProps) => {
      const { loading } = rewardPeriodStatsData;
      const rewardPeriodStats = rewardPeriodStatsData && rewardPeriodStatsData.getRewardPeriodStats;

      return { ...ownProps, loading, rewardPeriodStats };
    }
  });
