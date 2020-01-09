import * as React from 'react';
import { Form, Select } from 'antd';
import { Field, FieldProps, FieldAttributes } from 'formik';
import { SelectProps } from 'antd/lib/select';

const Option = Select.Option;

export interface SelectFieldValues {
  value: string;
  text: React.ReactNode;
}

export type SelectFields = SelectFieldValues[];

export interface SelectFieldOptionProps {
  selectFields: SelectFields;
}

type SelectFieldProps =
  FieldAttributes<{}> &
  SelectFieldOptionProps &
  SelectProps & {
  label?: string | React.ReactNode;
  style?: React.CSSProperties;
  hasFeedback?: boolean
};

export const SelectField: React.SFC<SelectFieldProps> = (props) => {
  const { name, style, label, defaultValue, selectFields, ...selectProps } = props;

  const renderSelect = (fieldProps: FieldProps<{}>) => {

    const { field, form: { touched, errors }, form } = fieldProps;
    const errorMessage = touched[field.name] && errors[field.name];

    return (
      <Form.Item
        label={label}
        style={style}
        help={errorMessage}
        validateStatus={errorMessage ? 'error' : undefined}
        hasFeedback={props.hasFeedback || false}
      >
        {/* tslint:disable no-any */}
        <Select
          style={style}
          {...field}
          {...selectProps}
          defaultValue={defaultValue}
          onChange={(value) => form.setFieldValue(name, value)}
        >
            {selectFields.map((option) => (
                <Option key={option.value} value={option.value}>
                  {option.text}
                </Option>
              ))}
        </Select>
      </Form.Item>
    );
  };

  return (
    <Field
      name={name}
      render={renderSelect}
    />
  );
};
