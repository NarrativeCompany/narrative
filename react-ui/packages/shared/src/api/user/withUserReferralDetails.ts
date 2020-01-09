import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { userReferralDetailsQuery } from '../graphql/user/userReferralDetailsQuery';
import { UserReferralDetailsQuery, UserReferralDetailsQueryVariables } from '../../types';

interface ParentProps {
  userOid: string;
}

export type WithUserReferralDetailsProps =
  NamedProps<{userReferralDetailsData: GraphqlQueryControls & UserReferralDetailsQuery}, ParentProps>;

export const withUserReferralDetails =
  graphql<
    ParentProps,
    UserReferralDetailsQuery,
    UserReferralDetailsQueryVariables
  >(userReferralDetailsQuery, {
    options: ({userOid}) => ({
      variables: {
        input: {userOid}
      }
    }),
    name: 'userReferralDetailsData'
  });
