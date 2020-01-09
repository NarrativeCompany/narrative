import * as React from 'react';
import { Checkbox as AntCheckbox } from 'antd';
import { CheckboxProps } from 'antd/lib/checkbox';
import styled from '../styled';

export const Checkbox = styled<CheckboxProps>(({...props}) => <AntCheckbox {...props}/>)`
  &.ant-checkbox-wrapper {
    display: flex;
    align-items: flex-start;
    
  }
  
  .ant-checkbox-wrapper + span, 
  .ant-checkbox + span {
    line-height: 22px;
    position: relative;
    top: -3px;
  } 
  
  .ant-checkbox-checked .ant-checkbox-inner {
    background-color: ${p => p.theme.primaryBlue};
  }
  
  &.ant-checkbox-wrapper:hover .ant-checkbox-inner,
  .ant-checkbox:hover .ant-checkbox-inner,
  .ant-checkbox-input:focus + .ant-checkbox-inner,
  .ant-checkbox-checked:after,
  .ant-checkbox-checked .ant-checkbox-inner {
    border-color: ${p => p.theme.primaryBlue};  
  }
`;
