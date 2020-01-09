import {
  confirmEmailAddress,
  emailAddress,
  getValidationSchemaWithConditional2fa,
  password,
  TwoFactorEnabledOnAccountProps
} from './shared';
import { pw2FACodeFormInitialValues, PW2FACodeFormValues } from './pw2faCode';

export interface ChangeEmailAddressFormValues extends PW2FACodeFormValues {
  emailAddress: string;
  confirmEmailAddress: string;
}

export const changeEmailAddressFormInitialValues: ChangeEmailAddressFormValues = {
  ...pw2FACodeFormInitialValues,
  emailAddress: '',
  confirmEmailAddress: ''
};

export const changeEmailAddressFormValidators = {
  currentPassword: password,
  emailAddress,
  confirmEmailAddress
};

export const changeEmailAddressFormFormikUtil = {
  validationSchema: (props: TwoFactorEnabledOnAccountProps) => {
    return getValidationSchemaWithConditional2fa(props, changeEmailAddressFormValidators, 'twoFactorAuthCode');
  },
  mapPropsToValues: () => (changeEmailAddressFormInitialValues)
};
