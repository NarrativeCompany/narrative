import gql from 'graphql-tag';
import { ContentStreamEntriesFragment } from '../fragments/contentStreamEntriesFragment';

export const channelContentStreamQuery = gql`
  query ChannelContentStreamQuery (
    $channel: ChannelInput!, 
    $nicheFilters: ContentStreamNicheFiltersInput, 
    $filters: ContentStreamFiltersInput!
  ) {
    getChannelContentStream (channel: $channel, nicheFilters: $nicheFilters, filters: $filters)
    
    @rest(
      type: "ContentStreamEntries", 
      path: "/content-streams/{args.channel.type}/{args.channel.oid}?{args.filters}&{args.nicheFilters}"
    ) {
      ...ContentStreamEntries
    }
  }
  ${ContentStreamEntriesFragment}
`;
