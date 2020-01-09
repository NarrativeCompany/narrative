import * as React from 'react';
import { WithQualityRatingControllerHandlers } from '../../containers/withQualityRatingController';
import { QualityRating, QualityRatingFields } from '@narrative/shared';
import styled, { css, theme } from '../../styled';
import { Link } from '../Link';
import { Icon, IconProps } from '../Icon';
import { RateObject } from './RateObject';
import { RatingMessages } from '../../i18n/RatingMessages';
import { EnhancedQualityLevel } from '../../enhancedEnums/qualityLevel';

interface IsSmallProps {
  isSmall?: boolean;
}

function resolveIconHeightOffset(props: StyledIconProps) {
  if (props.isSmall) {
    return null;
  }

  if (props.forLike) {
    return css`bottom: 4px;`;
  }

  return css`top: 4px;`;
}

type StyledIconProps =
  IconProps &
  IsSmallProps & {
    forLike: boolean;
    currentRating?: QualityRating;
  };

function resolveIconColor(props: StyledIconProps) {
  const { forLike, currentRating } = props;

  if (!currentRating) {
    return null;
  }

  const wasLiked = currentRating === QualityRating.LIKE;

  // jw: don't color the one that was not chosen
  if (forLike !== wasLiked) {
    return null;
  }

  if (forLike) {
    return css`color: ${theme.secondaryBlue} !important;`;
  }

  return css`color: ${theme.primaryRed} !important;`;
}

export const QualityRatingIcon = styled<StyledIconProps>(({forLike, currentRating, isSmall, ...props}) =>
  <Icon {...props} theme="filled" />
)`
  transition: all .15s ease-in-out;
  ${props => resolveIconColor(props)}
  ${props => resolveIconHeightOffset(props)}
`;

interface Props extends WithQualityRatingControllerHandlers, IsSmallProps {
  objectOid: string;
  authorOid: string;
  fields: QualityRatingFields;
  // jw: note: I loathe the null default, but that is what the graphql optional types result in, so I will consume that
  //     to make use of this easier from our data objects, and then convert internally.
  current: QualityRating | null;
}

export const QualityRateObject: React.SFC<Props> = (props) => {
  const { objectOid, authorOid, fields, current, isSmall, handleQualityRating, handleOpenDownVoteSelector } = props;

  const dash = <React.Fragment>&mdash;</React.Fragment>;

  const currentRating = current ? current : undefined;

  const isScoreSet = fields.score !== null;
  const isPreviewScore = isScoreSet && !fields.qualityLevel;
  const qualityLevel = fields.qualityLevel ? EnhancedQualityLevel.get(fields.qualityLevel) : null;
  const progressStrokeColor = qualityLevel ? qualityLevel.themeColor : 'secondaryBlue';

  const textColor = !isSmall ? undefined : isPreviewScore || !qualityLevel ? 'light' : qualityLevel.textColor;

  const handleUpvote = () => {
    if (currentRating === QualityRating.LIKE) {
      // bl: remove the vote if the current rating is a LIKE
      handleQualityRating(objectOid, authorOid, undefined);
    } else {
      handleQualityRating(objectOid, authorOid, QualityRating.LIKE);
    }
  };

  const handleDownvote = () => {
    if (currentRating && currentRating !== QualityRating.LIKE) {
      // bl: remove the vote if there is a current rating and it is NOT a LIKE
      handleQualityRating(objectOid, authorOid, undefined);
    } else {
      handleOpenDownVoteSelector(objectOid, authorOid, currentRating);
    }
  };

  return (
    <RateObject
      title={RatingMessages.Rating}
      fields={fields}
      scorePlaceholder={!isScoreSet ? dash : undefined}
      progressOverride={isPreviewScore ? 0 : undefined}
      progressStrokeColor={progressStrokeColor}
      smallTextColor={textColor}
      isSmall={isSmall}
      leftTool={(
        <Link.Anchor onClick={handleUpvote}>
          <QualityRatingIcon svgIcon="thumb-up" isSmall={isSmall} forLike={true} currentRating={currentRating}/>
        </Link.Anchor>
      )}
      rightTool={(
        <Link.Anchor onClick={handleDownvote}>
          <QualityRatingIcon svgIcon="thumb-down" isSmall={isSmall} forLike={false} currentRating={currentRating}/>
        </Link.Anchor>
      )}
    />
  );
};
