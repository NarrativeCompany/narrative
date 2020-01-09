import gql from 'graphql-tag';
import { ContentStreamEntriesFragment } from '../fragments/contentStreamEntriesFragment';

export const personalizedContentStreamQuery = gql`
  query PersonalizedContentStreamQuery ($filters: ContentStreamFiltersInput!) {
    getPersonalizedContentStream (filters: $filters)
    @rest(
      type: "ContentStreamEntries", 
      path: "/content-streams/current-user?{args.filters}"
    ) {
      ...ContentStreamEntries
    }
  }
  ${ContentStreamEntriesFragment}
`;
