import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { allTimePostRewardsQuery } from '../graphql/post/allTimePostRewardsQuery';
import { NrveUsdValue, AllTimePostRewardsQuery } from '../../types';
import { LoadingProps } from '../../utils';

const queryName = 'allTimePostRewardsData';

export interface WithExtractedAllTimePostRewardsProps extends LoadingProps {
  allTimePostRewards: NrveUsdValue;
}

type WithAllTimePostRewardsProps = NamedProps<{[queryName]: GraphqlQueryControls & AllTimePostRewardsQuery}, {}>;

export const withAllTimePostRewards =
  graphql<{}, AllTimePostRewardsQuery, {}, WithExtractedAllTimePostRewardsProps>(allTimePostRewardsQuery, {
    name: queryName,
    props: ({ allTimePostRewardsData, ownProps }: WithAllTimePostRewardsProps) => {
      const { loading } = allTimePostRewardsData;
      const getAllTimePostRewards = allTimePostRewardsData.getAllTimePostRewards;
      const allTimePostRewards = getAllTimePostRewards && getAllTimePostRewards.value;

      return { ...ownProps, loading, allTimePostRewards };
    }
  });
