import * as React from 'react';
import { Icon } from 'antd';
import { FlexContainer, FlexContainerProps, FlexDirection } from '../styled/shared/containers';
import { AnchorProps, Link } from './Link';
import { Button, ButtonProps } from './Button';
import styled from '../styled';

const ButtonGroupWrapper = styled<FlexContainerProps>(FlexContainer)`
  ${props => props.direction && props.direction.includes('column') && `
    .ant-btn {
      margin-bottom: 10px;
    }
  `}
`;

interface FormButtonGroupProps {
  linkText: React.ReactNode;
  hasBackArrow?: boolean;
  btnText: React.ReactNode;
  btnProps?: ButtonProps;
  linkProps?: AnchorProps;
  direction?: FlexDirection;
  style?: React.CSSProperties;
}

export const FormButtonGroup: React.SFC<FormButtonGroupProps> = (props) => {
  const { linkText, hasBackArrow, btnText, btnProps, linkProps, direction, style } = props;

  return (
    <ButtonGroupWrapper
      direction={direction || 'row'}
      alignItems="center"
      justifyContent="space-between"
      style={style}
    >
      <Link.Anchor {...linkProps}>
        {hasBackArrow && <Icon type="left" style={{marginRight: 5}}/>}
        {linkText}
      </Link.Anchor>

      <Button
        {...btnProps}
        type={btnProps && btnProps.type || 'primary'}
        size={btnProps && btnProps.size || 'large'}
      >
        {btnText}
      </Button>
    </ButtonGroupWrapper>
  );
};
