import * as React from 'react';
import { compose } from 'recompose';
import { MemberPostsList } from './MemberPostsList';
import { withQueryParamPaginationController } from '../../../shared/containers/withPaginationController';
import { WebRoute } from '../../../shared/constants/routes';
import { withDraftPosts, WithDraftPostsProps } from '@narrative/shared';
import { PAGE_PARAM } from '../../../shared/constants/constants';

export const MemberPostsDraftsList = compose(
  withQueryParamPaginationController<WithDraftPostsProps>(
    withDraftPosts,
    WebRoute.MemberManagePosts,
    PAGE_PARAM
  ),
)(MemberPostsList) as React.ComponentClass<{}>;
