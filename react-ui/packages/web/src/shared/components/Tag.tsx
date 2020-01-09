import * as React from 'react';
import { Tag as AntTag } from 'antd';
import { TagProps as AntTagProps } from 'antd/lib/tag';
import styled, { css } from '../styled';

const Colors = {
  red: css`
    color: ${props => props.theme.primaryRed};
    background-color: #ffe6e6;
    border: 1px solid ${props => props.theme.primaryRed};
  `,
  green: css`
    color: ${props => props.theme.primaryGreen};
    background-color: #e6fffa;
    border: 1px solid ${props => props.theme.primaryGreen};
  `,
  beige: css`
    color: #D0BE75;
    background-color: #FDF8E4;
    border: 1px solid #A5850B;
  `,
  default: css`
    color: ${props => props.theme.defaultTagColor};
    background-color: ${props => props.theme.defaultTagBackgroundColor};
    border-color: ${props => props.theme.defaultTagColor};
  `
};

export type TagColor = keyof typeof Colors;

export interface TagProps extends AntTagProps {
  color?: TagColor;
  size?: 'normal' | 'large';
  margin?: 'small' | 'none';
  notLinked?: boolean;
}

function getColoring(props: TagProps) {
  const { color, notLinked } = props;

  const colorCss = color ? Colors[color] : Colors.default;

  // jw: default is the only tag we ever link, but if we are not then do not give it the hover effect.
  if (colorCss === Colors.default && !notLinked) {
    return css`
      ${colorCss};
      &:hover {
        background: ${p => p.theme.defaultTagBackgroundColorHover};
      }
    `;
  }

  return colorCss;
}

function getHeight(props: TagProps) {
  const { size } = props;

  switch (size) {
    case 'normal':
      return '23px';
    default:
      return '34px';
  }
}

function getPadding(props: TagProps) {
  const { size } = props;

  switch (size) {
    case 'normal':
      return '0 12px';
    default:
      return '0 22px';
  }
}

function getBorderRadius(props: TagProps) {
  const { size } = props;

  switch (size) {
    case 'normal':
      return '4px';
    default:
      return '25px';
  }
}

function getMargin(props: TagProps) {
  const { margin } = props;

  switch (margin) {
    case 'small':
      return '5px';
    default:
      return '0';
  }
}

export const Tag = styled<TagProps>(({color, size, margin, notLinked, ...props}) => <AntTag {...props}/>)`
  &.ant-tag {
    ${props => getColoring(props)};
    min-height: ${props => getHeight(props)};
    padding: ${props => getPadding(props)};
    border-radius: ${props => getBorderRadius(props)};
    margin: ${props => getMargin(props)};
    
    ${p => p.notLinked && css`
      cursor: default;
    `};
    
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: inherit;
  } 
`;
