import * as React from 'react';
import { WithToggleCommentEditFormHandler } from './DisplayComment';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import { Icon } from 'antd';
import { Link } from '../Link';
import { CommentMessages } from '../../i18n/CommentMessages';
import { compose } from 'recompose';

// jw: Note: This component assumes that the caller has done the security check for the currentUsers ability to edit.

type Props = WithToggleCommentEditFormHandler &
  InjectedIntlProps;

export const EditCommentIconComponent: React.SFC<Props> = (props) => {
  const { toggleCommentEditForm, intl: { formatMessage } } = props;

  return (
    <Link.Anchor
      color="light"
      style={{marginRight: '10px'}}
      onClick={() => toggleCommentEditForm(true)}
    >
      <Icon type="edit" title={formatMessage(CommentMessages.EditThisComment)} />
    </Link.Anchor>
  );
};

export const EditCommentIcon = compose(
  injectIntl
)(EditCommentIconComponent) as React.ComponentClass<WithToggleCommentEditFormHandler>;
