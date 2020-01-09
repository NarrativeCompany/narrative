import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  CommentProps,
  WithToggleCommentEditFormHandler
} from './DisplayComment';
import styled from '../../styled';
import { DeleteCommentIcon } from './DeleteCommentIcon';
import { EditCommentIcon } from './EditCommentIcon';
import { WithPaginationCurrentPageProps } from '../../containers/withPaginationController';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from '../../containers/withExtractedCurrentUser';
import { WithOpenDeleteForAupViolationConfirmationHandler } from '../../containers/withDeleteForAupViolationController';
import { Link } from '../Link';
import { Icon } from 'antd';

// jw: let's ensure the icons maintain space away from whatever is to the left of them.
const PositioningContainer = styled.div`
  min-width: 40px;
  margin-left: 5px;
  text-align: right;
`;

export interface WithCurrentUserCommentDeleteProps {
  currentUserCanDeleteAllComments?: boolean;
}

type ParentProps =
  CommentProps &
  WithPaginationCurrentPageProps &
  WithOpenDeleteForAupViolationConfirmationHandler &
  WithToggleCommentEditFormHandler &
  WithCurrentUserCommentDeleteProps & {
  editFormShown?: boolean;
};

interface Props extends ParentProps {
  canEditComment?: boolean;
  canDeleteComment?: boolean;
}

const CommentAuthorControlsComponent: React.SFC<Props> = (props) => {
  const {
    canEditComment,
    canDeleteComment,
    editFormShown,
    toggleCommentEditForm,
    openDeleteForAupViolationConfirmation,
    ...deleteCommentIconProps
  } = props;

  // jw: if you can edit or delete this post then let's include the default icons.
  if (canEditComment || canDeleteComment) {
    return (
      <PositioningContainer>
        {canEditComment && !editFormShown && <EditCommentIcon toggleCommentEditForm={toggleCommentEditForm} />}
        {canDeleteComment && <DeleteCommentIcon {...deleteCommentIconProps} />}
      </PositioningContainer>
    );
  }

  // jw: if you can delete for AUP violation, then lets include the delete icon, but trigger that popup instead.
  if (openDeleteForAupViolationConfirmation) {
    return (
      <PositioningContainer>
        <Link.Anchor color="light" onClick={() => openDeleteForAupViolationConfirmation(props.comment.oid)}>
          <Icon type="delete" />
        </Link.Anchor>
      </PositioningContainer>
    );
  }

  return null;
};

export const CommentAuthorControls = compose(
  withExtractedCurrentUser,
  withProps<Pick<Props, 'canEditComment' | 'canDeleteComment'>, ParentProps & WithExtractedCurrentUserProps>
  ((props: ParentProps & WithExtractedCurrentUserProps):
    Pick<Props, 'canEditComment' | 'canDeleteComment'> => {
    const { canCommentOrRate, currentUser, comment, currentUserCanDeleteAllComments } = props;

    let canEditComment: boolean | undefined;
    let canDeleteComment: boolean | undefined;

    if (canCommentOrRate && currentUser && comment) {
      if (currentUser.oid === comment.user.oid) {
        canEditComment = true;
        canDeleteComment = true;
      } else if (currentUserCanDeleteAllComments) {
        canDeleteComment = true;
      }
    }

    return { canEditComment, canDeleteComment };
  }),
)(CommentAuthorControlsComponent) as React.ComponentClass<ParentProps>;
