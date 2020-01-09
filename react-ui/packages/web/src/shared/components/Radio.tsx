import * as React from 'react';
import { Radio as AntRadio } from 'antd';
import { RadioProps } from 'antd/lib/radio';
import styled from '../styled';

interface ParentProps {
  addMargin: boolean;
}

export const Radio = styled<RadioProps & ParentProps>(({addMargin, ...props}) => <AntRadio {...props}/>)`
  &.ant-radio-wrapper {
    display: flex;
    white-space: normal;
    line-height: 20px;
    margin-bottom: ${props => props.addMargin && '12px'};
  }
  
  .ant-radio {
    height: 16px;
    top: 1.5px;
  }
`;
