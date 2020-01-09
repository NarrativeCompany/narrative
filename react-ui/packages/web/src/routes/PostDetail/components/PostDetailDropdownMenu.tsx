import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import { generatePath, RouteComponentProps, withRouter } from 'react-router';
import { resolvePreviousLocation } from '../../../shared/utils/routeUtils';
import { WebRoute } from '../../../shared/constants/routes';
import {
  withDeletePostController,
  WithDeletePostControllerProps,
  WithPostDeletedHandler
} from '../../../shared/containers/withDeletePostController';
import { WithDeletePostProps, WithPostByIdProps } from '@narrative/shared';
import { WithExtractedCurrentUserProps } from '../../../shared/containers/withExtractedCurrentUser';
import { MenuItemsDivider, PostDropdown } from '../../../shared/components/post/PostDropdown';
import styled from '../../../shared/styled';
import { mediaQuery } from '../../../shared/styled/utils/mediaQuery';
import { DeletePostConfirmation } from '../../Post/components/DeletePostConfirmation';
import {
  withDeletePostFromNicheController,
  WithDeletePostFromNicheControllerProps
} from '../../../shared/containers/withDeletePostFromNicheController';
import { DeletePostFromNicheConfirmation } from '../../../shared/components/post/DeletePostFromNicheConfirmation';
import {
  withDeleteForAupViolationController,
  WithDeleteForAupViolationControllerProps,
  WithDeleteObjectForAupViolationHandler,
  WithOnObjectDeletedForAupViolationHandler
} from '../../../shared/containers/withDeleteForAupViolationController';
import {
  DeleteForAupViolationConfirmation
} from '../../../shared/components/tribunal/DeleteForAupViolationConfirmation';
import { Menu } from 'antd';
import { FormattedMessage } from 'react-intl';
import { Link } from '../../../shared/components/Link';
import { PostDetailMessages } from '../../../shared/i18n/PostDetailMessages';
import {
  withFeaturePostController,
  WithFeaturePostControllerProps
} from '../../../shared/containers/withFeaturePostController';
import { FeaturePostModals } from '../../../shared/components/post/FeaturePostModals';
import {
  withRemovePostFromPublicationController,
  WithRemovePostFromPublicationControllerProps
} from '../../../shared/containers/withRemovePostFromPublicationController';
import { RemovePostFromPublicationModal } from '../../../shared/components/post/RemovePostFromPublicationModal';
import {
  withApprovePublicationPostController,
  WithApprovePublicationPostControllerProps
} from '../../../shared/containers/withApprovePublicationPostController';
import { ApprovePublicationPostModal } from '../../../shared/components/post/ApprovePublicationPostModal';

const DropdownContainer = styled.div`
  position: absolute;
  top: 12px;
  right: 0;

  ${mediaQuery.xs`
    position: relative;
    margin-bottom: 10px;
    right: 0;
  `}
`;

type Props =
  WithPostByIdProps &
  WithExtractedCurrentUserProps &
  WithDeletePostControllerProps &
  WithDeletePostFromNicheControllerProps &
  WithDeleteForAupViolationControllerProps &
  WithFeaturePostControllerProps &
  WithRemovePostFromPublicationControllerProps &
  WithApprovePublicationPostControllerProps;

