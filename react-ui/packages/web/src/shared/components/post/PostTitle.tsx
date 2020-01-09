import * as React from 'react';
import { Post } from '@narrative/shared';
import { Heading, headingLineHeightBuffer, HeadingProps } from '../Heading';
import { postTitleBaseStyles } from '../../styled/shared/post';
import { QualityLevelIcon } from '../QualityLevelIcon';
import { PostLink } from './PostLink';
import { IconSize } from '../CustomIcon';
import styled from '../../styled';
import { EnhancedAgeRating } from '../../enhancedEnums/ageRating';
import { RestrictedBadge } from '../RestrictedBadge';
import { FeaturedInPublicationIcon } from '../publication/FeaturedInPublicationIcon';
import { themeColors, themeTypography } from '../../styled/theme';
import { LiveOnNetworkTag } from './LiveOnNetworkTag';

type StyleProps = Pick<HeadingProps, 'size'>;

// zb: isLink is assumed to be true when the property is not set
export interface PostTitleParentProps extends StyleProps {
  post: Post;
  isLink?: boolean;
  style?: React.CSSProperties;
  forPublicationDisplay?: boolean;
  forPublicationReview?: boolean;
}

type Props = PostTitleParentProps;

function getPostFontSize(props: StyleProps) {
  switch (props.size) {
    case 1:
      return themeTypography.h1FontSize;
    case 2:
      return themeTypography.h2FontSize;
    case 3:
      return themeTypography.h3FontSize;
    case 4:
      return themeTypography.h4FontSize;
    case 5:
      return themeTypography.h5FontSize;
    case 6:
      return themeTypography.h6FontSize;
    default:
      throw new Error('Unsupported post title size');
  }
}

function getPostLineHeight(props: StyleProps) {
  switch (props.size) {
    case 1:
      return themeTypography.h1FontSize + headingLineHeightBuffer;
    case 2:
      return themeTypography.h2FontSize + headingLineHeightBuffer;
    case 3:
      return themeTypography.h3FontSize + headingLineHeightBuffer;
    case 4:
      return themeTypography.h4FontSize + headingLineHeightBuffer;
    case 5:
      return themeTypography.h5FontSize + headingLineHeightBuffer;
    case 6:
      return themeTypography.h6FontSize + headingLineHeightBuffer;
    default:
      throw new Error('Unsupported post title size');
  }
}

const StyledHeading = styled<HeadingProps & { forPublicationReview?: boolean }>(Heading)`
  margin-bottom: 0;

  &, & a {
    ${postTitleBaseStyles};
    ${p => p.forPublicationReview && `
      color: ${themeColors.primaryBlue};
    `};
    font-size: ${p => getPostFontSize(p)}px;
    line-height: ${p => getPostLineHeight(p)}px;
  };
`;

// jw: I tried to use a styled<RestrictedBadgeProps>(RestrictedBadge) but the styling would never come across. So, gonna
//     do this dirty since I just want to get it done and am totally unsure why the styling is not coming across after
//     20 minutes of digging.
const badgeStyle: React.CSSProperties = {
  // jw: to make this appear centered we need to shift it up a touch.
  position: 'relative',
  top: '-0.1em'
};

export const PostTitle: React.SFC<Props> = (props) => {
  const { post, size, isLink, style, forPublicationDisplay, forPublicationReview } = props;

  const ageRating = EnhancedAgeRating.get(post.ageRatingFields.ageRating);

  const iconSize: IconSize = getPostFontSize(props);
  const useLink = (isLink == null ? true : isLink);

  return (
    <StyledHeading size={size} style={style} itemProp="headline" forPublicationReview={forPublicationReview}>
      {forPublicationDisplay
        // jw: if we are excluding the quality icon then let's try to include the featured icon.
        ? <FeaturedInPublicationIcon
            post={post}
            size={iconSize}
            style={{...badgeStyle, marginRight: '0.25em'}}
          />
        : <QualityLevelIcon
            qualityLevel={post.qualityRatingFields.qualityLevel}
            size={iconSize}
            style={{...badgeStyle, marginRight: '0.25em'}}
          />
      }
      {useLink
        ? <PostLink post={post} size="inherit" target={forPublicationReview ? '_blank' : undefined} />
        : post.title
      }
      {forPublicationReview && post.postLive && <LiveOnNetworkTag/>}

      {ageRating.isRestricted() && <RestrictedBadge style={{...badgeStyle, marginLeft: '0.25em'}} /> }
    </StyledHeading>
  );
};
