import gql from 'graphql-tag';
import { ContentStreamEntriesFragment } from '../fragments/contentStreamEntriesFragment';

export const networkWideContentStreamQuery = gql`
  query NetworkWideContentStreamQuery ($filters: ContentStreamFiltersInput!) {
    getNetworkWideContentStream (filters: $filters)
    @rest(
      type: "ContentStreamEntries", 
      path: "/content-streams/network-wide?{args.filters}"
    ) {
      ...ContentStreamEntries
    }
  }
  ${ContentStreamEntriesFragment}
`;
