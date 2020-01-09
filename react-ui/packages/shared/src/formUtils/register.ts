import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { booleanValidator, displayName, emailAddress, password, username } from './shared/user';
import {
  RecaptchaFormValues,
  recaptchaInitialValues,
  recaptchaValidationSchema
} from './shared/recaptcha';

export interface RegisterFormValues extends RecaptchaFormValues {
  emailAddress: string;
  password: string;
  displayName: string;
  username: string;
  hasAgreedToTos: boolean;
  recaptchaToken: string;
  nichesToFollow: string[];
}

export const registerInitialValues: RegisterFormValues = {
  emailAddress: '',
  password: '',
  displayName: '',
  username: '',
  hasAgreedToTos: false,
  ...recaptchaInitialValues,
  recaptchaToken: '',
  nichesToFollow: []
};

const hasAgreedToTos = booleanValidator
  .oneOf([true], 'You must agree to the terms of service')
  .required();

const recaptchaToken = yup
  .string()
  .required();

const nichesToFollow = yup
  .array()
  .of(yup.string());

export const registerValidationSchema: ObjectSchema<RegisterFormValues> =
  yup.object({
    emailAddress,
    password,
    displayName,
    username,
    hasAgreedToTos,
    ...recaptchaValidationSchema,
    recaptchaToken,
    nichesToFollow
});

export const registerFormUtil = {
  validationSchema: registerValidationSchema,
  mapPropsToValues: (defaultEmailAddress: string) => ({...registerInitialValues, emailAddress: defaultEmailAddress})
};
