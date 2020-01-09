import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import { withRouter } from 'react-router-dom';
import { Card } from '../../../shared/components/Card';
import { NotFound } from '../../../shared/components/NotFound';
import { viewWrapperPlaceholder, withLoadingPlaceholder } from '../../../shared/utils/withLoadingPlaceholder';
import { DetailsViewWrapper } from '../../../shared/components/DetailsViewWrapper';
import { withNicheDetail } from '@narrative/shared';
import { NicheDetailsContext, WithNicheDetailsContextProps } from './components/NicheDetailsContext';
import {
  withCardTabsController,
  WithCardTabsControllerProps
} from '../../../shared/containers/withCardTabsController';
import { WebRoute } from '../../../shared/constants/routes';
import * as Routes from '../../index';
import { EnhancedNicheStatus } from '../../../shared/enhancedEnums/nicheStatus';
import { NicheDetailsMessages } from '../../../shared/i18n/NicheDetailsMessages';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';
import { NicheSidebarItems } from './components/NicheSidebarItems';
import { TabDetails, TabRoute, TabSubRoute } from '../../../shared/containers/withTabsController';
import { convertUrlIdToIdForApi, getIdForUrl, IdRouteProps } from '../../../shared/utils/routeUtils';
import { getChannelUrl } from '../../../shared/utils/channelUtils';

interface WithProps {
  prettyUrlString: string;
  nicheId: string;
}

type Props =
  WithNicheDetailsContextProps &
  WithCardTabsControllerProps;

const NicheDetailsComponent: React.SFC<Props> = (props) => {
  const { nicheDetail, cardTabProps, narrowViewportMenu, selectedTabRoute } = props;

  const { niche } = nicheDetail;

  return (
    <NicheDetailsContext.Provider value={{nicheDetail}}>
      <DetailsViewWrapper sidebarItems={<NicheSidebarItems/>}>
        <div itemScope={true} itemType="http://schema.org/DefinedTerm">
          {/* jw: because we are not linking this, we need to include this link for scrappers */}
          <a itemProp="url" href={getChannelUrl(niche)} />

          <Card.Channel
            channel={niche}
            titleSize={2}
            includeStatus={true}
            link={false}
            titleItemProp="name"
            descriptionItemProp="description"
          />
        </div>

        <Card style={{marginTop: '20px', width: '100%'}} {...cardTabProps}>
          {narrowViewportMenu}
          {selectedTabRoute}
        </Card>
      </DetailsViewWrapper>
    </NicheDetailsContext.Provider>
  );
};

export default compose(
  // jw: first: let's pull the prettyUrlString out of the URL and place it so it can be used to query for details.
  withRouter,
  withProps((props: IdRouteProps) => {
    const nicheId = convertUrlIdToIdForApi(props);

    return { nicheId };
  }),
  branch((props: WithProps) => (!props.nicheId),
    renderComponent(() => <NotFound/>)
  ),
  // jw: now that we know we have a nicheId in place, let's fetch the details for the Niche
  withNicheDetail,
  withLoadingPlaceholder(viewWrapperPlaceholder()),
  branch<WithNicheDetailsContextProps>(props => !props.nicheDetail,
    renderComponent(() => <NotFound/>)
  ),

  // jw: the last thing we need is the currentUser details so that we know if the viewer is the owner
  withExtractedCurrentUser,

  // jw: now that the details have been loaded, we are free to setup the tabs for the niche details page.

  withProps((props: WithNicheDetailsContextProps & WithExtractedCurrentUserProps) => {
    const { currentUser, nicheDetail: { niche } } = props;
    const  { oid, status, prettyUrlString } = niche;

    const nicheStatus = EnhancedNicheStatus.get(status);

    let viewedByOwner = false;
    if (currentUser && nicheStatus.isActive() && niche.owner) {
      viewedByOwner = currentUser.oid === niche.owner.oid;
    }

    // jw: this will be the default route if the niche is not active, so lets prime it that way.
    let activityRoute = WebRoute.NicheDetails;

    const tabs: TabDetails[] = [];

    if (nicheStatus.isActive()) {
      // jw: since the niche is active, the activity will be presented on its own route.
      activityRoute = WebRoute.NicheActivity;

      tabs.push(new TabDetails(
        new TabRoute(WebRoute.NicheDetails),
        NicheDetailsMessages.Posts,
        Routes.NichePosts,
        [new TabSubRoute(WebRoute.NichePosts)]
      ));
    }

    // jw: now we can add the activity
    tabs.push(new TabDetails(
      new TabRoute(activityRoute),
      NicheDetailsMessages.Activity,
      Routes.NicheActivity
    ));

    // jw: we will always include the profile
    tabs.push(new TabDetails(
      new TabRoute(WebRoute.NicheProfile),
      NicheDetailsMessages.Profile,
      Routes.NicheProfile
    ));

    // bl: only show Rewards if the Niche is Active or Rejected (in which case it may have previously had rewards)
    if (nicheStatus.isActive() || nicheStatus.isRejected()) {
      tabs.push(new TabDetails(
        new TabRoute(WebRoute.NicheRewards),
        NicheDetailsMessages.Rewards,
        Routes.NicheRewards,
        [new TabSubRoute(WebRoute.NicheRewardsLeaderboard)]
      ));
    }

    // jw: if this is the owner, include the settings
    if (viewedByOwner) {
      tabs.push(new TabDetails(
        new TabRoute(WebRoute.NicheSettings),
        NicheDetailsMessages.Settings,
        Routes.NicheSettings
      ));
    }

    const id = getIdForUrl(prettyUrlString, oid);

    return {
      tabs,
      tabRouteParams: { id }
    };
  }),
  withCardTabsController,
)(NicheDetailsComponent) as React.ComponentClass<{}>;
