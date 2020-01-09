import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { NichePostLeaderboardQuery, NichePostLeaderboardQueryVariables, RewardLeaderboardPost } from '../../types';
import { nichePostLeaderboardQuery } from '../graphql/niche/nichePostLeaderboardQuery';
import { NichePeriodParentProps } from './nicheLeaderboardUtils';
import { LoadingProps } from '../../utils';

const queryName = 'nichePostLeaderboardData';

export interface WithExtractedNichePostLeaderboardProps extends LoadingProps {
  leaderboard: RewardLeaderboardPost[];
}

type WithNichePostLeaderboardProps = NamedProps<
  {[queryName]: GraphqlQueryControls & NichePostLeaderboardQuery},
  NichePeriodParentProps
>;

export const withNichePostLeaderboard =
  graphql<
    NichePeriodParentProps,
    NichePostLeaderboardQuery,
    NichePostLeaderboardQueryVariables,
    WithExtractedNichePostLeaderboardProps
   >(nichePostLeaderboardQuery, {
     name: queryName,
    options: ({nicheOid, month}: NichePeriodParentProps) => ({
      variables: {
        nicheOid,
        input: {
          month
        }
      }
    }),
    props: ({ nichePostLeaderboardData, ownProps }: WithNichePostLeaderboardProps) => {
      const { loading } = nichePostLeaderboardData;

      const leaderboard = nichePostLeaderboardData && nichePostLeaderboardData.getNichePostLeaderboard || [];

      return { ...ownProps, loading, leaderboard };
    }
  });
