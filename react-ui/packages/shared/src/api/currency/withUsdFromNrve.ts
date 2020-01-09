import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { usdFromNrveQuery } from '../graphql/currency/usdFromNrveQuery';
import { UsdFromNrveQuery } from '../../types';
import { LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithUsdFromNrveProps extends LoadingProps {
  usdAmount?: string;
}

const queryName = 'usdFromNrveData';

type Props = NamedProps<{[queryName]: GraphqlQueryControls & UsdFromNrveQuery}, {}>;

export const withUsdFromNrve =
  graphql<{}, UsdFromNrveQuery, {}, WithUsdFromNrveProps>(usdFromNrveQuery, {
    name: queryName,
    props: ({ usdFromNrveData }: Props): WithUsdFromNrveProps => {
      const { loading, getUsdFromNrve } = usdFromNrveData;
      const usdAmount = getUsdFromNrve && getUsdFromNrve.usdAmount || undefined;

      return { loading, usdAmount };
    }
  });
