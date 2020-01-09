import * as React from 'react';
import { Icon } from 'antd';
import { FlexContainer, FlexContainerProps } from '../../../../shared/styled/shared/containers';
import styled from '../../../../shared/styled';

// this class is taken from BallotBoxAction
const TribunalIconWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-right: 5px;
  
  > span {
    margin-left: 5px;
    line-height: 11px;
    font-size: ${props => props.theme.textFontSizeSmall};
    color: ${props => props.theme.textColor}
  }
`;

interface ParentProps {
  iconType: string;
  children: React.ReactNode;
}

export const TribunalAppealAction: React.SFC<ParentProps> = (props) => {
  const {iconType, children} = props;

  return (
    <TribunalIconWrapper centerAll={true}>
      <Icon type={iconType}/>
      <span>{children}</span>
    </TribunalIconWrapper>
  );
};
