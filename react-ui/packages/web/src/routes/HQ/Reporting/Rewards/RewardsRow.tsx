import styled from 'styled-components';
import { FlexContainer, FlexContainerProps } from '../../../../shared/styled/shared/containers';
import * as React from 'react';
import { ReactNode } from 'react';
import { mediaQuery } from '../../../../shared/styled/utils/mediaQuery';

export const RewardsWrapper = styled<FlexContainerProps>(FlexContainer)`
  flex-direction: column;
`;

const RewardsRowWrapper = styled<FlexContainerProps>(FlexContainer)`
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  font-size: 18px;
  margin-bottom: 10px;
  
  ${mediaQuery.xs`
    flex-direction: column;
    align-items: flex-start;
  `};
`;

const RewardsCell = styled<FlexContainerProps>(FlexContainer)`
  flex-direction: row;
`;

const TitleCell = styled<FlexContainerProps>(RewardsCell)`
  ${mediaQuery.xs`
    font-weight: 600;
  `};
`;

const ValueCell = styled<FlexContainerProps>(RewardsCell)`
  align-self: flex-start;
  margin-left: 5px;
  
  ${mediaQuery.xs`
    margin-left: 0;
  `};
`;

const Percentage = styled<FlexContainerProps>(FlexContainer)`
  margin-left: 10px;
  color: ${props => props.theme.lightGray};
`;

interface RewardsRowProps extends FlexContainerProps {
  title: ReactNode;
  percentage?: number;
  value: ReactNode;
}

export const RewardsRow: React.SFC<RewardsRowProps> = (props) => {
  const { title, value, percentage, ...otherProps } = props;
  return (
    <RewardsRowWrapper {...otherProps}>
      <TitleCell>
        {title}
        {percentage !== undefined && <Percentage>{percentage}%</Percentage>}
      </TitleCell>
      <ValueCell>
        {value}
      </ValueCell>
    </RewardsRowWrapper>
  );
};
