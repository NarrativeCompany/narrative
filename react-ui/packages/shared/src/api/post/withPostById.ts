import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { postByIdQuery } from '../graphql/post/postByIdQuery';
import { Niche, Post, PostByIdQuery, PostDetail, User } from '../../types';

const queryName = 'postByIdData';

interface ParentProps {
  postId?: string;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & PostByIdQuery},
  WithPostByIdProps
>;

export type WithPostByIdProps =
  ChildDataProps<ParentProps, PostByIdQuery> & {
  postDetailLoading: boolean;
  postDetail: PostDetail;
  post: Post;
  publishedToNiches: Niche[];
  author: User;
};

export const withPostById =
  graphql<
    ParentProps,
    PostByIdQuery,
    {},
    WithPostByIdProps
  >(postByIdQuery, {
    skip: ({ postId }: ParentProps) => !postId,
    options: ({ postId }: ParentProps) => ({
      variables: {
        postId
      }
    }),
    name: queryName,
    props: ({ postByIdData, ownProps }: WithProps) => {
      const postDetailLoading =
        postByIdData.loading;

      const postDetail =
        postByIdData &&
        postByIdData.getPostById;

      const post =
        postDetail &&
        postDetail.post;

      const publishedToNiches =
        getPublishedToNichesFromPost(post) as Niche[];

      const author =
        post &&
        post.author;

      return { ...ownProps, postDetailLoading, postDetail, post, publishedToNiches, author };
    }
  });

function getPublishedToNichesFromPost (post: Post) {
  return post && post.publishedToNiches || [];
}
