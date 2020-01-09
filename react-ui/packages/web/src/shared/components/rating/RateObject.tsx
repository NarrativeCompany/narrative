import * as React from 'react';
import { QualityRatingFields } from '@narrative/shared';
import styled, { css } from '../../styled';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { Progress } from 'antd';
import { Text, TextColor } from '../Text';
import { Block, BlockProps } from '../Block';
import { FormattedMessage } from 'react-intl';
import { RatingMessages } from '../../i18n/RatingMessages';
import { LocalizedNumber } from '../LocalizedNumber';
import { themeColors, ThemeColorType } from '../../styled/theme';
import { Card, CardProps } from '../Card';
import { Heading } from '../Heading';
import { mediaQuery } from '../../styled/utils/mediaQuery';

interface IsSmallProps {
  isSmall?: boolean;
}

// jw: let's limit the incoming properties to the ones we care about.
type RatingFields = Pick<QualityRatingFields, 'totalVoteCount' | 'score'>;

function getGeneralStyling(props: IsSmallProps) {
  if (!props.isSmall) {
    return css`
      justify-content: space-between;
      max-width: 250px;
      margin: auto;

      .has-active-rating.ant-progress-circle {
        &.ant-progress-status-success .ant-progress-text {
          color: rgba(0, 0, 0, 0.65);
        }
        .ant-progress-circle-trail {
          stroke: ${p => p.theme.primaryRed};
        }
      }
    `;
  }

  return css`
    justify-content: left;
    margin-top: 10px;
  `;
}

function getIconSize(props: IsSmallProps) {
  return props.isSmall
    ? css`font-size: 18px;`
    : css`font-size: 40px;`;
}

function getIconDistance(props: IsSmallProps) {
  return props.isSmall
    ? '7px'
    : '10px';
}

function getIconHeightOffset(props: IsSmallProps) {
  return props.isSmall
    ? '0'
    : '4px';
}

const RatingWrapper = styled<FlexContainerProps & IsSmallProps>(({isSmall, ...rest}) =>
  <FlexContainer {...rest} />)`
  position: relative;
  ${p => getGeneralStyling(p)}

  .anticon {
    position: relative;
    color: ${p => p.theme.borderGrey}
    cursor: pointer;
    ${p => getIconSize(p)}
  }
  
  .left-rating-tool {
    margin-right: ${p => getIconDistance(p)};
    bottom: ${p => getIconHeightOffset(p)};
  }
  
  .right-rating-tool {
    margin-left:${p => getIconDistance(p)};
    top: ${p => getIconHeightOffset(p)};
  }
  
  .rating-vote-count {
    margin-left: 7px;
  }
`;

const FooterRatingCount = styled<BlockProps>(Block)`
  text-align: center;
  margin-top: 10px;
`;

const RatingCard = styled<CardProps>(Card)`
  width: 100%;
  
  .ant-card-body {
    padding: 15px 24px;
  }
  
  ${mediaQuery.lg_up`max-width: 350px;`}
`;

interface Props extends IsSmallProps {
  title?: FormattedMessage.MessageDescriptor;
  leftTool: React.ReactNode;
  rightTool: React.ReactNode;
  scorePlaceholder?: React.ReactNode;
  progressOverride?: number;
  smallTextColor?: TextColor;
  progressStrokeColor: ThemeColorType;
  fields: RatingFields;
}

export const RateObject: React.SFC<Props> = (props) => {
  const {
    title,
    fields,
    leftTool,
    rightTool,
    scorePlaceholder,
    progressOverride,
    progressStrokeColor,
    smallTextColor,
    isSmall
  } = props;

  const votes = fields.totalVoteCount;
  const voteCount = <LocalizedNumber value={votes} />;

  let progress;
  if (!isSmall) {

    const hasActiveRating = fields.score !== null;

    const actualScorePercent = fields.score || 0;
    const percent = progressOverride !== undefined ? progressOverride : actualScorePercent;

    progress = (
      <Progress
        type="circle"
        width={60}
        strokeWidth={8}
        strokeColor={themeColors[progressStrokeColor]}
        percent={percent}
        format={() => scorePlaceholder ? scorePlaceholder : `${actualScorePercent}%`}
        className={hasActiveRating ? 'has-active-rating' : undefined}
      />
    );
  } else {
    progress = (
      <Text size="small" color={smallTextColor}>
        {scorePlaceholder ? scorePlaceholder : `${fields.score}%`}
      </Text>
    );
  }

  const ratingVoteCount = <FormattedMessage {...RatingMessages.VoteCount} values={{votes, voteCount}} />;

  const ratingTool = (
    <RatingWrapper alignItems="center" justifyContent="space-between" isSmall={isSmall}>
      <Block className="left-rating-tool">
        {leftTool}
      </Block>

      {progress}

      <Block className="right-rating-tool">
        {rightTool}
      </Block>
      {isSmall &&  <Block className="rating-vote-count" size="small">{ratingVoteCount}</Block>}
    </RatingWrapper>
  );

  if (isSmall) {
    return ratingTool;
  }

  return (
    <RatingCard noBoxShadow={true}>
      {title && (
        <Heading size={4} textAlign="center" style={{marginBottom: '15px'}}>
          <FormattedMessage {...title}/>
        </Heading>
      )}

      {ratingTool}
      <FooterRatingCount size="small">{ratingVoteCount}</FooterRatingCount>
    </RatingCard>
  );
};
