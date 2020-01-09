import * as React from 'react';
import { Switch } from 'antd';
import { FlexContainer, FlexContainerProps } from '../styled/shared/containers';
import { SwitchProps } from 'antd/lib/switch';
import styled from '../styled';
import { Heading, HeadingProps } from './Heading';

const SwitchWrapper = styled<FlexContainerProps>(FlexContainer)`
  .ant-switch {
    margin-right: 10px;
  }
`;

const Label = styled<HeadingProps>(Heading)`
  min-width: 175px;
  margin-top: 6px;
`;

interface ParentProps {
  label?: React.ReactNode;
  checkedMessage?: React.ReactNode;
  uncheckedMessage?: React.ReactNode;
}

type Props =
  ParentProps &
  SwitchProps;

export const SwitchComponent: React.SFC<Props> = (props) => {
  const { label, checkedMessage, uncheckedMessage, ...switchProps } = props;

  return (
    <SwitchWrapper alignItems="center">
      <Label uppercase={true} size={6}>{label}</Label>

      <Switch {...switchProps}/>

        {switchProps.checked &&
        checkedMessage}

        {!switchProps.checked &&
        uncheckedMessage}
    </SwitchWrapper>
  );
};
