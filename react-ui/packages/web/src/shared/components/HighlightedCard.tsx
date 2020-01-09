import * as React from 'react';
import { Card, CardProps } from './Card';
import { themeColors } from '../styled/theme';
import styled from '../styled';

export const HighlightedCardColors = {
  'primary-blue': themeColors.primaryBlue,
  blue: '#CCDCF3',
  'secondary-blue': '#D6E7FF',
  'dark-blue': themeColors.darkBlue,
  green: '#C6D83F',
  'bright-green': themeColors.brightGreen,
  gold: themeColors.gold,
  orange: themeColors.primaryOrange,
  red: themeColors.primaryRed,
  'grey-blue': themeColors.greyBlue,
  'midnight-blue': '#0087B2',
  'purple': themeColors.purple
};

export type HighlightColor = keyof typeof HighlightedCardColors;
export type HighlightSide = 'top' | 'right' | 'bottom' | 'left' | 'all';
export type HighlightWidth = 'wide' | 'narrow' | number;

export interface HighlightedCardStyleProps {
  highlightColor?: HighlightColor;
  highlightSide?: HighlightSide;
  highlightWidth?: HighlightWidth;
}

function getHighlightWidth(props: HighlightedCardStyleProps) {
  const { highlightWidth } = props;

  if (typeof highlightWidth === 'number') {
    return `${highlightWidth}px`;
  }

  switch (highlightWidth) {
    case 'wide':
      return '15px';
    default:
      return '7px';
  }
}

function getBorder(props: HighlightedCardStyleProps) {
  const { highlightSide } = props;

  const borderDefinition = `solid ${getHighlightWidth(props)} ${getHighlightColor(props)}`;

  switch (highlightSide) {
    case 'top':
    case 'right':
    case 'bottom':
    case 'left':
      return `border-${highlightSide}: ${borderDefinition};`;
    case 'all':
      return `border: ${borderDefinition};`;
    default:
      return `border-left: ${borderDefinition};`;
  }
}

function getHighlightColor(props: HighlightedCardStyleProps) {
  const { highlightColor } = props;

  if (!highlightColor) {
    return null;
  }

  return HighlightedCardColors[highlightColor];
}

export type HighlightedCardProps =
  CardProps &
  HighlightedCardStyleProps;

const StyledCard = styled<HighlightedCardProps>
(({highlightColor, highlightSide, highlightWidth, ...cardProps}) => <Card {...cardProps}/>)`
  &.ant-card {
    ${props => getBorder(props)};
  }
`;

export const HighlightedCard: React.SFC<HighlightedCardProps> = (props) => <StyledCard {...props} />;
