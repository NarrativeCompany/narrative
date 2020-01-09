import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { nrveUsdPriceQuery } from '../graphql/currency/nrveUsdPriceQuery';
import { NrveUsdPrice, NrveUsdPriceQuery } from '../../types';
import { LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithNrveUsdPriceProps extends LoadingProps {
  nrveUsdPrice: NrveUsdPrice;
}

const queryName = 'nrveUsdPriceData';

type Props = NamedProps<{[queryName]: GraphqlQueryControls & NrveUsdPriceQuery}, {}>;

export const withNrveUsdPrice =
  graphql<{}, NrveUsdPriceQuery, {}, WithNrveUsdPriceProps>(nrveUsdPriceQuery, {
    name: queryName,
    props: ({ nrveUsdPriceData }: Props): WithNrveUsdPriceProps => {
      const { loading } = nrveUsdPriceData;
      const nrveUsdPrice = nrveUsdPriceData.getNrveUsdPrice;

      return { loading, nrveUsdPrice };
    }
  });
