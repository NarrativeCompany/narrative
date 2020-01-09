import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { WithExtractedRewardPeriodsProps } from '../rewards';
import { UserRewardPeriodsQuery, UserRewardPeriodsQueryVariables } from '../../types';
import { userRewardPeriodsQuery } from '../graphql/user/userRewardPeriodsQuery';

const queryName = 'userRewardPeriodsData';

interface ParentProps {
  userOid: string;
}

type WithUserRewardPeriodsProps = NamedProps<
  {[queryName]: GraphqlQueryControls & UserRewardPeriodsQuery},
  ParentProps
>;

export const withUserRewardPeriods =
  graphql<
    ParentProps,
    UserRewardPeriodsQuery,
    UserRewardPeriodsQueryVariables,
    WithExtractedRewardPeriodsProps
   >(userRewardPeriodsQuery, {
     name: queryName,
    options: ({userOid}: ParentProps) => ({
      variables: {
        input: {userOid}
      }
    }),
    props: ({ userRewardPeriodsData, ownProps }: WithUserRewardPeriodsProps) => {
      const { loading } = userRewardPeriodsData;

      const rewardPeriods = userRewardPeriodsData && userRewardPeriodsData.getUserRewardPeriods || [];

      return { ...ownProps, loading, rewardPeriods };
    }
  });
