import * as React from 'react';
import { Post } from '@narrative/shared';
import { LinkStyleProps } from '../Link';
import { Link } from '../Link';
import { WebRoute } from '../../constants/routes';
import { generatePath } from 'react-router';
import { getPostUrl } from '../../utils/postUtils';

export interface PostLinkProps extends LinkStyleProps {
  post: Post;
  isEditLink?: boolean;
}

export const PostLink: React.SFC<PostLinkProps> = (props) => {
  const { post, isEditLink, children, ...linkProps } = props;
  const { prettyUrlString, title } = post;

  const postOid = post.oid;

  let to;

  if (isEditLink) {
    to = generatePath(WebRoute.Post, {postOid});
  } else {
    to = getPostUrl(prettyUrlString, postOid);
  }

  return (
    <Link
      {...linkProps}
      to={to}
    >
      {children ? children : title}
    </Link>
  );
};
