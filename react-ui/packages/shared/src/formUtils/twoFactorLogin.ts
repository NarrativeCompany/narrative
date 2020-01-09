import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { verificationCodeValidator } from './shared/user';

const booleanValidator = yup.boolean();

export interface TwoFactorLoginFormValues {
  verificationCode: string;
  rememberMe: boolean;
}

export const twoFactorLoginFormInitialValues: TwoFactorLoginFormValues = {
  verificationCode: '',
  rememberMe: false
};

export const twoFactorLoginValidationSchema: ObjectSchema<TwoFactorLoginFormValues> =
  yup.object({verificationCode: verificationCodeValidator, rememberMe: booleanValidator});

export const twoFactorLoginFormikUtil = {
  validationSchema: twoFactorLoginValidationSchema,
  mapPropsToValues: () => (twoFactorLoginFormInitialValues)
};
