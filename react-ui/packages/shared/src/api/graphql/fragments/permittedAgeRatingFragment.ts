import gql from 'graphql-tag';

export const PermittedAgeRatingFragment = gql`
  fragment PermittedAgeRating on PermittedAgeRating {
    permittedAgeRatings
  }
`;
