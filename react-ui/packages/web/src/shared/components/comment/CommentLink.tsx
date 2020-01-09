import * as React from 'react';
import { Comment } from '@narrative/shared';
import { LinkStyleProps } from '../Link';
import { Link } from '../Link';
import { createUrl } from '../../utils/routeUtils';
import * as H from 'history';

export const commentParam = 'comment';

export interface CommentLinkProps extends LinkStyleProps {
  toConsumer: H.LocationDescriptor;
  comment: Comment;
}

export const CommentLink: React.SFC<CommentLinkProps> = (props) => {
  const { toConsumer, comment, ...linkProps } = props;

  const commentOid = comment.oid;
  const to = createUrl(`${toConsumer}`, {[commentParam]: commentOid}, commentOid);

  return (
    <Link {...linkProps} to={to} />
  );
};
