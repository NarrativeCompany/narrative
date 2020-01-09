import * as React from 'react';
import { PostAuthor } from './PostAuthor';
import { PostNiches } from './PostNiches';
import { WithPostByIdProps } from '@narrative/shared';
import { PostRewards } from './PostRewards';

export const PostSidebar: React.SFC<WithPostByIdProps> = (props) => {
  const { author, publishedToNiches, post } = props;
  const postOid = post.oid;

  return (
    <React.Fragment>
      <PostAuthor author={author}/>

      {publishedToNiches.length > 0 &&
      <PostNiches niches={publishedToNiches}/>}
      <PostRewards postOid={postOid}/>
    </React.Fragment>
  );
};
