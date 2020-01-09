import * as React from 'react';
import { Post } from '@narrative/shared';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import styled from '../../styled';
import { mediaQuery } from '../../styled/utils/mediaQuery';
import { ContentStreamItem, ContentStreamItemStyleProps } from './ContentStreamItem';

const FeaturedPostsRow = styled<FlexContainerProps>(FlexContainer)`
  ${mediaQuery.lg_up`
    & > .ant-card:not(:first-child) {
      margin-left: 20px;
    }
    & > .ant-card {
      width: calc(50% - 10px);
    }
    & > .ant-card > .ant-card-body {
      height: 100%;
    }
  `}

  ${mediaQuery.md_down`
    flex-direction: column;
  `}
`;

export interface FeaturedContentStreamRowPosts extends ContentStreamItemStyleProps {
  post1: Post;
  post2: Post;
}

export const FeaturedContentStreamRow: React.SFC<FeaturedContentStreamRowPosts> = (props) => {
  const { post1, post2, ...itemStyleProps } = props;

  if (!post1 || !post2) {
    // todo:error-handling: We should never be called without any posts, so what happened?
    return null;
  }

  return (
    <FeaturedPostsRow>
      <ContentStreamItem isFeatured={true} post={post1} {...itemStyleProps} />
      <ContentStreamItem isFeatured={true} post={post2} {...itemStyleProps} />
    </FeaturedPostsRow>
  );
};
