import * as React from 'react';
import { branch, compose, renderComponent } from 'recompose';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';
import { NotFound } from '../../../shared/components/NotFound';
import { HomeWrapper } from './HomeWrapper';
import { HomeMessages } from '../../../shared/i18n/HomeMessages';
import { NicheStatsSidebarItem } from '../../../shared/components/sidebar/NicheStatsSidebarItem';
import { Loading } from '../../../shared/components/Loading';
import { ContentStreamSortOrder, withPersonalizedContentStream } from '@narrative/shared';
import {
  withContentStreamSorts,
  WithContentStreamSortsProps
} from '../../../shared/containers/withContentStreamSorts';
import { WebRoute } from '../../../shared/constants/routes';
import {
  ContentStream,
  WithContentStreamPropsFromQuery,
  withContentStreamPropsFromQuery
} from '../../../shared/components/contentStream/ContentStream';
import {
  FeaturedPostsSidebarItem
} from '../../../shared/components/sidebar/FeaturedPostSidebarItems';
import { GetCertifiedSidebarCard } from '../../../shared/components/sidebar/GetCertifiedSidebarCard';
import { PillMenu } from '../../../shared/components/navigation/pills/PillMenu';
import { OwnedNichesSidebarItem } from '../../../shared/components/sidebar/OwnedNichesSidebarItem';
import { PublicationsPromoSidebarItem } from '../../../shared/components/sidebar/PublicationsPromoSidebarItem';
import { YourPublicationsSidebarItem } from '../../../shared/components/sidebar/YourPublicationsSidebarItem';

type Props =
  WithContentStreamPropsFromQuery &
  WithContentStreamSortsProps;

const MyStreamComponent: React.SFC<Props> = (props) => {
  const { contentStreamProps, pillMenuProps } = props;

  return (
    <HomeWrapper
      title={HomeMessages.MyStreamTitle}
      description={HomeMessages.MyStreamDescription}
      sidebarItems={
        <React.Fragment>
          <OwnedNichesSidebarItem/>
          <YourPublicationsSidebarItem/>
          <FeaturedPostsSidebarItem/>
          <GetCertifiedSidebarCard/>
          <NicheStatsSidebarItem/>
          <PublicationsPromoSidebarItem/>
        </React.Fragment>
      }
      headerContent={<PillMenu {...pillMenuProps}/>}
    >
      <ContentStream {...contentStreamProps}/>
    </HomeWrapper>
  );
};

export const MyStream = compose(
  withExtractedCurrentUser,
  branch(
    (props: WithExtractedCurrentUserProps) => !!props.currentUserLoading,
    renderComponent(() => <Loading />)
  ),
  branch(
    (props: WithExtractedCurrentUserProps) => !props.currentUser,
    // todo:error-handling: This should never happen, so we should report this to the server
    renderComponent(() => <NotFound />)
  ),
  // jw: Okay, first lets setup the sort options. This will pull the current sort out of the URL and set up our sort
  //     for consumption by the withPersonalizedContentStream below.
  withContentStreamSorts(WebRoute.Home, WebRoute.HomeParameterized, ContentStreamSortOrder.MOST_RECENT),
  // jw: now that the sort option is setup, lets run the content stream query
  withPersonalizedContentStream,
  // jw: now, let's make those search results easy to consume.
  withContentStreamPropsFromQuery(HomeMessages.MyStreamNoResultsMessage)
)(MyStreamComponent) as React.ComponentClass<{}>;