const PostDetailDropdownMenuComponent: React.SFC<Props> = (props) => {
  const {
    currentUser,
    post,
    postDetail,
    postDetail: { editableByCurrentUser, deletableByCurrentUser },
    handleOpenDeletePostConfirmation,
    deletePostConfirmationProps,
    deletePostFromNicheMenuItems,
    deletePostFromNicheConfirmationProps,
    openDeleteForAupViolationConfirmation,
    deleteForAupViolationConfirmationProps,
    generateFeaturePostMenuItem,
    featurePostModalsProps,
    generateRemovePostFromPublicationMenuItem,
    removePostFromPublicationModalProps,
    generateApprovePublicationPostMenuItem,
    approvePublicationPostModalProps,
  } = props;

  // jw: if this person is not logged in, let's short out.
  if (!currentUser) {
    return null;
  }

  // jw: now, let's aggregate the extra menu items together.
  let extraMenuItems: React.ReactNode[] = [];

  // jw: before we get into the deletion menu items let's see if we can be a bit more positive and give the option to
  //     feature this post.
  const featureMenuItem = generateFeaturePostMenuItem && generateFeaturePostMenuItem(postDetail);
  if (featureMenuItem) {
    extraMenuItems.push(featureMenuItem);
  }

  const approvePublicationPostMenuItem = generateApprovePublicationPostMenuItem &&
    generateApprovePublicationPostMenuItem();
  if (approvePublicationPostMenuItem) {
    extraMenuItems.push(approvePublicationPostMenuItem);
  }

  const removePostFromPublicationMenuItem = generateRemovePostFromPublicationMenuItem &&
    generateRemovePostFromPublicationMenuItem();
  if (removePostFromPublicationMenuItem) {
    extraMenuItems.push(removePostFromPublicationMenuItem);
  }

  // jw: if the user can remove posts from niches, let's include those first.
  if (deletePostFromNicheMenuItems) {
    // jw: let's include a divider if there are already menu items in the list.
    if (extraMenuItems.length) {
      extraMenuItems.push(<MenuItemsDivider key="nicheDeletionDivider" />);
    }
    extraMenuItems = extraMenuItems.concat(deletePostFromNicheMenuItems);
  }

  // jw: first, can the user delete for aup reasons?
  if (openDeleteForAupViolationConfirmation) {
    // jw: let's include a divider if there are already menu items in the list.
    if (extraMenuItems.length) {
      extraMenuItems.push(<MenuItemsDivider key="aupDeletionDivider" />);
    }
    extraMenuItems.push(
      <Menu.Item key="deletePostForAupViolationMenuItem">
        <Link.Anchor onClick={() => openDeleteForAupViolationConfirmation(post.oid)}>
          <FormattedMessage {...PostDetailMessages.DeletePostForAupViolation}/>
        </Link.Anchor>
      </Menu.Item>
    );
  }

  // jw: if they can't edit or delete and we do not have extra menu items, then short out.
  if (!editableByCurrentUser && !deletableByCurrentUser && !extraMenuItems.length) {
    return null;
  }

  return (
    <React.Fragment>
      <DropdownContainer>
        <PostDropdown
          post={post}
          editableByCurrentUser={!!editableByCurrentUser}
          deletableByCurrentUser={!!deletableByCurrentUser}
          handleOpenDeletePostConfirmation={handleOpenDeletePostConfirmation}
          extraMenuItems={extraMenuItems}
        />
      </DropdownContainer>
      {deletePostConfirmationProps &&
        <DeletePostConfirmation {...deletePostConfirmationProps} useGenericDeleteText={true} />
      }
      {deletePostFromNicheConfirmationProps &&
        <DeletePostFromNicheConfirmation {...deletePostFromNicheConfirmationProps}/>
      }
      {deleteForAupViolationConfirmationProps &&
        <DeleteForAupViolationConfirmation
          {...deleteForAupViolationConfirmationProps}
          deleteButtonMessage={PostDetailMessages.DeletePost}
        />
      }
      {featurePostModalsProps && <FeaturePostModals {...featurePostModalsProps} />}
      {removePostFromPublicationModalProps &&
        <RemovePostFromPublicationModal {...removePostFromPublicationModalProps} />
      }
      {approvePublicationPostModalProps &&
        <ApprovePublicationPostModal {...approvePublicationPostModalProps} />
      }
    </React.Fragment>
  );
};

export const PostDetailDropdownMenu = compose(
  withRouter,
  withHandlers<RouteComponentProps<{}>, WithPostDeletedHandler>({
    // jw: we need to add a onPostDeleted function for withDeletePostController
    onPostDeleted: (props: RouteComponentProps<{}>) => () => {
      const { history } = props;

      // jw: when this post is deleted, we will want to take the user back to their previous location, if possible
      history.push(resolvePreviousLocation(props.location, WebRoute.Home, generatePath(WebRoute.PostDetails)));
    }
  }),
  withDeletePostController,
  withDeletePostFromNicheController,
  // jw: let's setup the AUP Deletion for tribunal members
  withProps<WithOnObjectDeletedForAupViolationHandler, WithPostDeletedHandler>((props) => {
    // jw: note: I am just going to use the same handler we use for standard post deletion for the tribunal
    const { onPostDeleted } = props;

    return { onObjectDeletedForAupViolation: onPostDeleted };
  }),
  withHandlers<WithDeletePostProps, WithDeleteObjectForAupViolationHandler>({
    // jw: we need to add a handleDeleteObjectForAupViolation function for withDeleteForAupViolationController
    handleDeleteObjectForAupViolation: (props) => async (postOid: string) => {
      const { deletePost } = props;

      // jw: easy stuff, just delete the post
      await deletePost(postOid);
    }
  }),
  withDeleteForAupViolationController(PostDetailMessages.Post),
  withFeaturePostController,
  withRemovePostFromPublicationController,
  withApprovePublicationPostController,
)(PostDetailDropdownMenuComponent) as React.ComponentClass<WithPostByIdProps>;
