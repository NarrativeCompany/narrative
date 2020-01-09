import * as React from 'react';
import { Input as AntInput } from 'antd';
import { Checkbox as AntCheckbox } from './Checkbox';
import { FormControl, FormControlProps } from './FormControl';
import { Field, FieldProps, FieldAttributes } from 'formik';
import { InputProps, TextAreaProps } from 'antd/lib/input';
import { CheckboxProps } from 'antd/lib/checkbox';
import styled from '../styled';
import { getPropertyByPath } from '@narrative/shared';

const formFieldFontFamily = `
  font-family: Lato, sans-serif;
  
  &::placeholder {
    font-family: Lato, sans-serif;
  }
`;

export const StyledInput = styled<InputProps>((props) => <AntInput {...props}/>)`
  ${formFieldFontFamily}
`;

export const StyledTextArea = styled<TextAreaProps>((props) => <AntInput.TextArea {...props}/>)`
  ${formFieldFontFamily}
`;

export interface AntFormItemProps extends
  Pick<FormControlProps, 'label' | 'labelCol' | 'wrapperCol' | 'extra' | 'hasFeedback' | 'usesButtonAddonAfter'>
{
  formFieldStyle?: React.CSSProperties;
  errorCanContainHtml?: boolean;
}

// jw: because the Formik "Touched" object is a full object, we need to convert from the string fieldname to the actual
//     property it references.
function isFieldTouched(fieldName: string, touched: {[key: string]: {} | boolean}): boolean {
  const isTouched = getPropertyByPath(touched, fieldName);

  return !!isTouched;
}

function resolveError(fieldName: string, errors: {[key: string]: {} | string}): string | undefined {
  let error: string | {} | undefined = errors[fieldName];

  // jw: if we did not find an error and the name has a dot in it then let's try and get it from deeper in the object.
  // note: I am ignoring the 0 position since no nested property should ever be referenced without a name!
  if (!error && fieldName.indexOf('.') > 0) {
    error = getPropertyByPath(errors, fieldName);
  }

  // jw: let's ensure that the error is a string, and not a object.
  if (typeof error === 'string') {
    return error as string;
  }

  return undefined;
}

type FormItemProps =
  FieldAttributes<{}> &
  AntFormItemProps;

const FormFieldComponent: React.SFC<FormItemProps & {as: React.ComponentClass}> = (props) => {
  const {
    as: Component,
    name,
    style,
    formFieldStyle,
    errorCanContainHtml,
    label,
    labelCol,
    wrapperCol,
    extra,
    hasFeedback,
    usesButtonAddonAfter,
    ...formFieldProps
  } = props;

  const renderFormField = (fieldProps: FieldProps<{}>) => {
    const { field, form: { touched, errors } } = fieldProps;
    // jw: this deserves a bit of explanation. touched is a Formik internal object that represents the touched fields as
    //     a parallel object to the form data, with boolean trues for the values of fields that have been affected.
    //     errors on the other hand can be either a string or an object depending on where the error comes from.
    const errorMessage = isFieldTouched(field.name, touched)
      ? resolveError(field.name, errors)
      : undefined;

    const inputProps = {
      ...formFieldProps,
      style: formFieldStyle,
    };

    const errorForDisplay = !errorMessage
      ? undefined
      : errorCanContainHtml
        ? <span dangerouslySetInnerHTML={{__html: errorMessage}}/>
        : errorMessage;

    return (
      <FormControl
        style={style}
        label={label}
        labelCol={labelCol}
        wrapperCol={wrapperCol}
        help={errorForDisplay}
        validateStatus={errorForDisplay ? 'error' : undefined}
        hasFeedback={hasFeedback !== undefined ? hasFeedback : true}
        extra={extra}
        usesButtonAddonAfter={usesButtonAddonAfter}
      >
        <Component {...field} {...inputProps}/>
      </FormControl>
    );
  };

  return (
    <Field
      name={name}
      render={renderFormField}
    />
  );
};

export type InputFieldProps =
  FormItemProps &
  InputProps;

export type TextAreaFieldProps =
  FormItemProps &
  TextAreaProps;

export type CheckboxFieldProps =
  FormItemProps &
  CheckboxProps;

const Input: React.SFC<InputFieldProps> = (props) => <FormFieldComponent as={StyledInput} {...props}/>;

const TextArea: React.SFC<TextAreaFieldProps> = (props) => <FormFieldComponent as={StyledTextArea} {...props}/>;

const Checkbox: React.SFC<CheckboxFieldProps> = (props) => <FormFieldComponent as={AntCheckbox} {...props}/>;

export class FormField extends React.Component<{}, {}> {
  static Input = Input;
  static TextArea = TextArea;
  static Checkbox = Checkbox;
}
