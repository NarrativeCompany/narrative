import gql from 'graphql-tag';

export const AgeRatingFieldsFragment = gql`
  fragment AgeRatingFields on AgeRatingFields {
    totalVoteCount
    score
    ageRating
  }
`;
