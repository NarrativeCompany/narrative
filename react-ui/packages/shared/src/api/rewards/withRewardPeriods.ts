import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { rewardPeriodsQuery } from '../graphql/rewards/rewardPeriodsQuery';
import { RewardPeriodsQuery } from '../../types';
import { WithExtractedRewardPeriodsProps } from './rewardsUtils';

const queryName = 'rewardPeriodsData';

type WithRewardPeriodsProps = NamedProps<{[queryName]: GraphqlQueryControls & RewardPeriodsQuery}, {}>;

export const withRewardPeriods =
  graphql<{}, RewardPeriodsQuery, {}, WithExtractedRewardPeriodsProps>(rewardPeriodsQuery, {
    name: queryName,
    props: ({ rewardPeriodsData, ownProps }: WithRewardPeriodsProps) => {
      const { loading } = rewardPeriodsData;
      const rewardPeriods = rewardPeriodsData && rewardPeriodsData.getRewardPeriods || [];

      return { ...ownProps, loading, rewardPeriods };
    }
  });
