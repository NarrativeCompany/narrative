import gql from 'graphql-tag';

export const QualityRatingFieldsFragment = gql`
  fragment QualityRatingFields on QualityRatingFields {
    totalVoteCount
    score
    qualityLevel
  }
`;
