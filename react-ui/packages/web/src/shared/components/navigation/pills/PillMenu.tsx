import * as React from 'react';
import { PillMenuItem, PillMenuItemProps } from './PillMenuItem';
import styled from '../../../styled';
import { FlexContainer, FlexContainerProps } from '../../../styled/shared/containers';
import { CSSProperties } from 'react';

const PillMenuContainer = styled<FlexContainerProps>(FlexContainer)`
  margin: -10px 0 20px -20px;
  
  & > * {
    margin: 10px 0 0 20px;
  }
`;

export interface PillMenuProps {
  selectedPath: string;
  style?: CSSProperties;
  pills: PillMenuItemProps[];
}

export const PillMenu: React.SFC<PillMenuProps> = (props) => {
  const { selectedPath, style, pills } = props;

  return (
    <PillMenuContainer alignItems="center" flexWrap="wrap" style={style}>
      {pills.map((pill) =>
        <PillMenuItem key={pill.path} {...pill} selected={pill.path === selectedPath} />
      )}
    </PillMenuContainer>
  );
};
