import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { NeoWallet, NeoWalletsQuery } from '../../types';
import { LoadingProps } from '../../utils';
import { neoWalletsQuery } from '../graphql/neowallet/neoWalletsQuery';

const queryName = 'neoWalletsData';

export interface WithNeoWalletsProps extends LoadingProps {
  neoWallets: NeoWallet[];
}

type Props = NamedProps<{[queryName]: GraphqlQueryControls & NeoWalletsQuery}, {}>;

export const withNeoWallets =
  graphql<{}, NeoWalletsQuery, {}, WithNeoWalletsProps>(neoWalletsQuery, {
    name: queryName,
    props: ({ neoWalletsData }: Props): WithNeoWalletsProps => {
      const { loading } = neoWalletsData;
      const neoWallets = neoWalletsData.getNeoWallets;

      return { loading, neoWallets };
    }
  });
