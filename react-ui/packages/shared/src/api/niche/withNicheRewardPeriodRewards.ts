import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import {
  NicheRewardPeriodRewardsQuery,
  NicheRewardPeriodRewardsQueryVariables,
  NicheRewardPeriodStats
} from '../../types';
import { NichePeriodParentProps, NicheQueryParentProps } from './nicheLeaderboardUtils';
import { nicheRewardPeriodRewardsQuery } from '../graphql/niche/nicheRewardPeriodRewardsQuery';
import { LoadingProps } from '../../utils';

const queryName = 'nicheRewardPeriodRewardsData';

export interface WithExtractedNicheRewardPeriodRewardsProps extends LoadingProps {
  rewardPeriodStats: NicheRewardPeriodStats;
}

type WithNicheRewardPeriodRewardsProps = NamedProps<
  {[queryName]: GraphqlQueryControls & NicheRewardPeriodRewardsQuery},
  NicheQueryParentProps
>;

export const withNicheRewardPeriodRewards =
  graphql<
    NichePeriodParentProps,
    NicheRewardPeriodRewardsQuery,
    NicheRewardPeriodRewardsQueryVariables,
    WithExtractedNicheRewardPeriodRewardsProps
   >(nicheRewardPeriodRewardsQuery, {
     name: queryName,
    options: (props: NichePeriodParentProps) => {
      const { nicheOid, month } = props;
      return {
        variables: {
          nicheOid,
          input: {
            month
          }
        }
      };
    },
    props: ({ nicheRewardPeriodRewardsData, ownProps }: WithNicheRewardPeriodRewardsProps) => {
      const { loading } = nicheRewardPeriodRewardsData;

      const rewardPeriodStats = nicheRewardPeriodRewardsData &&
        nicheRewardPeriodRewardsData.getNicheRewardPeriodRewards;

      return { ...ownProps, loading, rewardPeriodStats };
    }
  });
