import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { userNeoWalletQuery } from '../graphql/redemption/userNeoWalletQuery';
import { UserNeoWallet, UserNeoWalletQuery } from '../../types';
import { LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithUserNeoWalletProps extends LoadingProps {
  userNeoWallet: UserNeoWallet;
}

const queryName = 'userNeoWalletData';

type Props = NamedProps<{[queryName]: GraphqlQueryControls & UserNeoWalletQuery}, {}>;

export const withUserNeoWallet =
  graphql<{}, UserNeoWalletQuery, {}, WithUserNeoWalletProps>(userNeoWalletQuery, {
    name: queryName,
    props: ({ userNeoWalletData }: Props): WithUserNeoWalletProps => {
      const { loading } = userNeoWalletData;
      const userNeoWallet = userNeoWalletData.getUserNeoWallet;

      return { loading, userNeoWallet };
    }
  });
