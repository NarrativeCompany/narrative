import * as React from 'react';
import { compose, withProps } from 'recompose';
import { MemberPostsList } from './MemberPostsList';
import {  withQueryParamPaginationController } from '../../../shared/containers/withPaginationController';
import { WebRoute } from '../../../shared/constants/routes';
import { withPublishedPosts, WithPublishedPostsProps } from '@narrative/shared';
import { PAGE_PARAM } from '../../../shared/constants/constants';

export const MemberPostsPublishedList = compose(
  withProps({isPublishedPosts : true}),
  withQueryParamPaginationController<WithPublishedPostsProps>(
    withPublishedPosts,
    WebRoute.MemberManagePublishedPosts,
    PAGE_PARAM
  ),
)(MemberPostsList) as React.ComponentClass<{}>;
