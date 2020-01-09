import * as React from 'react';
import { Alert } from 'antd';

interface ParentProps {
  title: React.ReactNode;
  description: React.ReactNode;
}

export const FormError: React.SFC<ParentProps> = (props) => {
  return (
    <Alert
      type="error"
      showIcon={true}
      message={props.title}
      description={props.description}
      style={{marginBottom: 15}}
      closable={true}
    />
  );
};
