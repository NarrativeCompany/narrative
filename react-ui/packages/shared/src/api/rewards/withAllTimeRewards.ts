import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { allTimeRewardsQuery } from '../graphql/rewards/allTimeRewardsQuery';
import { AllTimeRewardsQuery } from '../../types';
import { WithExtractedAllTimeRewardsProps } from './rewardsUtils';

const queryName = 'allTimeRewardsData';

type WithAllTimeRewardsProps = NamedProps<{[queryName]: GraphqlQueryControls & AllTimeRewardsQuery}, {}>;

export const withAllTimeRewards =
  graphql<{}, AllTimeRewardsQuery, {}, WithExtractedAllTimeRewardsProps>(allTimeRewardsQuery, {
    name: queryName,
    props: ({ allTimeRewardsData, ownProps }: WithAllTimeRewardsProps) => {
      const { loading } = allTimeRewardsData;
      const getAllTimeRewards = allTimeRewardsData.getAllTimeRewards;
      const allTimeRewards = getAllTimeRewards && getAllTimeRewards.value;

      return { ...ownProps, loading, allTimeRewards };
    }
  });
