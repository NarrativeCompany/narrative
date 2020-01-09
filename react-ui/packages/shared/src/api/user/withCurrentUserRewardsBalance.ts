import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { CurrentUserRewardsBalanceQuery, NrveUsdValue } from '../../types';
import { currentUserRewardsBalanceQuery } from '../graphql/user/currentUserRewardsBalanceQuery';
import { LoadingProps } from '../../utils';

const queryName = 'currentUserRewardsBalanceData';

export interface WithExtractedCurrentUserRewardsBalanceProps extends LoadingProps {
  rewardsBalance: NrveUsdValue;
}

export type WithCurrentUserRewardsBalanceProps = NamedProps<
  {[queryName]: GraphqlQueryControls & CurrentUserRewardsBalanceQuery},
  {}
>;

export const withCurrentUserRewardsBalance =
  graphql<
    {},
    CurrentUserRewardsBalanceQuery,
    {},
    WithExtractedCurrentUserRewardsBalanceProps
  >(currentUserRewardsBalanceQuery, {
    name: queryName,
    props: ({ currentUserRewardsBalanceData, ownProps }: WithCurrentUserRewardsBalanceProps) => {
      const { loading } = currentUserRewardsBalanceData;

      const getCurrentUserRewardsBalance = currentUserRewardsBalanceData &&
        currentUserRewardsBalanceData.getCurrentUserRewardsBalance;
      const rewardsBalance = getCurrentUserRewardsBalance && getCurrentUserRewardsBalance.value;

      return { ...ownProps, loading, rewardsBalance };
    }
  });
