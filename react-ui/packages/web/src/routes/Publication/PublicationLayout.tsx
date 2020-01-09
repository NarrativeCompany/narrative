import * as React from 'react';
import { branch, compose, lifecycle, renderComponent, withProps } from 'recompose';
import { matchPath, RouteComponentProps, withRouter } from 'react-router-dom';
import { NotFound } from '../../shared/components/NotFound';
import { DetailsViewWrapper } from '../../shared/components/DetailsViewWrapper';
import {
  WithPublicationDetailProps,
  withPublicationDetail,
  includeScript,
  WithPublicationDetailParentProps
} from '@narrative/shared';
import { WebRoute } from '../../shared/constants/routes';
import * as Routes from '../index';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../shared/containers/withExtractedCurrentUser';
import { TabDetails, TabRoute, TabSubRoute } from '../../shared/containers/withTabsController';
import { convertUrlIdToIdForApi, getIdForUrl, IdRouteProps } from '../../shared/utils/routeUtils';
import {
  PublicationDetailsContext,
  WithPublicationDetailsContextProps
} from './components/PublicationDetailsContext';
import { PublicationDetailsMessages } from '../../shared/i18n/PublicationDetailsMessages';
import { withTabBarController, WithTabBarControllerProps } from '../../shared/containers/withTabBarController';
import { PublicationTabBar } from './components/PublicationTabBar';
import { PublicationHeader } from './components/PublicationHeader';
import { LayoutBgConnect, LayoutBgStoreProps } from '../../shared/stores/LayoutBgStore';
import { getChannelUrl } from '../../shared/utils/channelUtils';
import { Location, UnregisterCallback } from 'history';
import { getPublicationSocialLinks } from '../../shared/enhancedEnums/publicationUrlType';
import { PublicationSocialLinks } from './components/PublicationSocialLinks';
import { viewWrapperPlaceholder, withLoadingPlaceholder } from '../../shared/utils/withLoadingPlaceholder';
import styled from 'styled-components';
import { getPublicationRoleBooleans } from '../../shared/utils/publicationRoleUtils';

let fathomUnlisten: UnregisterCallback | undefined;

const HeaderWrapper = styled.div`
  margin-bottom: 15px;
`;

interface ParentProps extends WithPublicationDetailParentProps {
  postId?: string;
}

type Props =
  WithPublicationDetailsContextProps &
  WithTabBarControllerProps;

const resetStylingOnUnmount = (): boolean => {
  const { pathname } = window.location;
  // jw: if the new location is for a publication,
  let match = matchPath<{}>(pathname, WebRoute.PublicationDetails);
  // jw: I could check to make sure that the publication id from the URL matches, but I don't care, either way the
  //     styling will be the same.
  if (match) {
    return false;
  }

  // jw: if we are directing to a Post, let's assume that the post will handle styling itself, so don't reset styling.
  match = matchPath<{}>(pathname, WebRoute.PostDetails);
  if (match) {
    return false;
  }

  // jw: guess we need to reset the styling back to default.
  return true;
};

const PublicationLayoutComponent: React.SFC<Props> = (props) => {
  const { publicationDetail, currentUserRoles, refetchPublicationDetail, tabBar, selectedTabRoute } = props;

  const { publication } = publicationDetail;
  const socialLinks = getPublicationSocialLinks(publicationDetail);

  return (
    <PublicationDetailsContext.Provider value={{publicationDetail, currentUserRoles, refetchPublicationDetail}}>
      <DetailsViewWrapper sidebarItems={null}>
        <div itemScope={true} itemType="https://schema.org/Blog">
          {/* jw: because we are not linking this, we need to include this link for scrappers */}
          <a itemProp="url" href={getChannelUrl(publication)} />

          <HeaderWrapper>
            <PublicationHeader publicationDetail={publicationDetail} />
          </HeaderWrapper>
        </div>

        <PublicationTabBar
          publication={publication}
          tabBar={tabBar}
          socialLinks={socialLinks}
        />

        {selectedTabRoute}

        <PublicationSocialLinks links={socialLinks} />
      </DetailsViewWrapper>
    </PublicationDetailsContext.Provider>
  );
};

