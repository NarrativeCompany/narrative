import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { booleanValidator, password, verificationCodeValidator } from './shared/user';

export interface PW2FACodeRememberMeFormValues {
  currentPassword: string;
  twoFactorAuthCode: string;
  rememberMe?: boolean;
}

export const pw2FACodeRememberMeFormInitialValues: PW2FACodeRememberMeFormValues = {
  currentPassword: '',
  twoFactorAuthCode: '',
  rememberMe: undefined
};

export const pw2FACodeRememberMeFormValidationSchema: ObjectSchema<PW2FACodeRememberMeFormValues> =
  yup.object(
    {
      currentPassword: password,
      twoFactorAuthCode: verificationCodeValidator,
      rememberMe: booleanValidator
    });

export const pw2FACodeRememberMeFormFormikUtil = {
  validationSchema: pw2FACodeRememberMeFormValidationSchema,
  mapPropsToValues: () => (pw2FACodeRememberMeFormInitialValues)
};
