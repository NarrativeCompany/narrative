import * as React from 'react';
import styled, { css } from '../styled';
import { getTextColor, getTextSize, WithTextColor, WithTextSize } from './Text';
import { HTMLAttributes } from 'react';

const MarginBottomDefault = css`
  margin-bottom: 10px;
`;

const MarginBottomSmall = css`
  margin-bottom: 5px;
`;

const MarginBottomLarge = css`
  margin-bottom: 15px;
`;

const ItalicFontStyle = css`
  font-style: italic;
`;

function getMarginBottom (props: ParagraphProps) {
  const { marginBottom } = props;

  switch (marginBottom) {
    case 'default':
      return MarginBottomDefault;
    case 'small':
      return MarginBottomSmall;
    case 'large':
      return MarginBottomLarge;
    default:
      return null;
  }
}

function getFontStyle (props: ParagraphProps) {
  const { fontStyle } = props;

  switch (fontStyle) {
    case 'italic':
      return ItalicFontStyle;
    default:
      return null;
  }
}

export interface ParagraphProps extends WithTextColor, WithTextSize, Pick<HTMLAttributes<{}>, 'itemProp'> {
  marginBottom?: 'small' | 'default' | 'large';
  fontStyle?: 'italic';
  uppercase?: boolean;
  isLink?: boolean;
  textAlign?: string;
  onClick?: () => void;
  style?: React.CSSProperties;
  className?: string;
}

export const Paragraph =
  styled<ParagraphProps>((
    {color, size, marginBottom, fontStyle, uppercase, isLink, textAlign, ...rest}
  ) => <p {...rest}>{rest.children}</p>)`
    ${props => getTextColor(props)};
    text-transform: ${props => props.uppercase && 'uppercase'};
    margin: 0;
    transition: all .15s ease-in-out;
    text-align: ${props => props.textAlign ? props.textAlign : 'left'};
    ${props => getMarginBottom(props)};
    ${props => getFontStyle(props)};
    
    &:hover {
      ${props => props.isLink && `
        color: ${props.theme.primaryBlue}; 
        transition: all .15s ease-in-out';
        cursor: pointer;
      `}
    }
      
    // bl: make sure links in the paragraph have the same font size. for whatever reason, our default link size
    // is 14px while our default font size is 16px. this will ensure links are consistent within the paragraph.
    &, & a {
      ${props => getTextSize(props)};
    }
  `;
