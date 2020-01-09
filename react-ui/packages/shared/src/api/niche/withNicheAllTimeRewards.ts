import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { NicheAllTimeRewardsQuery, NicheAllTimeRewardsQueryVariables } from '../../types';
import { NicheQueryParentProps } from './nicheLeaderboardUtils';
import { WithExtractedAllTimeRewardsProps } from '../rewards';
import { nicheAllTimeRewardsQuery } from '../graphql/niche/nicheAllTimeRewardsQuery';

const queryName = 'nicheAllTimeRewardsData';

type WithNicheAllTimeRewardsProps = NamedProps<
  {[queryName]: GraphqlQueryControls & NicheAllTimeRewardsQuery},
  NicheQueryParentProps
>;

export const withNicheAllTimeRewards =
  graphql<
    NicheQueryParentProps,
    NicheAllTimeRewardsQuery,
    NicheAllTimeRewardsQueryVariables,
    WithExtractedAllTimeRewardsProps
   >(nicheAllTimeRewardsQuery, {
     name: queryName,
    options: (props: NicheQueryParentProps) => {
      const { nicheOid } = props;
      return {
        variables: {
          nicheOid
        }
      };
    },
    props: ({ nicheAllTimeRewardsData, ownProps }: WithNicheAllTimeRewardsProps) => {
      const { loading } = nicheAllTimeRewardsData;

      const getNicheAllTimeRewards = nicheAllTimeRewardsData && nicheAllTimeRewardsData.getNicheAllTimeRewards;
      const allTimeRewards = getNicheAllTimeRewards && getNicheAllTimeRewards.value;

      return { ...ownProps, loading, allTimeRewards };
    }
  });
