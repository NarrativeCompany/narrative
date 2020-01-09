import {
  getValidationSchemaWithConditional2fa,
  password,
  TwoFactorEnabledOnAccountProps
} from './shared';
import { pw2FACodeFormInitialValues, PW2FACodeFormValues } from './pw2faCode';
import { requiredNrveValidator } from './shared/nrveFormUtils';

export interface RequestRedemptionFormValues extends PW2FACodeFormValues {
  redemptionAmount: string;
}

const initialValues: RequestRedemptionFormValues = {
  ...pw2FACodeFormInitialValues,
  redemptionAmount: ''
};

const baseValidators = {
  currentPassword: password,
  redemptionAmount: requiredNrveValidator
};

export const requestRedemptionFormFormikUtil = {
  validationSchema: (props: TwoFactorEnabledOnAccountProps) => {
    return getValidationSchemaWithConditional2fa(props, baseValidators, 'twoFactorAuthCode');
  },
  mapPropsToValues: () => (initialValues)
};
