import * as React from 'react';
import { compose } from 'recompose';
import { PostDetailConnect, WithPostDetailContextProps } from './components/PostDetailContext';
import { getBaseUrl, Niche } from '@narrative/shared';
import { SEO } from '../../shared/components/SEO';
import { generatePath } from 'react-router';
import { WebRoute } from '../../shared/constants/routes';
import { getPostUrl } from '../../shared/utils/postUtils';
import { Post } from './components/Post';

const PostDetailBodyComponent: React.SFC<WithPostDetailContextProps> = (props) => {
  const { post, postDetail } = props.postByIdProps;
  const { prettyUrlString, author: { username } } = post;

  const tags: string[] = [];
  post.publishedToNiches.forEach((niche: Niche) => {
    tags.push(niche.name);
  });

  return (
    <React.Fragment>
      {/* jw: the definition for Post needing to have optional titles and descriptions for edits of drafts is a pain */}
      <SEO
        title={post.title}
        publication={post.publishedToPublication || undefined}
        author={post.author.displayName}
        authorUrl={getBaseUrl() + generatePath(WebRoute.UserProfile, { username })}
        url={getBaseUrl() + getPostUrl(prettyUrlString, post.oid)}
        canonicalUrl={postDetail.canonicalUrl || undefined}
        ogType="article"
        imageUrl={post.titleImageLargeUrl || undefined}
        imageWidth={post.titleImageLargeWidth || undefined}
        imageHeight={post.titleImageLargeHeight || undefined}
        description={post.subTitle || postDetail.extract || undefined}
        tags={tags}
        publishedTime={post.liveDatetime}
      />

      <Post {...props.postByIdProps}/>
    </React.Fragment>
  );
};

export const PostDetailBody = compose(
  PostDetailConnect,
)(PostDetailBodyComponent) as React.ComponentClass<{}>;

export default PostDetailBody;
