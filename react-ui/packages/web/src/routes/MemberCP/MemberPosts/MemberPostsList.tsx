import * as React from 'react';
import { List } from 'antd';
import { MemberPostListItem } from './MemberPostsListItem';
import { Post, WithPostsProps, WithRefetchPostListHandler, } from '@narrative/shared';
import { WithPaginationControllerProps } from '../../../shared/containers/withPaginationController';
import { Paragraph } from '../../../shared/components/Paragraph';
import { FormattedMessage } from 'react-intl';
import { MemberPostsMessages } from '../../../shared/i18n/MemberPostsMessages';
import { compose, withProps } from 'recompose';
import { DeletePostConfirmation } from '../../Post/components/DeletePostConfirmation';
import {
  withDeletePostController,
  WithDeletePostControllerProps
} from '../../../shared/containers/withDeletePostController';
import {
  withPermissionsModalHelpers,
  WithPermissionsModalHelpersProps
} from '../../../shared/containers/withPermissionsModalHelpers';
import { PermissionErrorModal } from '../../../shared/components/PermissionErrorModal';
import { generateSkeletonListProps, renderSkeleton } from '../../../shared/utils/loadingUtils';
import { RevokeReasonMessages } from '../../../shared/i18n/RevokeReasonMessages';

type ParentProps =
  WithPostsProps &
  WithPaginationControllerProps &
  WithPermissionsModalHelpersProps &
  WithPaginationControllerProps & {
    isPublishedPosts?: boolean;
    isPendingPosts?: boolean;
  };

type Props =
  ParentProps &
  WithDeletePostControllerProps;

const MemberPostsListComponent: React.SFC<Props> = (props) => {
  const {
    loading,
    pageSize,
    posts,
    pagination,
    isPublishedPosts,
    isPendingPosts,
    handleOpenDeletePostConfirmation,
    deletePostConfirmationProps,
    permissionErrorModalProps,
    permissionLinkSecurer
  } = props;

  if (loading) {
    return (
      <List {...generateSkeletonListProps(pageSize, renderSkeleton)}/>
    );
  }

  if (!posts.length) {
    const noPostsMessage = isPublishedPosts
        ? MemberPostsMessages.NoPublishedPosts
        : isPendingPosts
          ? MemberPostsMessages.NoPendingPosts
          : MemberPostsMessages.NoDraftPosts;

    return (
      <Paragraph>
        <FormattedMessage {...noPostsMessage}/>
      </Paragraph>
    );
  }

  return (
    <React.Fragment>
      {deletePostConfirmationProps && <DeletePostConfirmation
        {...deletePostConfirmationProps}
        useGenericDeleteText={true}
      />}
      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps}/>}

      <List
        dataSource={posts}
        pagination={pagination}
        renderItem={(post: Post) => (
          <MemberPostListItem
            key={post.oid}
            post={post}
            isPublishedPost={!!isPublishedPosts}
            isPendingPost={!!isPendingPosts}
            handleOpenDeletePostConfirmation={handleOpenDeletePostConfirmation}
            permissionLinkSecurer={permissionLinkSecurer}
          />
        )}
      />
    </React.Fragment>
  );
};

export const MemberPostsList = compose(
  withProps((props: WithRefetchPostListHandler) => {
    // jw: we need to expose the refetchPostList function as onPostDeleted for the withDeletePostController.
    const onPostDeleted = props.refetchPostList;

    return { onPostDeleted };
  }),
  withDeletePostController,
  withPermissionsModalHelpers('postContent', RevokeReasonMessages.EditPosts)
)(MemberPostsListComponent) as React.ComponentClass<ParentProps>;