export default compose(
  // jw: this is a bit tricky, so let me take a few to explain what is going on here. For the post details we will be
  //     rendered by the PostDetail routed component, and the publicationId will be specified directly, meaning we do
  //     not need to get it from the route.

  // jw: first: let's pull the prettyUrlString out of the URL and place it so it can be used to query for details.
  withRouter,
  withProps((props: IdRouteProps & WithPublicationDetailParentProps) => {
    // jw: in the event where a publication id is provided then let's not try to parse it from the URL.
    let { publicationId } = props;
    if (!publicationId) {
      publicationId = convertUrlIdToIdForApi(props);
    }

    return { publicationId };
  }),
  branch((props: WithPublicationDetailParentProps) => !props.publicationId,
    renderComponent(() => <NotFound/>)
  ),

  // jw: now that we know we have a publicationId in place, let's fetch the details for the Publication
  withPublicationDetail,
  withLoadingPlaceholder(viewWrapperPlaceholder()),
  branch((props: WithPublicationDetailProps) => !props.publicationDetail,
    renderComponent(() => <NotFound/>)
  ),

  // jw: now that we have a publication let's go ahead and setup the container to be properly styled
  LayoutBgConnect,
  lifecycle<LayoutBgStoreProps, {}>({
    // tslint:disable object-literal-shorthand
    componentWillMount: function () {
      this.props.changeLayout('white', 'gray', true);
    },
    componentWillUnmount: function () {
      // jw: reset the viewporet to default background colors
      if (resetStylingOnUnmount()) {
        this.props.changeLayout();
      }
    }
  }),

  // jw: the last thing we need is the currentUser details so that we know if the viewer is the owner
  withExtractedCurrentUser,

  // jw: now that the details have been loaded, we are free to setup the tabs for the page.

  withProps((props: WithPublicationDetailProps & WithExtractedCurrentUserProps & ParentProps) => {
    const { currentUser, publicationDetail, postId } = props;
    const { currentUserRoles, publication: { oid, prettyUrlString } } = publicationDetail;

    const roles = getPublicationRoleBooleans(publicationDetail, currentUser);

    let tabs: TabDetails[] = [];

    tabs.push(new TabDetails(
      new TabRoute(WebRoute.PublicationDetails),
      PublicationDetailsMessages.FrontPage,
      Routes.PublicationPosts
    ).setXsAntIconReplacement('home'));

    tabs.push(new TabDetails(
      // jw: the profile sits at the root of the about section for a publication.
      new TabRoute(WebRoute.PublicationAbout),
      PublicationDetailsMessages.About,
      // jw: this component will determine where to route the user to (profile/activity)
      Routes.PublicationAbout,
      [new TabSubRoute(WebRoute.PublicationActivity)]
    ).setXsAntIconReplacement('info-circle'));

    // jw: if the viewer is the owner, admin or editor then include the manage menu item!
    // jw: note: writers can now access power users so that they can remove themselves as writers. So we might as well
    //     just allow access if the user has ANY explicit roles defined.
    if (roles.owner || currentUserRoles.length) {
      tabs.push(new TabDetails(
        // jw: note: this route is not exact since there are many CP pages all under this route.
        new TabRoute(WebRoute.PublicationCP, false),
        PublicationDetailsMessages.Manage,
        Routes.PublicationCP
      ).setXsAntIconReplacement('setting'));
    }

    // jw: we have a number of hidden tabs which are only here for routing purposes so that we can expose features/pages
    //     that are not represented within the navigation.

    const hiddenTabs: TabDetails[] = [];

    hiddenTabs.push(new TabDetails(
      // jw: note: this route is not exact since there are many CP pages all under this route.
      new TabRoute(WebRoute.PublicationSearch),
      PublicationDetailsMessages.Search,
      Routes.PublicationSearch
    ));

    hiddenTabs.push(new TabDetails(
      // jw: by default the route is going to be resolved with the tabRouteParams defined below, which is the
      //     publication ID, and for this we need to make sure we use the postId if one is present.
      new TabRoute(WebRoute.PostDetails, true, {id: postId}),
      PublicationDetailsMessages.PublicationPost,
      Routes.PublicationPostDetail
    ));

    hiddenTabs.push(new TabDetails(
      new TabRoute(WebRoute.PublicationInvitation),
      PublicationDetailsMessages.PublicationInvitation,
      Routes.PublicationInvitation
    ));

    // jw: now that all hidden tabs are defined, let's flag them as hidden
    hiddenTabs.forEach(tab => tab.hidden = true);

    // jw: finally, add all of the hidden tabs
    tabs = tabs.concat(hiddenTabs);

    const id = getIdForUrl(prettyUrlString, oid);

    return {
      tabs,
      tabRouteParams: { id },
      currentUserRoles: roles,
      useNarrowTabs: true,
      showTabsOnNarrowViewport: true
    };
  }),
  withTabBarController,
  lifecycle<WithPublicationDetailProps & RouteComponentProps, {}>({
    componentDidMount() {
      const { publicationDetail: { fathomSiteId, publication: { prettyUrlString } }, history } = this.props;
      if (fathomSiteId) {
        const recordPageView = () => {
          // tslint:disable-next-line no-any
          const fathom = (window as any).fathom;
          fathom('set', 'siteId', fathomSiteId);
          fathom('trackPageview');
        };
        // bl: if fathom is already loaded, just record the page view
        // tslint:disable-next-line no-any
        if ((window as any).fathom) {
          recordPageView();
        } else {
          // bl: if fathom isn't loaded yet, then load it now and record a page view onload
          // tslint:disable no-any
          (window as any).fathom = function() {
              ((window as any).fathom.q = (window as any).fathom.q || []).push(arguments);
          };
          // tslint:enable no-any

          // bl: we can register the page view event now and wait for the script to load
          recordPageView();

          includeScript('//cdn.usefathom.com/tracker.js', undefined, (script: HTMLScriptElement) => {
            script.id = 'fathom-script';
          }, true);
        }
        fathomUnlisten = history.listen((location: Location) => {
          // bl: the listen event is triggered _before_ the component unmounts (where we'll deregister the listener).
          // so, to work around the issue we need to make sure the path matches the publication base route.
          const match = matchPath<{id: string}>(location.pathname, WebRoute.PublicationDetails);
          // bl: in addition, we have to make sure it's for the same publication! it's possible the new page could be
          // a different publication, so we have to make sure the publication hasn't changed.
          if (match && match.params.id === prettyUrlString) {
            recordPageView();
          }
        });
      }
    },
    componentWillUnmount() {
      // bl: deregister the listener once we unmount.
      // note that unmount happens when changing publications, as well.
      if (fathomUnlisten) {
        fathomUnlisten();
        fathomUnlisten = undefined;
      }
    }
  })
)(PublicationLayoutComponent) as React.ComponentClass<ParentProps>;
