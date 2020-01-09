import * as yup from 'yup';

export const MAX_USERNAME_LENGTH = 20;
export const MAX_DISPLAY_NAME_LENGTH = 40;
export const MAX_EMAIL_ADDRESS_LENGTH = 255;
export const MAX_PASSWORD_LENGTH = 40;

export const displayName = yup
  .string()
  .min(1)
  .max(MAX_DISPLAY_NAME_LENGTH)
  .required();

export const username = yup
  .string()
  .min(3)
  .max(MAX_USERNAME_LENGTH)
  // TODO: localize yup validation messages #894
  .matches(/^[a-zA-Z0-9_]*$/, 'Handle can only contain letters, numbers, and underscores.')
  .required();

export const emailAddress = yup
  .string()
  .min(6)
  .max(MAX_EMAIL_ADDRESS_LENGTH)
  .email()
  .required();

export const confirmEmailAddress = yup
  .string()
  .email()
  .min(6)
  .max(MAX_EMAIL_ADDRESS_LENGTH)
  .test('match',
    // TODO: localize yup validation messages #894
    'Email addresses must match.',
    function(confirmEmailAddressInput: string) {
      return confirmEmailAddressInput === this.parent.emailAddress;
    })
  .required();

export const confirmNewPassword = yup
  .string()
  .min(8)
  .max(MAX_PASSWORD_LENGTH)
  .test('match',
    // TODO: localize yup validation messages #894
    'Passwords must match.',
    function(confirmNewPasswordInput: string) {
      return confirmNewPasswordInput === this.parent.newPassword;
    })
  .required();

export const password = yup
  .string()
  .min(8)
  .max(MAX_PASSWORD_LENGTH)
  .required();

// TODO: localize yup validation messages #894
export const verificationCodeValidator = yup
  .string()
  .matches(/^[0-9]{6}$/, 'Code must be exactly 6 numeric digits.')
  .required();

export const booleanValidator = yup
  .boolean();
