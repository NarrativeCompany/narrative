import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { kycPricingQuery } from '../graphql/kyc/kycPricingQuery';
import { KycPricing, KycPricingQuery } from '../../types';
import { LoadingProps } from '../../utils';

const queryName = 'kycPricingData';

export interface WithLoadedKycPricingProps extends LoadingProps {
    kycPricing: KycPricing;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & KycPricingQuery},
  WithLoadedKycPricingProps & ChildDataProps<{}, KycPricingQuery>
>;

export const withKycPricing = graphql<{}, KycPricingQuery, {}>(kycPricingQuery, {
  name: queryName,
  props: ({ kycPricingData, ownProps }: WithProps) => {
    const { loading, getKycPricing } = kycPricingData;

    return {
      ...ownProps,
      kycPricing: getKycPricing,
      loading
    };
  }
});
