import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { currentUserKycQuery } from '../graphql/user/currentUserKycQuery';
import { CurrentUserKycQuery, UserKyc } from '../../types';
import { LoadingProps } from '../../utils';

const queryName = 'currentUserKycData';

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & CurrentUserKycQuery},
  WithCurrentUserKycProps
>;

export interface WithCurrentUserKycProps extends ChildDataProps<{}, CurrentUserKycQuery>, LoadingProps {
    userKyc: UserKyc;
    refetchUserKyc: () => void;
}

export const withCurrentUserKyc =
  graphql<{}, CurrentUserKycQuery, {}>(currentUserKycQuery, {
    name: queryName,
    props: ({ currentUserKycData, ownProps }: WithProps) => {
      const { loading, getCurrentUserKyc, refetch } = currentUserKycData;

      return {
        ...ownProps,
        userKyc: getCurrentUserKyc,
        refetchUserKyc: refetch,
        loading
      };
    }
  });
