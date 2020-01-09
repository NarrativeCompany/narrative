import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import {
  RewardPeriodInput,
  RewardLeaderboardUser,
  NicheCreatorLeaderboardQuery,
  NicheCreatorLeaderboardQueryVariables
} from '../../types';
import { nicheCreatorLeaderboardQuery } from '../graphql/niche/nicheCreatorLeaderboardQuery';
import { LoadingProps } from '../../utils';

const queryName = 'nicheCreatorLeaderboardData';

interface NicheCreatorLeaderboardParentProps {
  nicheOid: string;
  input?: RewardPeriodInput;
}

export interface WithExtractedNicheCreatorLeaderboardProps extends LoadingProps {
  leaderboard: RewardLeaderboardUser[];
}

type WithNicheCreatorLeaderboardProps = NamedProps<
  {[queryName]: GraphqlQueryControls & NicheCreatorLeaderboardQuery},
  NicheCreatorLeaderboardParentProps
>;

export const withNicheCreatorLeaderboard =
  graphql<
    NicheCreatorLeaderboardParentProps,
    NicheCreatorLeaderboardQuery,
    NicheCreatorLeaderboardQueryVariables,
    WithExtractedNicheCreatorLeaderboardProps
   >(nicheCreatorLeaderboardQuery, {
     name: queryName,
    options: ({nicheOid, input}: NicheCreatorLeaderboardParentProps) => ({
      variables: {
        nicheOid,
        input
      }
    }),
    props: ({ nicheCreatorLeaderboardData, ownProps }: WithNicheCreatorLeaderboardProps) => {
      const { loading } = nicheCreatorLeaderboardData;

      const leaderboard = nicheCreatorLeaderboardData && nicheCreatorLeaderboardData.getNicheCreatorLeaderboard || [];

      return { ...ownProps, loading, leaderboard };
    }
  });
