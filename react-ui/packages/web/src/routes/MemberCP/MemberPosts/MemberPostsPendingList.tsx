import * as React from 'react';
import { compose, withProps } from 'recompose';
import { withQueryParamPaginationController } from '../../../shared/containers/withPaginationController';
import { withPendingPosts, WithPendingPostsProps } from '@narrative/shared';
import { WebRoute } from '../../../shared/constants/routes';
import { MemberPostsList } from './MemberPostsList';
import { PAGE_PARAM } from '../../../shared/constants/constants';

export const MemberPostsPendingList = compose(
  withProps({isPendingPosts : true}),
  withQueryParamPaginationController<WithPendingPostsProps>(
    withPendingPosts,
    WebRoute.MemberManagePendingPosts,
    PAGE_PARAM
  ),
)(MemberPostsList) as React.ComponentClass<{}>;
