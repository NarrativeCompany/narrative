import * as React from 'react';
import { Input } from 'antd';
import { FormControl } from './FormControl';
import { Field, FieldProps, FieldAttributes } from 'formik';
import { TextAreaProps } from 'antd/lib/input';
import { ColProps } from 'antd/lib/grid';

const { TextArea } = Input;

interface FormItemProps {
  label?: React.ReactNode;
  labelCol?: ColProps;
  wrapperCol?: ColProps;
  extra?: React.ReactNode;
  hasFeedback?: boolean;
}

type Props =
  FieldAttributes<{}> &
  TextAreaProps &
  FormItemProps;

export const TextAreaField: React.SFC<Props> = (props) => {
  const {
    name,
    style,
    label,
    labelCol,
    wrapperCol,
    extra,
    hasFeedback,
    ...textAreaProps
  } = props;

  const renderTextArea = (fieldProps: FieldProps<{}>) => {
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
        <TextArea {...field} {...textAreaProps} />
      </FormControl>
    );
  };

  return (
    <Field
      name={name}
      render={renderTextArea}
    />
  );
};
