import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { userDetailQuery } from '../graphql/user/userDetailQuery';
import { UserDetailQuery, UserDetailQueryVariables } from '../../types';
import { infiniteLoadingFixProps } from '../../utils';

interface ParentProps {
  userId: string;
}

export type WithUserDetailProps =
  NamedProps<{userDetailData: GraphqlQueryControls & UserDetailQuery}, ParentProps>;

export const withUserDetail =
  graphql<
    ParentProps,
    UserDetailQuery,
    UserDetailQueryVariables
  >(userDetailQuery, {
    options: ({userId}) => ({
      // jw: we started getting the infinite loading issue when we added a redirect to the `MemberProfile` component
      //     when we detect that the URL username has a different case than the user who it resolved to.
      ...infiniteLoadingFixProps,
      variables: {
        userId
      }
    }),
    name: 'userDetailData'
  });
