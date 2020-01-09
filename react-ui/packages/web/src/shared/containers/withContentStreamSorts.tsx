import { ContentStreamSortOrder, getQueryArg, LoadMorePostsParams } from '@narrative/shared';
import {
  EnhancedContentStreamSortOrder,
  getContentStreamSortOrderFromRouteParamValue,
  getDefaultContentStreamSortOrder
} from '../enhancedEnums/contentStreamSortOrder';
import { compose, withProps } from 'recompose';
import { RouteComponentProps, withRouter } from 'react-router';
import { TabDetails, TabRoute, TabsControllerParentProps } from './withTabsController';
import { WebRoute } from '../constants/routes';
import { withPillMenuController, WithPillMenuControllerProps } from './withPillMenuController';

export const ContentStreamSortParam = 'sort';

export interface ContentStreamSortsParentProps {
  baseParameters?: {};
}

export type WithContentStreamSortsProps = WithPillMenuControllerProps;

// tslint:disable-next-line: no-any
function resolveTo(props: any, to: WebRoute | ((props: any) => WebRoute)): WebRoute {
  if (typeof to === 'function') {
    // tslint:disable-next-line: no-any
    const toResolver = to as ((props: any) => WebRoute);
    return toResolver(props);
  }
  return to;
}

// jw: note: we need to consume the `to` since that will change from call to call.
export function withContentStreamSorts(
  // jw: the base route used for the primary sort type
  // tslint:disable-next-line: no-any
  defaultTo: WebRoute | ((props: any) => WebRoute),
  // jw: this one is always required when using sorts.
  parameterizedTo: WebRoute,
  defaultSortOverride?: ContentStreamSortOrder
) {
  return compose(
    withRouter,
    withProps<
      TabsControllerParentProps,
      ContentStreamSortsParentProps & RouteComponentProps<{[ContentStreamSortParam]: string}>
    >((props) => {
      const defaultToResolved = resolveTo(props, defaultTo);

      const tabRouteParams = props.baseParameters;

      // jw: We always want to have a sort option selected, so let's default to the first one in the enhancers
      const sortOrder = getContentStreamSortOrderFromRouteParamValue(
        props.match.params[ContentStreamSortParam],
        defaultSortOverride
      );

      // jw: if we did not get a sortOrder, then that means that either the specified value does not correspond to a
      //     sortOrder, or they tried to specify the default directly. Either way, let's short out with no sortOrder
      const tabs: TabDetails[] = [];
      if (sortOrder) {
        // jw: let's derive the default sort order so that the the component above can just require it as part of its
        //     contract.
        const defaultSortOrder = getDefaultContentStreamSortOrder(defaultSortOverride);

        EnhancedContentStreamSortOrder.enhancers.forEach((order) => {

          const isDefault = defaultSortOrder === order.sortOrder;

          const tab = new TabDetails(
            isDefault
              ? new TabRoute(defaultToResolved)
              : new TabRoute(parameterizedTo, true, {[ContentStreamSortParam]: order.routeParamValue})
            ,
            order.titleMessage,
            undefined,
            undefined,
            order.sortIcon
          );

          // jw: we only want spiders to follow MOST_RECENT
          if (!order.isMostRecent()) {
            tab.notFollowed();
          }

          tabs.push(tab);
        });
      }

      // jw: in addition to the sort orders above, let's parse the LoadMorePostsParams if this is for MOST_RECENT
      let loadMoreParams: LoadMorePostsParams = {};
      if (sortOrder === ContentStreamSortOrder.MOST_RECENT) {
        const lastItemOid = getQueryArg(props.location.search, 'lastItemOid');
        const lastItemDatetime = getQueryArg(props.location.search, 'lastItemDatetime');

        loadMoreParams = { lastItemOid, lastItemDatetime };
      }

      return {
        ...loadMoreParams,
        tabRouteParams,
        tabs,
        // jw: let's also include the sortOrder so that it will be applied to the withXContentStream HOC auto-magically.
        sortOrder
      };
    }),
    withPillMenuController
  );
}
