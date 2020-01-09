import gql from 'graphql-tag';

export const ContentStreamScrollParamsFragment = gql`
  fragment ContentStreamScrollParams on ContentStreamScrollParams {
    lastItemDatetime
    trendingBuildTime
    lastItemTrendingScore
    lastItemQualityScore
    lastItemSecondaryQualityValue
    lastItemOid
    nextItemOids
  }
`;
