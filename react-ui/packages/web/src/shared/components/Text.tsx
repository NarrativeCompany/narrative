import * as React from 'react';
import styled, { css } from '../styled';

/*
     TEXT COLORS
 */
export const TextColors = {
  default: css`color: ${props => props.theme.textColor};`,
  white: css`color: #fff;`,
  light: css`color: ${props => props.theme.textColorLight};`,
  dark: css`color: ${props => props.theme.textColorDark};`,
  error: css`color: ${props => props.theme.primaryRed};`,
  primary: css`color: ${props => props.theme.secondaryBlue};`,
  lightBlue: css`color: ${props => props.theme.primaryBlue};`,
  lightGray: css`color: ${props => props.theme.lightGray};`,
  success: css`color: ${props => props.theme.primaryGreen};`,
  warning: css`color: ${props => props.theme.primaryOrange};`,
  inherit: css`color: inherit;`,
};

export type TextColor = keyof typeof TextColors;

export interface WithTextColor {
  color?: TextColor;
}

export function getTextColor (props: WithTextColor) {
  const { color } = props;

  if (color) {
    return TextColors[color];
  }

  return TextColors.default;
}

/*
      TEXT TRANSFORM
 */
export const TextTransforms = {
  uppercase: css`text-transform: uppercase;`,
  inherit: css`text-transform: inherit;`,
};

export type TextTransform = keyof typeof TextTransforms;

export interface WithTextTransform {
  transform?: TextTransform;
}

export function getTextTransform (props: WithTextTransform) {
  const { transform } = props;

  if (transform) {
    return TextTransforms[transform];
  }

  return null;
}

/*
     TEXT SIZES
 */
export const TextSizes = {
  small: css`font-size: ${props => props.theme.textFontSizeSmall};`,
  default: css`font-size: ${props => props.theme.textFontSizeDefault};`,
  large: css`font-size: ${props => props.theme.textFontSizeLarge};`,
  inherit: css`font-size: inherit;`,
};

export type TextSize = keyof typeof TextSizes;

export interface WithTextSize {
  size?: TextSize;
}

export function getTextSize (props: WithTextSize) {
  const { size } = props;

  if (size) {
    return TextSizes[size];
  }

  // bl: this used to return default. that wreaks havoc on font sizing, particularly with links. if no
  // size is set explicitly, we should always inherit and allow the parent to control the font size.
  return TextSizes.inherit;
}

/*
      ACTUAL COMPONENT
 */
function getTextWeight(props: TextProps) {
  const { weight } = props;

  if (!weight) {
    return null;
  }

  if ((typeof weight) === 'number' && weight < 1) {
    return null;
  }

  return css`font-weight: ${weight};`;
}

export interface TextProps extends WithTextColor, WithTextTransform, WithTextSize {
  weight?: number | string;
  style?: React.CSSProperties;
  className?: string;
}

export const Text = styled<TextProps>((
  {color, transform, size, weight, ...spanProps}
) => <span {...spanProps}>{spanProps.children}</span>)`
  ${props => getTextColor(props)};
  ${props => getTextTransform(props)};
  ${props => getTextWeight(props)};
  
  // bl: make sure links in the text have the same font size. for whatever reason, our default link size
  // is 14px while our default font size is 16px. this will ensure links are consistent within the text.
  &, & a {
    ${props => getTextSize(props)};
  }
`;
