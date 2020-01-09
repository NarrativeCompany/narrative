import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import {
  withDeleteComment,
  WithDeleteCommentProps,
} from '@narrative/shared';
import { CommentProps } from './DisplayComment';
import { showValidationErrorDialogIfNecessary } from '../../utils/webErrorUtils';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import { Icon, Popconfirm } from 'antd';
import { Link } from '../Link';
import { CommentMessages } from '../../i18n/CommentMessages';
import { WithPaginationCurrentPageProps } from '../../containers/withPaginationController';

// jw: Note: This component assumes that the caller has done the security check for the currentUsers ability to edit.

interface WithHandlers {
  handleDeleteComment: () => void;
}

type ParentProps =
  CommentProps &
  WithPaginationCurrentPageProps;

const DeleteCommentIconComponent: React.SFC<WithHandlers> = (props) => {
  const { handleDeleteComment } = props;

  return (
    <Popconfirm
      title={<FormattedMessage {...CommentMessages.DeleteCommentConfirmation} />}
      icon={null}
      okText={<FormattedMessage {...CommentMessages.DeleteCommentYesText} />}
      cancelText={<FormattedMessage {...CommentMessages.DeleteCommentNoText} />}
      onConfirm={handleDeleteComment}
      placement="bottomRight"
    >
      <Link.Anchor color="light"><Icon type="delete" /></Link.Anchor>
    </Popconfirm>
  );
};

type HandleDeleteCommentProps =
  ParentProps &
  WithDeleteCommentProps &
  InjectedIntlProps;

export const DeleteCommentIcon = compose(
  withDeleteComment,
  injectIntl,
  withHandlers({
    handleDeleteComment: (props: HandleDeleteCommentProps) => async () => {
      const { consumerType, consumerOid, comment, deleteComment } = props;

      try {
        await deleteComment({consumerType, consumerOid, commentOid: comment.oid});
      } catch (err) {
        showValidationErrorDialogIfNecessary(props.intl.formatMessage(SharedComponentMessages.FormErrorTitle), err);
      }
    }
  })
)(DeleteCommentIconComponent) as React.ComponentClass<ParentProps>;
