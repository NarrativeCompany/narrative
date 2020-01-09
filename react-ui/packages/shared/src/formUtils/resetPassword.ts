import { confirmNewPassword, password } from './shared/user';
import { getValidationSchemaWithConditional2fa, TwoFactorEnabledOnAccountProps } from './shared';
import { TwoFACodeFormValues, twoFactorAuthCodeFormInitialValues } from './pw2faCode';

export interface ResetPasswordFormValues extends TwoFACodeFormValues {
  newPassword: string;
  confirmNewPassword: string;
}

export const resetPasswordInitialValues: ResetPasswordFormValues = {
  ...twoFactorAuthCodeFormInitialValues,
  newPassword: '',
  confirmNewPassword: ''
};

const baseValidators = {
  newPassword: password,
  confirmNewPassword
};

export const resetPasswordFormFormikUtil = {
  validationSchema: (props: TwoFactorEnabledOnAccountProps) => {
    return getValidationSchemaWithConditional2fa(props, baseValidators, 'twoFactorAuthCode');
  },
  mapPropsToValues: () => (resetPasswordInitialValues)
};
