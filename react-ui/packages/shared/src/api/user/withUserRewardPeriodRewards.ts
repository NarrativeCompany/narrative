import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import {
  UserRewardPeriodRewardsQuery,
  UserRewardPeriodRewardsQueryVariables,
  UserRewardPeriodStats
} from '../../types';
import { userRewardPeriodRewardsQuery } from '../graphql/user/userRewardPeriodRewardsQuery';
import { LoadingProps } from '../../utils';

const queryName = 'userRewardPeriodRewardsData';

export interface UserPeriodParentProps {
  userOid: string;
  month?: string;
}

export interface WithExtractedUserRewardPeriodRewardsProps extends LoadingProps {
  rewardPeriodStats: UserRewardPeriodStats;
}

type WithUserRewardPeriodRewardsProps = NamedProps<
  {[queryName]: GraphqlQueryControls & UserRewardPeriodRewardsQuery},
  UserPeriodParentProps
>;

export const withUserRewardPeriodRewards =
  graphql<
    UserPeriodParentProps,
    UserRewardPeriodRewardsQuery,
    UserRewardPeriodRewardsQueryVariables,
    WithExtractedUserRewardPeriodRewardsProps
   >(userRewardPeriodRewardsQuery, {
     name: queryName,
    options: (props: UserPeriodParentProps) => {
      const { userOid, month } = props;
      return {
        variables: {
          userOid,
          input: {
            month
          }
        }
      };
    },
    props: ({ userRewardPeriodRewardsData, ownProps }: WithUserRewardPeriodRewardsProps) => {
      const { loading } = userRewardPeriodRewardsData;

      const rewardPeriodStats = userRewardPeriodRewardsData &&
        userRewardPeriodRewardsData.getUserRewardPeriodRewards;

      return { ...ownProps, loading, rewardPeriodStats };
    }
  });
