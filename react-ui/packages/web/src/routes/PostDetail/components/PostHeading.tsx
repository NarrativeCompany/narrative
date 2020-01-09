import * as React from 'react';
import { Tag } from '../../../shared/components/Tag';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { Heading } from '../../../shared/components/Heading';
import { Text } from '../../../shared/components/Text';
import { NicheLink } from '../../../shared/components/niche/NicheLink';
import { MemberAvatar } from '../../../shared/components/user/MemberAvatar';
import { MemberLink } from '../../../shared/components/user/MemberLink';
import { LocalizedTime } from '../../../shared/components/LocalizedTime';
import { WithPostByIdProps } from '@narrative/shared';
import { postSubtitleStyles, postTitleStyles } from '../../../shared/styled/shared/post';
import styled from '../../../shared/styled';
import { mediaQuery } from '../../../shared/styled/utils/mediaQuery';
import { PostDetailDropdownMenu } from './PostDetailDropdownMenu';
import { PostTitle } from '../../../shared/components/post/PostTitle';
import { getChannelUrl } from '../../../shared/utils/channelUtils';
import { createNicheFilteredContentStreamUrl } from '../../../shared/utils/publicationUtils';

const PostHeadingWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 20px;
  
  ${mediaQuery.xs`
    align-items: center;
  `}
`;

const PostHeaderAndMenuWrapper = styled<FlexContainerProps>(FlexContainer)`
  position: relative;
  
  ${mediaQuery.xs`
    align-items: center;
  `}
`;

const PostTitleWrapper = styled<FlexContainerProps>(FlexContainer)`
  h1 {
    ${postTitleStyles};
    margin-bottom: 8px;
  }
  
  h3 {
    ${postSubtitleStyles};
    margin-bottom: 15px;
  }
  
  ${mediaQuery.xs`
    h1, h3 {
      text-align: center;
    }
  `}
`;

const PostTagsWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 5px;

  .ant-tag {
    margin: 0 15px 5px 0;
    
    &:last-child {
      margin-right: 0;
    }
  }
  
  ${mediaQuery.xs`
    flex-direction: column;
    justify-content: center;
    
    .ant-tag {
      margin-right: 0;
    }
  `}
`;

export const PostHeading: React.SFC<WithPostByIdProps> = (props) => {
  const { post, author, publishedToNiches } = props;

  let nicheLinkBase: string | undefined;
  if (post.publishedToPublication) {
    nicheLinkBase = getChannelUrl(post.publishedToPublication);
  }

  return (
    <PostHeadingWrapper column={true}>
      <PostTitleWrapper column={true}>
        {publishedToNiches.length > 0  &&
        <PostTagsWrapper flexWrap="wrap">
          {publishedToNiches.map(niche =>
            <Tag size="normal" key={niche.oid}>
              <NicheLink
                niche={niche}
                linkPath={
                  nicheLinkBase
                    ? createNicheFilteredContentStreamUrl(nicheLinkBase, niche.oid)
                    : undefined
                }
                itemProp="articleSection"
              />
            </Tag>
          )}
        </PostTagsWrapper>}

        <PostHeaderAndMenuWrapper column={true} justifyContent="flex-start">
          <PostDetailDropdownMenu {...props} />
          <PostTitle
            post={post}
            size={1}
            isLink={false}
            // jw: we never want to include the quality icon for posts published to a publication
            forPublicationDisplay={!!post.publishedToPublication}
            // bl: don't allow the title to overlap with the ellipsis dropdown menu (25px wide + 20px margin)
            style={{marginBottom: 8, maxWidth: 'calc(100% - 45px)'}}
          />
        </PostHeaderAndMenuWrapper>

        <Heading size={3} color="light" weight={400}>
          {post.subTitle}
        </Heading>
      </PostTitleWrapper>

      <FlexContainer alignItems="center">
        <MemberAvatar user={author} size="small" style={{ marginRight: 10 }}/>

        <div>
          <MemberLink user={author} color="dark" itemProp="creator" />
          <Text size="small" color="light">
            <span style={{ marginRight: 5 }}>•</span>
            <LocalizedTime time={post.liveDatetime} dateOnly={true} itemProp="datePublished" />
          </Text>
        </div>
      </FlexContainer>
    </PostHeadingWrapper>
  );
};
