import * as React from 'react';
import { Button, Dropdown, Icon } from 'antd';
import { FlexContainer } from '../styled/shared/containers';
import { DropDownProps } from 'antd/lib/dropdown';
import { NativeButtonProps } from 'antd/lib/button/button';
import styled from '../styled';

// tslint:disable no-any
const DropdownButton =
  styled<NativeButtonProps>(({...rest}) => <Button {...rest}/>)`
    background-color: #f9fafb !important;
    
    &:hover,
    &:focus, 
    &:active {
      border-color: #d9d9d9 !important;
      color: rgba(0, 0, 0, 0.65) !important;
    }
  ` as any;

// tslint:enable no-any

interface ParentProps {
  isFullWidth?: boolean;
  btnText: string;
}

type Props =
  DropDownProps &
  ParentProps;

export const ButtonDropdownMenu: React.SFC<Props> = (props) => {
  const {btnText, isFullWidth, trigger, ...rest} = props;
  const buttonWidth = isFullWidth ? '100%' : 'auto';

  return (
    <Dropdown {...rest} trigger={trigger || ['click']}>
      <DropdownButton size="large" style={{width: buttonWidth}}>
        <FlexContainer justifyContent="space-between" alignItems="center">
          {btnText}
          <Icon type="down"/>
        </FlexContainer>
      </DropdownButton>
    </Dropdown>
  );
};
