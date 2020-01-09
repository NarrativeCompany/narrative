import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { booleanValidator, emailAddress, password } from './shared/user';
import {
  RecaptchaFormValues,
  recaptchaInitialValues,
  recaptchaValidationSchema
} from './shared/recaptcha';

export interface LoginFormValues extends RecaptchaFormValues {
  emailAddress: string;
  password: string;
  rememberMe: boolean;
}

export const loginInitialValues: LoginFormValues = {
  emailAddress: '',
  password: '',
  rememberMe: true,
  ...recaptchaInitialValues
};

export const loginValidationSchema: ObjectSchema<LoginFormValues> =
  yup.object({emailAddress, password, rememberMe: booleanValidator, ...recaptchaValidationSchema});

export const loginFormikUtil = {
  validationSchema: loginValidationSchema,
  mapPropsToValues: () => (loginInitialValues)
};
