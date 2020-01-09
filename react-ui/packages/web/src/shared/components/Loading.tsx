import * as React from 'react';
import styled from '../styled';
import { FlexContainer, FlexContainerProps } from '../styled/shared/containers';
import { List, Spin } from 'antd';
import { SpinProps } from 'antd/lib/spin';
import { ListGridType } from 'antd/lib/list';
import { generateSkeletonListProps, renderLoadingCard } from '../utils/loadingUtils';

const LoadingWrapper = styled(FlexContainer)<FlexContainerProps>`
  min-height: 100vh;
`;

export type LoadingProps = SpinProps;

export const Loading: React.SFC<LoadingProps> = (props) => (
  <LoadingWrapper centerAll={true}>
    <Spin
      tip={props.tip || 'Loading...'}
      size={props.size || 'large'}
    />
  </LoadingWrapper>
);

// todo: We need to integrate this inline version of the Loading indicator into the Loading component above!
// jw: The version above is absolutely positioned which is great for some circumstances, but in many places we want
//     the loading spinner and message to be a placeholder for content that is loading in dynamically. In that case
//     we want it to take up the space it occupies, and be centered horizontally within that area.
export const ContainedLoading: React.SFC<LoadingProps> = (props) => (
  <FlexContainer centerAll={true}>
    <Spin
      tip={props.tip || 'Loading...'}
      size={props.size || 'large'}
    />
  </FlexContainer>
);

export interface  CardListLoadingProps {
  grid: ListGridType;
  listLength: number;
}

export const CardListLoading: React.SFC<CardListLoadingProps> = (props) => {
  const { grid, listLength } = props;

  return (
    <List
      grid={grid}
      {...generateSkeletonListProps(listLength, renderLoadingCard)}
    />
  );
};
