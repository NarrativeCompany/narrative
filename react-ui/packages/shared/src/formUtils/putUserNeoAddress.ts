import {
  getValidationSchemaWithConditional2fa,
  password,
  TwoFactorEnabledOnAccountProps
} from './shared';
import { pw2FACodeFormInitialValues, PW2FACodeFormValues } from './pw2faCode';
import { neoAddressRequiredValidator } from './shared';

export interface PutUserNeoAddressFormValues extends PW2FACodeFormValues {
  neoAddress?: string;
}

const initialValues: PutUserNeoAddressFormValues = {
  ...pw2FACodeFormInitialValues,
  neoAddress: undefined
};

const baseValidators = {
  currentPassword: password,
  neoAddress: neoAddressRequiredValidator
};

export const putUserNeoAddressFormFormikUtil = {
  validationSchema: (props: TwoFactorEnabledOnAccountProps) => {
    return getValidationSchemaWithConditional2fa(props, baseValidators, 'twoFactorAuthCode');
  },
  mapPropsToValues: () => (initialValues)
};
