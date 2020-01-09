import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { nichesOfInterestQuery } from '../graphql/niche/nichesOfInterestQuery';
import { Niche, NichesOfInterestQuery } from '../../types';

const queryName = 'nichesOfInterestData';

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & NichesOfInterestQuery},
  WithNichesOfInterestProps
>;

export type WithNichesOfInterestProps =
  ChildDataProps<{}, NichesOfInterestQuery> & {
  nichesOfInterest: Niche[];
};

export const withNichesOfInterest =
  graphql<
    {},
    NichesOfInterestQuery,
    {},
    WithNichesOfInterestProps
    >(nichesOfInterestQuery, {
    name: queryName,
    props: ({ nichesOfInterestData, ownProps }: WithProps) => {
      const loading = nichesOfInterestData.loading;
      const nichesOfInterest = extractedNichesOfInterest(nichesOfInterestData) as Niche[];

      return { ...ownProps, loading, nichesOfInterest };
    }
  });

function extractedNichesOfInterest (data: GraphqlQueryControls & NichesOfInterestQuery) {
  return data && data.getNichesOfInterest || [];
}
