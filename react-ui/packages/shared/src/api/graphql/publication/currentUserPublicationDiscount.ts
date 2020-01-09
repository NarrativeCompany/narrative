import gql from 'graphql-tag';
import { PublicationDiscountFragment } from '../fragments/currentUserPublicationDiscountFragment';

export const getCurrentUserPublicationDiscountQuery = gql`
  query CurrentUserPublicationDiscountQuery {
    getCurrentUserPublicationDiscount @rest(type: "PublicationDiscount", path: "/publications/current-user/discount") {
      ...PublicationDiscount
    }
  }
  ${PublicationDiscountFragment}
`;
