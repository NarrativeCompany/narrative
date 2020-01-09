import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { nicheStatsQuery } from '../graphql/stats/nicheStatsQuery';
import { NicheStats, NicheStatsQuery } from '../../types';

const queryName = 'nicheStatsData';

export interface WithExtractedNicheStatsProps {
  loadingNicheStats: boolean;
  nicheStats: NicheStats;
}

type WithNicheStatsProps = NamedProps<{[queryName]: GraphqlQueryControls & NicheStatsQuery}, {}>;

export const withNicheStats =
  graphql<{}, NicheStatsQuery, {}, WithExtractedNicheStatsProps>(nicheStatsQuery, {
    name: queryName,
    props: ({ nicheStatsData }: WithNicheStatsProps) => {
      const loadingNicheStats = nicheStatsData.loading;
      const nicheStats = nicheStatsData.getNicheStats;

      return { loadingNicheStats, nicheStats };
    }
  });
