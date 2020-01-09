import * as React from 'react';
import { Niche, Post } from '@narrative/shared';
import { Card, CardProps } from '../Card';
import styled, { css } from '../../styled';
import { PostLink } from '../post/PostLink';
import { PostAvatar } from '../post/PostAvatar';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { Tag } from '../Tag';
import { NicheLink } from '../niche/NicheLink';
import { Paragraph, ParagraphProps } from '../Paragraph';
import { TitleImage } from '../TitleImage';
import { mediaQuery } from '../../styled/utils/mediaQuery';
import {
  postSubtitleBaseStyles
} from '../../styled/shared/post';
import { PostByline } from '../post/PostByline';
import { PostTitle } from '../post/PostTitle';
import { getChannelUrl } from '../../utils/channelUtils';
import { createNicheFilteredContentStreamUrl } from '../../utils/publicationUtils';

const cardPadding = 12;

interface IsFeaturedProps {
  isFeatured?: boolean;
}

export interface ContentStreamItemStyleProps {
  forPublicationDisplay?: boolean;
  forPublicationReview?: boolean;
}

type PostCardProps =
  IsFeaturedProps &
  CardProps;

const PostCard = styled<PostCardProps>(({isFeatured, ...rest}) => <Card {...rest}/>)`
  width: 100%;

  &.ant-card {
    margin-bottom: 20px;
    
    .ant-card-body {
      padding: ${cardPadding}px;
      
      .header-avatar.ant-avatar {
        width: 100% !important;
        height: 80% !important;
        border-radius: 0;
      }
    }
  }
  
  ${p => !p.isFeatured && css`
    .title-image {
      ${mediaQuery.hide_lg_up}
    }
    .right-post-avatar {
      margin-left: 10px;
      ${mediaQuery.hide_md_down}
    }
    ${mediaQuery.lg_up`
      &.ant-card .ant-card-body {
        padding: 0;
      }
    `}
  `}
`;

const PostDetails = styled<NichesContainerProps>(({isFeatured, ...rest}) => <FlexContainer {...rest}/>)`
  // float the niche container to the bottom so it is always positioned at the bottom of the card
  ${p => p.isFeatured && mediaQuery.md_up`
    justify-content: space-between;
  `};
  
  width: 100%;
  
  // subtract the 150px right avatar width plus the 10px of left margin between the post details and the avatar.
  ${p => !p.isFeatured && mediaQuery.lg_up`
    width: calc(100% - 160px);
  `}
`;

const PostSubTitle = styled<ParagraphProps>(Paragraph)`
  ${postSubtitleBaseStyles};
  font-size: 18px;
  line-height: 24px;
`;

type NichesContainerProps =
  FlexContainerProps &
  IsFeaturedProps;

export const ChannelsContainer = styled<NichesContainerProps>(({isFeatured, ...rest}) => <FlexContainer {...rest}/>)`
  margin-top: 10px;
  
  .ant-tag:not(:first-child) {
    margin-left: 7px;
  }
  
  ${mediaQuery.xs`
    margin-top: 0;
    flex-direction: column;
    
    .ant-tag:not(:first-child) {
      margin-left: 0;
    }
    
    .ant-tag {
      margin-top: 10px;
    }
  `};
`;

export interface PostProps {
  post: Post;
}

type Props =
  PostProps &
  ContentStreamItemStyleProps &
  IsFeaturedProps;

export const ContentStreamItem: React.SFC<Props> = (props) => {
  const { post, isFeatured, forPublicationDisplay, forPublicationReview} = props;

  const niches = post.publishedToNiches || [];

  const titleImage = post.titleImageUrl;

  const channels: React.ReactNode[] = [];

  if (niches.length > 0) {
    let nicheLinkBase: string | undefined;
    if (forPublicationDisplay && post.publishedToPublication) {
      nicheLinkBase = getChannelUrl(post.publishedToPublication);
    }
    niches.forEach((niche: Niche) => {
      channels.push(
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
      );
    });
  }

  return (
    <PostCard isFeatured={isFeatured} noBoxShadow={true} className={niches.length > 0 ? 'has-niches' : undefined}>
      <article itemScope={true} itemProp="https://schema.org/Article">
        <FlexContainer style={{height: '100%'}}>
          <PostDetails column={true} isFeatured={isFeatured}>
            {/* wrap the main post details in a div so that the flex layout allows the NichesContainer
                to float to the bottom via the justify-content: space-between; style. */}
            <div>
              {titleImage &&
                <PostLink post={post} style={{display: 'block'}} target={forPublicationReview ? '_blank' : undefined}>
                  <TitleImage className="title-image" imageUrl={titleImage} heightRatio={60}/>
                </PostLink>
              }

              <PostTitle
                post={post}
                size={isFeatured ? 2 : 3}
                forPublicationDisplay={forPublicationDisplay}
                forPublicationReview={forPublicationReview}
                style={{marginBottom: '0.3em'}}
              />

              {post.subTitle && <PostSubTitle>{post.subTitle}</PostSubTitle>}

              <PostByline
                post={post}
                forPublicationDisplay={forPublicationDisplay}
                forPublicationReview={forPublicationReview}
              />
            </div>

            {channels.length > 0 &&
              <ChannelsContainer isFeatured={isFeatured}>
                {channels.map((channel, index) =>
                  <React.Fragment key={index}>{channel}</React.Fragment>
                )}
              </ChannelsContainer>
            }
          </PostDetails>
          {!isFeatured && <PostAvatar post={post} size={150} className="right-post-avatar" />}
        </FlexContainer>
      </article>
    </PostCard>
  );
};
