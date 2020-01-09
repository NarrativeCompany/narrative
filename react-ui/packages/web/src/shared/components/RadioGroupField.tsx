import * as React from 'react';
import { Form, Radio as AntRadio } from 'antd';
import { Radio } from './Radio';
import { Field, FieldProps, FieldAttributes } from 'formik';
import { RadioChangeEvent, RadioGroupProps } from 'antd/lib/radio';

const RadioGroup = AntRadio.Group;

export interface RadioFieldValues {
  value: string;
  text: React.ReactNode;
}

export type RadioField = RadioFieldValues[];

export interface RadioFieldProps {
  radioFields: RadioField;
  help?: React.ReactNode;
}

type RadioGroupFieldProps =
  RadioFieldProps &
  RadioGroupProps & {
  label?: string;
  fieldMargin?: number | string;
  hasFeedback?: boolean;
};

type Props =
  FieldAttributes<{}> &
  RadioGroupFieldProps;

export const RadioGroupField: React.SFC<Props> = (props) => {
  const { name, fieldMargin, label, hasFeedback, radioFields, help } = props;

  const renderRadioGroup = (fieldProps: FieldProps<{}>) => {
    const { field, form: { touched, errors }, form } = fieldProps;
    const errorMessage = touched[field.name] && errors[field.name];

    return (
      <Form.Item
        style={{margin: fieldMargin}}
        label={label}
        help={errorMessage || help}
        validateStatus={errorMessage ? 'error' : undefined}
        hasFeedback={hasFeedback}
      >
        <RadioGroup
          {...field}
          name={name}
          onChange={(e: RadioChangeEvent) => form.setFieldValue(name, e.target.value)}
        >
          {radioFields.map((radio, i) => (
            <Radio key={i} value={radio.value} addMargin={true}>
              {radio.text}
            </Radio>
          ))}
        </RadioGroup>
      </Form.Item>
    );
  };

  return (
    <Field
      name={name}
      render={renderRadioGroup}
    />
  );
};
