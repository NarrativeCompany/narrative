import * as React from 'react';
import { FormField, InputFieldProps } from './FormField';
import { TwoFactorLoginMessages } from '../i18n/TwoFactorLoginMessages';
import { FormattedMessage, injectIntl } from 'react-intl';
import { compose } from 'recompose';
import InjectedIntlProps = ReactIntl.InjectedIntlProps;
import { HTMLAttributes } from 'react';

type ParentProps = Pick<InputFieldProps, 'name'> &
  // jw: I can't explain why, but pulling style off of the actual props above results in style being required. My best
  //     bet is that somewhere in the InputFieldProps definitions there is a style that is required, and that is what
  //     is being picked.
  Pick<HTMLAttributes<{}>, 'style'>;

type Props = ParentProps & InjectedIntlProps;

const TwoFactorAuthenticationCodeInputComponent: React.SFC<Props> = (props) => {
  const { intl, ...inputProps } = props;

  return (
    <FormField.Input
      size="large"
      type="text"
      pattern="\d*"
      maxLength={6}
      autoComplete="off"
      label={<FormattedMessage {...TwoFactorLoginMessages.VerificationCodeInputLabel} />}
      placeholder={intl.formatMessage(TwoFactorLoginMessages.VerificationCodeInputPlaceholder)}
      {...inputProps}
    />
  );
};

export const TwoFactorAuthenticationCodeInput = compose(
  injectIntl
)(TwoFactorAuthenticationCodeInputComponent) as React.ComponentClass<ParentProps>;
