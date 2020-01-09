import gql from 'graphql-tag';
import { PermittedAgeRatingFragment } from '../fragments/permittedAgeRatingFragment';

export const currentUserPermittedAgeRatingQuery = gql`
  query CurrentUserPermittedAgeRatingQuery {
    getCurrentUserPermittedAgeRating @rest(type: "PermittedAgeRating", path: "/users/current/permitted-age-rating") {
      ...PermittedAgeRating
    }
  }
  ${PermittedAgeRatingFragment}  
`;
