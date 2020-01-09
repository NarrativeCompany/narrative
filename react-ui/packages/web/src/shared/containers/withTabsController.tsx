import { WebRoute } from '../constants/routes';
import * as React from 'react';
import { generatePath, matchPath, Route, RouteComponentProps } from 'react-router';
import { FormattedMessage } from 'react-intl';
import { branch, compose, renderComponent, withProps } from 'recompose';
import { NotFound } from '../components/NotFound';
import { SvgComponent } from '../components/Icon';

export class TabRoute {
  path: WebRoute;
  exact: boolean;
  // jw: some tabs have custom tab specific route parameters
  extraRouteParams?: {};

  constructor(path: WebRoute, exact?: boolean, extraRouteParams?: {}) {
    this.path = path;
    // jw: since most of the time we want the matches to be exact, let's make that the default.
    this.exact = exact === undefined || exact;
    this.extraRouteParams = extraRouteParams;
  }
}

// tslint:disable-next-line: max-classes-per-file
export class TabSubRoute extends TabRoute {
  // jw: by default all route resolutions are compared to first make sure they match the route, and if they do then we
  //     check to make sure that the location.pathname starts with the primaryRoute. If a subroute is parallel to the
  //     primary (or below it) then we need to generate a path from the subroute, and do a prefix match from that
  //     instead of the primary.
  parallelToPrimary?: boolean;

  constructor(path: WebRoute, exact?: boolean, extraRouteParams?: {}, parallelToPrimary?: boolean) {
    super(path, exact, extraRouteParams);
    this.parallelToPrimary = parallelToPrimary;
  }
}

// jw: exporting this so that consumers can properly type their tabs property.
// tslint:disable-next-line: max-classes-per-file
export class TabDetails {
  // jw: all primary routes should have the same set of route parameters.
  primaryRoute: TabRoute;
  // jw: this route is used for cases where the secondary route has more parameters than the primary.
  secondaryRoutes?: TabSubRoute[];
  // jw: pretty obvious, what is the title that should be shown when rendering this tab?
  title: FormattedMessage.MessageDescriptor;

  // jw: this is what should be routed to if this tab is the selected tab.
  // jw: this corresponds to RouteProps.component, FYI.  Wish they exported a type for this
  // tslint:disable-next-line: no-any
  loadable?: React.ComponentType<RouteComponentProps<any>> | React.ComponentType<any>;

  icon?: SvgComponent;

  // jw: this property is a little funky. When specified the idea is to use an icon instead of text for the menu item
  //     but you will need to check the concret `withXMenu` consumer you intend to use with this, since only
  //     `withTabBarController` has been updated to work with this at this point.
  // jw: yet another time that ant is letting me down. There is no strong type we can use for this...
  xsAntIconReplacement?: string;

  noFollow?: boolean;
  hidden?: boolean;

  // jw: once this controller resolves the path, it will set it here
  path?: string;

  constructor(
    primaryRoute: TabRoute,
    title: FormattedMessage.MessageDescriptor,
    // tslint:disable-next-line: no-any
    loadable?: React.ComponentType<RouteComponentProps<any>> | React.ComponentType<any>,
    secondaryRoutes?: TabSubRoute[],
    icon?: SvgComponent
  ) {
    this.primaryRoute = primaryRoute;
    this.secondaryRoutes = secondaryRoutes;
    this.title = title;
    this.loadable = loadable;
    this.icon = icon;
  }

  setPath(path: string) {
    this.path = path;
  }

  notFollowed(): TabDetails {
    this.noFollow = true;

    return this;
  }

  setXsAntIconReplacement(iconReplacement: string): TabDetails {
    this.xsAntIconReplacement = iconReplacement;

    return this;
  }
}

export interface TabsControllerParentProps {
  tabs: TabDetails[];
  // jw: these are only meant to be processed against the primaryRoute if provided.
  tabRouteParams?: {};
}

export interface WithTabsControllerProps {
  activeTab: TabDetails;
  selectedTabRoute?: Route;
}

function resolveRoute(tab: TabDetails, tabRouteParams?: {}, routeOverride?: TabRoute): string {
  const route = routeOverride || tab.primaryRoute;

  if (tabRouteParams || route.extraRouteParams) {
    return generatePath(route.path, {...tabRouteParams, ...route.extraRouteParams});
  }

  return route.path;
}

function matchesRoute(pathname: string, route: TabRoute, path: string, expectParams: boolean): boolean {
  const match = matchPath(pathname, route);

  // jw: first, let's ensure that if we got a match that we were able to parse out any params.
  if (match !== null && (!expectParams || match.params !== undefined)) {
    // jw: if we matched and params were parsed, let's ensure that the path at least starts with the path we will be
    //     using. This is vital since we have tabs that all have the same parameterized path, so we need to make sure
    //     we only count the one that actually resolves to what is in the location.pathname.
    return pathname.startsWith(path);
  }

  return false;
}

export const visibleTabsFilter = (tab: TabDetails) => !tab.hidden;

export const withTabsController = compose(
  withProps((props: TabsControllerParentProps & RouteComponentProps<{}>) => {
    const { tabs, tabRouteParams, location: { pathname } } = props;

    // jw: strange, but perhaps the tabs are only optional based on environment and user... Let's just short out.
    if (!tabs || !tabs.length) {
      return null;
    }

    const expectParams = tabRouteParams !== undefined;
    // jw: the route lookup is still necessary because we need to make sure we resolve the loadable with the proper
    //     route, otherwise path parameters will not get pulled out properly.
    const routeLookup: {} = {};

    // jw: it's important to notice that the last tab that matches will be considered active.
    let activeTab: TabDetails | undefined;

    // jw: let's process each tab and
    tabs.forEach((tab: TabDetails) => {
      const path = resolveRoute(tab, tabRouteParams);
      tab.setPath(path);
      let tabRoute = tab.primaryRoute;

      // jw: We need to make sure we use the most exact match possible, so let's start with the secondary routes.
      const foundSecondary = tab.secondaryRoutes && tab.secondaryRoutes.some((secondaryRoute: TabSubRoute) => {
        // jw: if the secondary path is parallel to the primary we need to generate a new path from its route.
        const secondaryPath = secondaryRoute.parallelToPrimary
          ? resolveRoute(tab, tabRouteParams, secondaryRoute)
          : path;

        if (matchesRoute(pathname, secondaryRoute, secondaryPath, expectParams)) {
          activeTab = tab;
          tabRoute = secondaryRoute;

          return true;
        }

        return false;
      });

      // jw: if we did not find a secondary route that matches this path, let's see if the primary one does.
      if (!foundSecondary && matchesRoute(pathname, tab.primaryRoute, path, expectParams)) {
        activeTab = tab;
      }

      routeLookup[path] = tab.loadable ? <Route path={tabRoute.path} component={tab.loadable}/> : undefined;
    });

    // jw: if we never found an active path, then let's short out.
    if (!activeTab || !activeTab.path) {
      return null;
    }

    return {
      activeTab,
      selectedTabRoute: routeLookup[activeTab.path]
    };
  }),
  branch(({activeTab}: WithTabsControllerProps) => !activeTab,
    renderComponent(() => <NotFound/>)
  )
);
