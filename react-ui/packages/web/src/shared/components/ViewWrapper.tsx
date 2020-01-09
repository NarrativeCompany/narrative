import * as React from 'react';
import styled, { theme } from '../styled';
import { FlexContainerProps, FlexContainer } from '../styled/shared/containers';
import { mediaQuery } from '../styled/utils/mediaQuery';

export const defaultViewWrapperPadding = 24;

function getViewportPadding(props: ViewWrapperProps): number {
  return !props.noPadding ? defaultViewWrapperPadding : 0;
}

// jw: the view adds padding to the view wrapper to ensure that for smaller viewports there is a gap between the side of
//     the screen and the content. Without padding the content juts right up against the viewport. Because of that, lets
//     adjust the max-width of our container so that after the padding the inner space will be what we want.
export function getViewportMaxWidth(props: ViewWrapperProps): number {
  const maxWidth = props.maxWidth ? props.maxWidth : theme.layoutMaxWidth;
  const padding = getViewportPadding(props);

  // jw: no adjustment necessary if we do not have any padding.
  if (padding === 0) {
    return maxWidth;
  }

  // jw: since the padding will be on both sides, let's double the pading value.
  return maxWidth + (2 * padding);
}

const StyledViewWrapper = styled<ViewWrapperProps>(FlexContainer)`
  padding: ${p => getViewportPadding(p)}px;
  background: transparent;
  height: 100%;
  max-width: ${p => getViewportMaxWidth(p)}px;
  margin: 0 auto;
  min-height: ${props => props.minHeight || 'calc(100vh - 64px)'};
  
  ${p => !p.noPadding && mediaQuery.xs`
    padding: ${defaultViewWrapperPadding}px ${defaultViewWrapperPadding / 2}px;
  `}
`;

const GradientBox = styled.div`
  position: absolute;
  top: 0;
  left: 0;
  
  height: 5px;
  width: 100%;
  border: 1px solid #E2E6EC;
  background: linear-gradient(176.08deg, #40a9ff 0%, #9674C2 49.19%, #FF2A68 100%);
  
  // jw: this parallels height from Header.tsx
  margin-top: 50px;
  ${mediaQuery.md_up`
    margin-top: 64px;
  `}
`;

interface ParentProps {
  // jw: this will override the default gradient box that appears at the top of the page.
  // note: Right now we require one, so it's either the default, or a custom one, but never none.
  gradientBox?: React.ReactNode;
  children?: React.ReactNode;
  noPadding?: boolean;
  maxWidth?: number;
  minHeight?: string | number;
}

export type ViewWrapperProps =
  FlexContainerProps
  & ParentProps;

export const ViewWrapper: React.SFC<ViewWrapperProps> = (props) => {
  const { gradientBox } = props;

  return (
    <StyledViewWrapper column={true} {...props}>
      {gradientBox ? gradientBox : <GradientBox />}

      {props.children}
    </StyledViewWrapper>
  );
};
