import * as React from 'react';
import { compose, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { MemberPostsMessages } from '../../../shared/i18n/MemberPostsMessages';
import { WebRoute } from '../../../shared/constants/routes';
import { MemberPostsPublishedList } from './MemberPostsPublishedList';
import { MemberPostsDraftsList } from './MemberPostsDraftsList';
import { MemberPostsPendingList } from './MemberPostsPendingList';
import { MemberProfileConnect } from '../../../shared/context/MemberProfileContext';
import { SEO } from '../../../shared/components/SEO';
import { MemberProfileHeaderText } from '../../MemberProfile/MemberProfile';
import { TabDetails, TabRoute, TabSubRoute } from '../../../shared/containers/withTabsController';
import { withPillMenuController, WithPillMenuControllerProps } from '../../../shared/containers/withPillMenuController';
import { PillMenu } from '../../../shared/components/navigation/pills/PillMenu';

const MemberPosts: React.SFC<WithPillMenuControllerProps> = (props) => {
  const { pillMenuProps, selectedTabRoute } = props;

  return (
    <React.Fragment>
      <SEO title={MemberPostsMessages.SEOTitle} />

      <MemberProfileHeaderText>
        <FormattedMessage {...MemberPostsMessages.PageHeaderDescription}/>
      </MemberProfileHeaderText>

      <PillMenu {...pillMenuProps} />

      {selectedTabRoute}
    </React.Fragment>
  );
};

export default compose(
  MemberProfileConnect,
  withProps(() => {
    const tabs: TabDetails[] = [
      new TabDetails(
        // jw: default to the route for draft posts
        new TabRoute(WebRoute.MemberManagePosts),
        MemberPostsMessages.DraftsTab,
        MemberPostsDraftsList,
        [new TabSubRoute(WebRoute.MemberManageDraftsPosts)]
      ),
      new TabDetails(
        new TabRoute(WebRoute.MemberManagePendingPosts),
        MemberPostsMessages.PendingTab,
        MemberPostsPendingList
      ),
      new TabDetails(
        new TabRoute(WebRoute.MemberManagePublishedPosts),
        MemberPostsMessages.PublishedTab,
        MemberPostsPublishedList
      ),
    ];

    return {
      tabs
    };
  }),
  withPillMenuController
)(MemberPosts) as React.ComponentClass<{}>;
