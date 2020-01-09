import { defineMessages } from 'react-intl';

export const ContentStreamMessages = defineMessages({
  FeaturedTitle: {
    id: 'contentStreamSortOrder.featuredTitle',
    defaultMessage: 'Featured'
  },
  TrendingTitle: {
    id: 'contentStreamSortOrder.trendingTitle',
    defaultMessage: 'Trending'
  },
  HighestRatedTitle: {
    id: 'contentStreamSortOrder.highestRatedTitle',
    defaultMessage: 'Quality'
  },
  MostRecentTitle: {
    id: 'contentStreamSortOrder.mostRecentTitle',
    defaultMessage: 'Recent'
  },
  NoResultsMessage: {
    id: 'contentStream.notResultsMessage',
    defaultMessage: 'There is currently nothing to show in this content stream. Try a different filter above!'
  },
  InPublication: {
    id: 'postByline.inPublication',
    defaultMessage: 'in {publicationLink}'
  },
  LiveOnNetwork: {
    id: 'postByline.liveOnNetwork',
    defaultMessage: 'Live On Network'
  },
});
