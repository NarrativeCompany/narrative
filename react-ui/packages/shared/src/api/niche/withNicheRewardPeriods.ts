import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { NicheRewardPeriodsQuery, NicheRewardPeriodsQueryVariables } from '../../types';
import { NichePeriodParentProps, NicheQueryParentProps } from './nicheLeaderboardUtils';
import { nicheRewardPeriodsQuery } from '../graphql/niche/nicheRewardPeriodsQuery';
import { WithExtractedRewardPeriodsProps } from '../rewards';

const queryName = 'nicheRewardPeriodsData';

type WithNicheRewardPeriodsProps = NamedProps<
  {[queryName]: GraphqlQueryControls & NicheRewardPeriodsQuery},
  NicheQueryParentProps
>;

export const withNicheRewardPeriods =
  graphql<
    NichePeriodParentProps,
    NicheRewardPeriodsQuery,
    NicheRewardPeriodsQueryVariables,
    WithExtractedRewardPeriodsProps
   >(nicheRewardPeriodsQuery, {
     name: queryName,
    options: ({nicheOid}: NichePeriodParentProps) => ({
      variables: {
        nicheOid
      }
    }),
    props: ({ nicheRewardPeriodsData, ownProps }: WithNicheRewardPeriodsProps) => {
      const { loading } = nicheRewardPeriodsData;

      const rewardPeriods = nicheRewardPeriodsData && nicheRewardPeriodsData.getNicheRewardPeriods || [];

      return { ...ownProps, loading, rewardPeriods };
    }
  });
