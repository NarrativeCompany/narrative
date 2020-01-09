import * as React from 'react';
import { Form } from 'antd';
import { FormItemProps } from 'antd/lib/form';
import styled, { css } from '../styled';

export interface FormControlProps extends FormItemProps {
  usesButtonAddonAfter?: boolean;
}

export const FormControl = styled<FormControlProps>(({usesButtonAddonAfter, ...rest}) =>
  <Form.Item colon={false} {...rest}/>)`
  .ant-form-item-label {
    text-align: initial;
    text-transform: uppercase;
    font-size: 12px;
    font-weight: 600;
  }
  ${p => p.usesButtonAddonAfter && css`
    .ant-input-wrapper .ant-input-group-addon:last-child {
      padding: 0;
      border: 0;
    
      .ant-btn {
        border-radius: 0 4px 4px 0;
      }
    }
  `};
`;
