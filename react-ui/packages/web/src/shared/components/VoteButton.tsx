import * as React from 'react';
import { Button, ButtonProps } from './Button';
import styled from '../styled';

export const StyledButton =
  styled<ButtonProps & {isButtonActive: boolean}>(({isButtonActive, ...rest}) => <Button {...rest}/>)`
    // a style issue was introduced where the button icon would temporarily 
    // turn black while request was in flight (keep this style here to prevent it)
    .anticon {
      color: #fff;
    }
    
    ${props => !props.isButtonActive && 
      `border-color: #e2e6ec !important;
      background-color: #f9fafb !important;

      color: ${props.theme.textColorDark} !important;
      transition: all .15s ease-in-out;
      
      .anticon {
        color: #e2e6ec !important;
        transition: all .15s ease-in-out;
      }
      
      &:hover {
        background-color: #f5f5f5 !important;
        border-color: #e1e1e1 !important;

        .anticon {
          color: #e1e1e1 !important;
          transition: all .15s ease-in-out;
        }
      }`
    }
  `;

export interface VoteButtonProps {
  isButtonActive: boolean;
  buttonType: 'primary' | 'danger';
  onClick?: () => void;
  isVoting?: boolean;
  isDisabled?: boolean;
  className?: string;
}

export const VoteButton: React.SFC<VoteButtonProps> = (props) => {
  // jw: passing the rest of the properties through to StyledButton so that things like event listeners and children
  //     will work as expected. Discovered when I tried to use a Tooltip with this.
  const { isButtonActive, buttonType, onClick, isVoting, isDisabled, ...rest } = props;

  return (
    <StyledButton
      isButtonActive={isButtonActive}
      type={buttonType}
      loading={isVoting}
      disabled={isDisabled}
      size="large"
      block={true}
      onClick={onClick}
      {...rest}
    />
  );
};
