import * as React from 'react';
import { List } from 'antd';
import { ListItemMetaProps, ListItemProps } from 'antd/lib/list';
import styled from '../../../shared/styled';
import { MemberPostsListItemInfo } from './MemberPostsListItemInfo';
import { Post } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { MemberPostsMessages } from '../../../shared/i18n/MemberPostsMessages';
import { PostAvatar } from '../../../shared/components/post/PostAvatar';
import { PostLink } from '../../../shared/components/post/PostLink';
import { WithOpenDeletePostConfirmationHandler } from '../../../shared/containers/withDeletePostController';
import { PostDropdown } from '../../../shared/components/post/PostDropdown';
import { WithPermissionLinkSecurer } from '../../../shared/containers/withPermissionsModalHelpers';

const ListItem = styled<ListItemProps>((props) => <List.Item {...props}/>)`
  @media screen and (max-width: 575px) {
    .ant-list-item-action {
      align-self: flex-start;
    }
  }
`;

const ListItemMeta = styled<ListItemMetaProps>(List.Item.Meta)`
  .ant-list-item-meta-avatar {
    width: 82px;
    height: 82px;
    
    img {
      width: 100%;
    }
  }

  .ant-list-item-meta-title {
    font-weight: bold;
  }
  
  @media screen and (max-width: 575px) {
    .ant-list-item-meta-avatar {
      display: none;
    }
  }
`;

export interface PostProps {
  post: Post;
}

interface ParentProps extends PostProps, WithOpenDeletePostConfirmationHandler, WithPermissionLinkSecurer {
  isPublishedPost: boolean;
  isPendingPost: boolean;
}

export const MemberPostListItem: React.SFC<ParentProps> = (props) => {
  const { post, isPublishedPost, isPendingPost, handleOpenDeletePostConfirmation, permissionLinkSecurer } = props;

  const description = (
    <React.Fragment>
      {post.subTitle}

      <MemberPostsListItemInfo
        post={post}
        isPublished={isPublishedPost}
        isPending={isPendingPost}
      />
    </React.Fragment>
  );

  // bl: Post.title is typed as required, but it might be empty for drafts. that's why we still have to check
  // to see if there is a title to display. if not, show <Untitled>.
  let title;
  if (post.title) {
    title = post.title;

  } else {
    title = (
      <React.Fragment>
        &lt;
        <FormattedMessage {...MemberPostsMessages.UntitledPlaceholder}/>
        &gt;
      </React.Fragment>
    );
  }

  // jw: never include the actions dropdown for the pending list.
  let actions: React.ReactNode[] | undefined;
  if (!isPendingPost) {
    actions = [(
      <PostDropdown
        key="list-dropdown"
        post={post}
        editableByCurrentUser={!isPublishedPost || !post.publishedToPublication}
        deletableByCurrentUser={!isPublishedPost || !post.publishedToPublication}
        handleOpenDeletePostConfirmation={handleOpenDeletePostConfirmation}
      />
    )];
  }

  return (
    <ListItem actions={actions}>
      <ListItemMeta
        avatar={<PostAvatar size={70} post={post} link={isPublishedPost} />}
        title={
          isPendingPost && !post.postLive
            ? title
            : (
              <PostLink
                post={post}
                isEditLink={!isPublishedPost && !post.postLive}
                linkSecurer={permissionLinkSecurer}
              >
                {title}
              </PostLink>
            )
        }
        description={description}
      />
    </ListItem>
  );
};
