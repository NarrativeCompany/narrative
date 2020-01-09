import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  ContentStreamFilters,
  ContentStreamSortOrder,
  Post,
  WithContentStreamProps,
  withNetworkWideContentStream,
  defaultContentStreamPostsPerPage,
} from '@narrative/shared';
import styled from '../../styled';
import { mediaQuery } from '../../styled/utils/mediaQuery';
import { FeaturedPostSidebarItem } from './FeaturedPostSidebarItem';
import { List } from 'antd';
import { generateSkeletonListProps, renderSkeleton } from '../../utils/loadingUtils';
import { WebRoute } from '../../constants/routes';
import { FormattedMessage } from 'react-intl';
import { SidebarMessages } from '../../i18n/SidebarMessages';
import { Link } from '../Link';
import { SectionHeader } from '../SectionHeader';
import { FlexContainer } from '../../styled/shared/containers';
import { CardSpacingContainer } from './TrendingNichesSidebarItem';

type Props =
  WithContentStreamProps &
  ContentStreamFilters;

const FeaturedPostSidebarItemsContainer = styled.div`
  ${mediaQuery.lg_up`
    .ant-card {
      background: transparent;
      border-color: transparent;
    }
  `};
  .ant-card-body {
    padding: 0;
  };
`;

const FeaturedPostSidebarItemsComponent: React.SFC<Props> = (props) => {
  const { contentStreamLoading, posts, count } = props;

  // zb: show 10 skeletons while we are loading
  if (contentStreamLoading) {
    return (
      <List {...generateSkeletonListProps(count || defaultContentStreamPostsPerPage, renderSkeleton)}/>
    );
  }

  return (
    <FeaturedPostSidebarItemsContainer>
      <CardSpacingContainer>
        {posts.map((post: Post) => <FeaturedPostSidebarItem key={post.oid} post={post}/>)}
        <FlexContainer justifyContent="flex-end">
          <Link to={WebRoute.Discover} style={{margin: '10px 0'}}>
            <FormattedMessage {...SidebarMessages.ViewMorePosts} />
          </Link>
        </FlexContainer>
      </CardSpacingContainer>
    </FeaturedPostSidebarItemsContainer>
  );
};

export const FeaturedPostSidebarItems = compose(
  withProps<ContentStreamFilters, {}>({
    sortOrder: ContentStreamSortOrder.FEATURED,
    forWidget: true,
    count: 10
  }),
  withNetworkWideContentStream
)(FeaturedPostSidebarItemsComponent) as React.ComponentClass<{}>;

export const FeaturedPostsSidebarItem: React.SFC<{}> = () => {
  return (
    <React.Fragment>
      <SectionHeader
        title={<FormattedMessage {...SidebarMessages.FeaturedPostsHeader} />}
      />
      <FeaturedPostSidebarItems/>
    </React.Fragment>
  );
};
