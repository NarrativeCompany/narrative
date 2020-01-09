import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { password, verificationCodeValidator } from './shared/user';

// jw: resetting a password does not require a password since the user likely forgot theirs and are, using an email link
//     to perform the reset. With that in mind, let's split this up.
export interface TwoFACodeFormValues {
  twoFactorAuthCode?: string;
}

export interface PW2FACodeFormValues extends TwoFACodeFormValues {
  currentPassword: string;
}

export const twoFactorAuthCodeFormInitialValues: TwoFACodeFormValues = {
  twoFactorAuthCode: undefined
};

export const pw2FACodeFormInitialValues: PW2FACodeFormValues = {
  ...twoFactorAuthCodeFormInitialValues,
  currentPassword: ''
};

export const pw2FACodeFormValidationSchema: ObjectSchema<PW2FACodeFormValues> =
  yup.object({currentPassword: password, twoFactorAuthCode: verificationCodeValidator});

export const pw2FACodeFormFormikUtil = {
  validationSchema: pw2FACodeFormValidationSchema,
  mapPropsToValues: () => (pw2FACodeFormInitialValues)
};
