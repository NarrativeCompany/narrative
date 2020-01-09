import gql from 'graphql-tag';
import { UserDetailFragment } from '../fragments/userDetailFragment';

export const currentUserDetailQuery = gql`
  query CurrentUserDetailQuery {
    getCurrentUserDetail @rest(type: "UserDetail", path: "/users/current/detail") {
      ...UserDetail
    }
  }
  ${UserDetailFragment}
`;
