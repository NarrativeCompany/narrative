import * as React from 'react';
import { AntFormItemProps, FormField } from './FormField';
import { FormattedMessage } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import { UsdFromNrve } from './UsdFromNrve';
import { FieldAttributes } from 'formik';

interface ParentProps extends
  Pick<FieldAttributes<{}>, 'name'>,
  // bl: have to pick these from InputHTMLAttributes, NOT FieldAttributes or else the optionality will be wrong,
  // which is presumably due to the complexity of FieldAttributes (which mixes intersection and union types. refer:
  // https://stackoverflow.com/questions/53741611/union-type-using-optional-properties
  // https://www.typescriptlang.org/docs/handbook/advanced-types.html
  Pick<React.InputHTMLAttributes<HTMLInputElement>, 'placeholder' | 'autoFocus' | 'style' | 'disabled'>,
  Pick<AntFormItemProps, 'errorCanContainHtml'>
{
  nrveUsdPrice?: string;
  showUsdOnly?: boolean;
  errors: {};
  values: {};
}

export const NrveInput: React.SFC<ParentProps> = (props) => {
  const {
    placeholder,
    name,
    autoFocus,
    style,
    disabled,
    errorCanContainHtml,
    errors,
    values,
    nrveUsdPrice,
    showUsdOnly
  } = props;

  const value = values[name];
  const error = errors[name];

  // jw: we should only resolve the USD from NRVE if we have a price, a value, and the field is not in error.
  let extra;
  if (nrveUsdPrice && value && !error) {
    extra = <UsdFromNrve nrve={value} nrveUsdPrice={nrveUsdPrice} showUsdOnly={showUsdOnly} />;
  }

  return (
    <FormField.Input
      placeholder={placeholder}
      name={name}
      maxLength={100}
      addonAfter={<FormattedMessage {...SharedComponentMessages.Nrve}/>}
      extra={extra}
      style={style}
      hasFeedback={false}
      autoFocus={autoFocus}
      disabled={disabled}
      errorCanContainHtml={errorCanContainHtml}
    />
  );
};
