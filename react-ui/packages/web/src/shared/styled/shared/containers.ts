import * as React from 'react';
import styled, { css } from '../index';

export type FlexDirection = 'row' | 'row-reverse' | 'column' | 'column-reverse';
export type FlexAlign = 'initial' | 'inherit' | 'center' | 'flex-start' | 'flex-end' | 'baseline' | 'stretch';
export type FlexWrap = 'nowrap' | 'wrap' | 'wrap-reverse';
export type FlexJustify =
  'initial' | 'inherit' | 'center' | 'flex-start' | 'flex-end' | 'space-around' | 'space-between';

/*
     BORDER RADIUSES
 */
export const BorderRadiuses = {
  default: css`border-radius: 6px;`,
  large: css`border-radius: 10px;`
};

export type BorderRadius = keyof typeof BorderRadiuses;

export function getBorderRadius (props: FlexContainerProps) {
  const { borderRadius } = props;

  if (borderRadius) {
    return BorderRadiuses[borderRadius];
  }

  return null;
}

// tslint:disable no-any
export interface FlexContainerProps {
  column?: boolean;
  centerAll?: boolean;
  direction?: FlexDirection;
  alignItems?: FlexAlign;
  justifyContent?: FlexJustify;
  flexWrap?: FlexWrap;
  onClick?: () => any;
  style?: React.CSSProperties;
  className?: string;
  id?: string;
  borderRadius?: BorderRadius;
}
// tslint:enable no-any

export const FlexContainer = styled.div<FlexContainerProps>`
  display: flex;
  // TODO: #1085 Change me to allow setting flex-direction explicitly
  flex-direction: ${props => props.column ? 'column' : 'row'};
  ${props => props.direction && `flex-direction: ${props.direction};`}
  align-items: ${props => props.alignItems && props.alignItems};
  justify-content: ${props => props.justifyContent && props.justifyContent};
  ${props => props.centerAll && 'align-items: center; justify-content: center;'};
  ${props => props.flexWrap && `flex-wrap: ${props.flexWrap}`}
  ${props => getBorderRadius(props)}
`;
