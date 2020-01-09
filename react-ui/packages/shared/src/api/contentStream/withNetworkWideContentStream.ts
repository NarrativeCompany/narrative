import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { networkWideContentStreamQuery } from '../graphql/contentStream/networkWideContentStreamQuery';
import { NetworkWideContentStreamQuery } from '../../types';
import {
  ContentStreamFilters,
  createContentStreamPropsFromQueryResults,
  extractContentStreamFilters,
  WithContentStreamProps
} from './contentStreamUtils';
import { infiniteLoadingFixProps } from '../../utils';

const queryName = 'networkWideContentStreamData';

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & NetworkWideContentStreamQuery},
  ChildDataProps<ContentStreamFilters, NetworkWideContentStreamQuery>
>;

export const withNetworkWideContentStream =
  graphql<
    ContentStreamFilters,
    NetworkWideContentStreamQuery,
    {},
    WithContentStreamProps
  >(networkWideContentStreamQuery, {
    options: (props: ContentStreamFilters) => ({
      ...infiniteLoadingFixProps,
      variables: {
        filters: extractContentStreamFilters(props)
      }
    }),
    name: queryName,
    props: ({ networkWideContentStreamData }: WithProps) => {
      return createContentStreamPropsFromQueryResults('getNetworkWideContentStream', networkWideContentStreamData);
    }
  });
