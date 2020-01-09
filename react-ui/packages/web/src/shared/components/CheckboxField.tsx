import * as React from 'react';
import { Form, Checkbox } from 'antd';
import { Field, FieldProps, FieldAttributes } from 'formik';
import { CheckboxProps } from 'antd/lib/checkbox';

type CheckboxFieldProps =
  FieldAttributes<{}> &
  CheckboxProps & {
  label?: string | React.ReactNode;
  style?: React.CSSProperties;
  hasFeedback?: boolean
  help?: React.ReactNode;
};

export const CheckboxField: React.SFC<CheckboxFieldProps> = (props) => {
  const { name, style, label, children, help, ...checkboxProps } = props;

  const renderCheckbox = (fieldProps: FieldProps<{}>) => {
    const { field, form: { touched, errors } } = fieldProps;
    const errorMessage = touched[field.name] && errors[field.name];

    return (
      <Form.Item
        style={style}
        label={label}
        help={errorMessage || help}
        validateStatus={errorMessage ? 'error' : undefined}
        hasFeedback={props.hasFeedback || false}
      >
        {/* tslint:disable no-any */}
        <Checkbox
          {...field}
          {...checkboxProps}
          onChange={field.onChange as any}
          defaultChecked={!!field.value}
        >
          {/* tslint:enable no-any */}
          {children}
        </Checkbox>
      </Form.Item>
    );
  };

  return (
    <Field
      name={name}
      render={renderCheckbox}
    />
  );
};
