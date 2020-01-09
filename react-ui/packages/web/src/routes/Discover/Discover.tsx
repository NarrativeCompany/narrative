import * as React from 'react';
import { DiscoverMessages } from '../../shared/i18n/DiscoverMessages';
import { HomeWrapper } from '../Home/components/HomeWrapper';
import { branch, compose, renderComponent } from 'recompose';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../shared/containers/withExtractedCurrentUser';
import { Redirect, RouteComponentProps, withRouter } from 'react-router';
import { WebRoute } from '../../shared/constants/routes';
import { SEOMessages } from '../../shared/i18n/SEOMessages';
import { Loading } from '../../shared/components/Loading';
import { withNetworkWideContentStream } from '@narrative/shared';
import {
  ContentStream,
  WithContentStreamPropsFromQuery,
  withContentStreamPropsFromQuery
} from '../../shared/components/contentStream/ContentStream';
import { TrendingNichesSidebarItem } from '../../shared/components/sidebar/TrendingNichesSidebarItem';
import { NicheStatsSidebarItem } from '../../shared/components/sidebar/NicheStatsSidebarItem';
import { withContentStreamSorts, WithContentStreamSortsProps } from '../../shared/containers/withContentStreamSorts';
import { PillMenu } from '../../shared/components/navigation/pills/PillMenu';
import { PublicationsPromoSidebarItem } from '../../shared/components/sidebar/PublicationsPromoSidebarItem';

interface ParentProps {
  banner?: React.ReactNode;
}

type Props =
  ParentProps &
  WithExtractedCurrentUserProps &
  WithContentStreamPropsFromQuery &
  WithContentStreamSortsProps;

const DiscoverComponent: React.SFC<Props> = (props) => {
  const { contentStreamProps, banner, pillMenuProps } = props;

  const forGuest = !props.currentUser;

  return (
    <HomeWrapper
      title={DiscoverMessages.Title}
      seoTitle={forGuest ? SEOMessages.HomeTitle : undefined}
      seoDescription={forGuest ? SEOMessages.HomeAndRegisterDescription : undefined}
      omitSeoSuffix={forGuest}
      sidebarItems={
        <React.Fragment>
          <NicheStatsSidebarItem/>
          <PublicationsPromoSidebarItem/>
          <TrendingNichesSidebarItem/>
        </React.Fragment>
      }
      headerContent={<PillMenu {...pillMenuProps}/>}
    >
      <ContentStream
        {...contentStreamProps}
        banner={banner}
      />
    </HomeWrapper>
  );
};

// jw: this is a bit tricky, but because this component is used to fulfill Home (/) and Discover (/discover) depending
//     on the current users logged in status. Guests use home, while logged in users use discover
function deriveDefaultTo(props: WithExtractedCurrentUserProps): WebRoute {
  if (props.currentUser === undefined) {
    return WebRoute.Home;
  }

  return WebRoute.Discover;
}

// jw: with all of that business behind us, we can focus on constructing the Discover component now.
export const Discover = compose(
  withExtractedCurrentUser,
  branch((props: WithExtractedCurrentUserProps) =>
    !!props.currentUserLoading,
    renderComponent(() => <Loading />)
  ),
  withRouter,
  // jw: if we are rendering from the WebRoute.Discover, and we are a guest then we need to redirect to the expected
  //     WebRoute.Home, otherwise spiders might index content on both paths.
  branch((props: RouteComponentProps<{}> & WithExtractedCurrentUserProps) =>
    // jw: because of different sorts, we now need to support the parameterized disvoer route here
    props.currentUser === undefined && props.location.pathname === WebRoute.Discover,
    renderComponent(() => <Redirect to={WebRoute.Home}/>)
  ),
  // jw: Okay, first lets setup the sort options. This will pull the current sort out of the URL and set up our sort
  //     for consumption by the withNetworkWideContentStream below.
  withContentStreamSorts(deriveDefaultTo, WebRoute.DiscoverParameterized),
  withNetworkWideContentStream,
  withContentStreamPropsFromQuery()
)(DiscoverComponent) as React.ComponentClass<ParentProps>;

// jw: let's expose a default version for routing purposes on the WebRoute.Discover path.
export default Discover;
