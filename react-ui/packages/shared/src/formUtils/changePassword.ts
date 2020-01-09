import {
  confirmNewPassword,
  getValidationSchemaWithConditional2fa,
  password,
  TwoFactorEnabledOnAccountProps
} from './shared';
import { pw2FACodeFormInitialValues, PW2FACodeFormValues } from './pw2faCode';

export interface ChangePasswordFormValues extends PW2FACodeFormValues {
  newPassword: string;
  confirmNewPassword: string;
}

const changePasswordFormInitialValues: ChangePasswordFormValues = {
  ...pw2FACodeFormInitialValues,
  newPassword: '',
  confirmNewPassword: ''
};

const changePasswordFormValidators = {
  currentPassword: password,
  newPassword: password,
  confirmNewPassword
};

export const changePasswordFormFormikUtil = {
  validationSchema: (props: TwoFactorEnabledOnAccountProps) => {
    return getValidationSchemaWithConditional2fa(props, changePasswordFormValidators, 'twoFactorAuthCode');
  },
  mapPropsToValues: () => (changePasswordFormInitialValues)
};
