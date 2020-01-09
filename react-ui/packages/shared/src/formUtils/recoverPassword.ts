import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { emailAddress } from './shared/user';
import {
  RecaptchaFormValues,
  recaptchaInitialValues,
  recaptchaValidationSchema
} from './shared/recaptcha';

export interface RecoverPasswordFormValues extends RecaptchaFormValues {
  emailAddress: string;
}

export const recoverPasswordInitialValues: RecoverPasswordFormValues = {
  emailAddress: '',
  ...recaptchaInitialValues
};

export const recoverPasswordValidationSchema: ObjectSchema<RecoverPasswordFormValues> =
  yup.object({emailAddress, ...recaptchaValidationSchema});

export const recoverPasswordFormFormikUtil = {
  validationSchema: recoverPasswordValidationSchema,
  mapPropsToValues: () => (recoverPasswordInitialValues)
};
