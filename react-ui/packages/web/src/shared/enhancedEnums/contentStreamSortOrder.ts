import { FormattedMessage } from 'react-intl';
import { ContentStreamSortOrder } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { ContentStreamMessages } from '../i18n/ContentStreamMessages';
import { SvgComponent } from '../components/Icon';

/*
  MOST_RECENT,
  TRENDING,
  FEATURED,
  HIGHEST_RATED
 */

export const DEFAULT_CONTENT_STREAM_SORT_ORDER = ContentStreamSortOrder.FEATURED;

// jw: let's define the ContentStreamSortOrderHelper that will provide all the extra helper logic for
//     ContentStreamSortOrders
export class ContentStreamSortOrderHelper {

  // jw: there are a few places where we just want to know if a order is featured or not. Let's ease that.
  public static isFeaturedType(sortOrder?: ContentStreamSortOrder): boolean {
    return sortOrder !== undefined && EnhancedContentStreamSortOrder.get(sortOrder).isFeatured();
  }

  sortOrder: ContentStreamSortOrder;
  titleMessage: FormattedMessage.MessageDescriptor;
  routeParamValue: SortOrderRouteValue;
  sortIcon: SvgComponent;

  constructor(
    sortOrder: ContentStreamSortOrder,
    titleMessage: FormattedMessage.MessageDescriptor,
    routeParamValue: SortOrderRouteValue,
    sortIcon: SvgComponent
  ) {
    this.sortOrder = sortOrder;
    this.titleMessage = titleMessage;
    this.routeParamValue = routeParamValue;
    this.sortIcon = sortIcon;
  }

  isFeatured() {
    return this.sortOrder === ContentStreamSortOrder.FEATURED;
  }

  isMostRecent() {
    return this.sortOrder === ContentStreamSortOrder.MOST_RECENT;
  }
}

// jw: next: lets create the lookup of ContentStreamSortOrder to helper object

const contentStreamSortOrderHelpers: {[key: number]: ContentStreamSortOrderHelper} = [];
// jw: make sure to register these in the order you want them to display.
contentStreamSortOrderHelpers[ContentStreamSortOrder.FEATURED] = new ContentStreamSortOrderHelper(
  ContentStreamSortOrder.FEATURED,
  ContentStreamMessages.FeaturedTitle,
  'featured',
  'featured-post-sort'
);
contentStreamSortOrderHelpers[ContentStreamSortOrder.TRENDING] = new ContentStreamSortOrderHelper(
  ContentStreamSortOrder.TRENDING,
  ContentStreamMessages.TrendingTitle,
  'trending',
  'trending-post-sort'
);
contentStreamSortOrderHelpers[ContentStreamSortOrder.HIGHEST_RATED] = new ContentStreamSortOrderHelper(
  ContentStreamSortOrder.HIGHEST_RATED,
  ContentStreamMessages.HighestRatedTitle,
  'quality',
  'post-quality-sort'
);
contentStreamSortOrderHelpers[ContentStreamSortOrder.MOST_RECENT] = new ContentStreamSortOrderHelper(
  ContentStreamSortOrder.MOST_RECENT,
  ContentStreamMessages.MostRecentTitle,
  'recent',
  'recent-post-sort'
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedContentStreamSortOrder = new EnumEnhancer<ContentStreamSortOrder, ContentStreamSortOrderHelper>(
  contentStreamSortOrderHelpers
);

// jw: let's create a little helper object for looking up orders by route value

const SortOrdersByRouteValue = {
  // jw: this one should never really be used, but let's include it to keep the Helper signature clean.
  featured: ContentStreamSortOrder.FEATURED,
  trending: ContentStreamSortOrder.TRENDING,
  quality: ContentStreamSortOrder.HIGHEST_RATED,
  recent: ContentStreamSortOrder.MOST_RECENT,
};

type SortOrderRouteValue = keyof typeof SortOrdersByRouteValue;

export function getDefaultContentStreamSortOrder(defaultSortOverride?: ContentStreamSortOrder): ContentStreamSortOrder {
  return defaultSortOverride
    ? defaultSortOverride
    : DEFAULT_CONTENT_STREAM_SORT_ORDER;
}

// jw: let's make it easier to convert a routeParamValue into a sort order.
export function getContentStreamSortOrderFromRouteParamValue(
  routeParamValue?: string,
  defaultSortOverride?: ContentStreamSortOrder
): ContentStreamSortOrder | undefined {
  const defaultSort = getDefaultContentStreamSortOrder(defaultSortOverride);

  // jw: if no value was specified then return the default
  if (!routeParamValue) {
    return defaultSort;
  }

  // jw: since we have a value, parse the order it corresponds to.
  const sortOrder = SortOrdersByRouteValue[routeParamValue];

  // jw: do not allow Featured to be specified directly
  if (sortOrder && sortOrder !== defaultSort) {
    return sortOrder as ContentStreamSortOrder;
  }

  // jw: if it did not match, or it was the default, than that should be an error signified by undefined
  return undefined;
}
