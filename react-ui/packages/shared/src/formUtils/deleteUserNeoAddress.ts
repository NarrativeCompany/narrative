import {
  getValidationSchemaWithConditional2fa,
  password,
  TwoFactorEnabledOnAccountProps
} from './shared';
import { pw2FACodeFormInitialValues, PW2FACodeFormValues } from './pw2faCode';

export type DeleteUserNeoAddressFormValues = PW2FACodeFormValues;

const initialValues: DeleteUserNeoAddressFormValues = {
  ...pw2FACodeFormInitialValues
};

const baseValidators = {
  currentPassword: password,
};

export const deleteUserNeoAddressFormFormikUtil = {
  validationSchema: (props: TwoFactorEnabledOnAccountProps) => {
    return getValidationSchemaWithConditional2fa(props, baseValidators, 'twoFactorAuthCode');
  },
  mapPropsToValues: () => (initialValues)
};
