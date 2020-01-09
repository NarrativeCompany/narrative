import * as React from 'react';
import { Input } from 'antd';
import { FormControl } from './FormControl';
import { Field, FieldProps, FieldAttributes } from 'formik';
import { InputProps } from 'antd/lib/input';
import { ColProps } from 'antd/lib/grid';

interface FormItemProps {
  label?: React.ReactNode;
  labelCol?: ColProps;
  wrapperCol?: ColProps;
  extra?: React.ReactNode;
  hasFeedback?: boolean;
}

type Props =
  FieldAttributes<{}> &
  InputProps &
  FormItemProps;

export const InputField: React.SFC<Props> = (props) => {
  const {
    name,
    style,
    label,
    labelCol,
    wrapperCol,
    extra,
    hasFeedback,
    ...inputProps
  } = props;

  const renderInput = (fieldProps: FieldProps<{}>) => {
    const { field, form: { touched, errors } } = fieldProps;
    const errorMessage = touched[field.name] && errors[field.name];

    return (
      <FormControl
        style={style}
        label={label}
        labelCol={labelCol}
        wrapperCol={wrapperCol}
        help={errorMessage}
        validateStatus={errorMessage ? 'error' : undefined}
        hasFeedback={hasFeedback || true}
        extra={extra}
      >
        <Input {...field} {...inputProps} />
      </FormControl>
    );
  };

  return (
    <Field
      name={name}
      render={renderInput}
    />
  );
};
