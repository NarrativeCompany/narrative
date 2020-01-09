import gql from 'graphql-tag';
import { SearchResultFragment } from '../fragments/searchResultFragment';

export const searchQuery = gql`
  query SearchQuery ($input: SearchQueryInput) {
    getSearchResults (input: $input)
    @rest(type: "SearchPayload", path: "/search?{args.input}") {
      items {
        ...SearchResult
      }
      hasMoreItems
      lastResultIndex
    }
  }
  ${SearchResultFragment}
`;
