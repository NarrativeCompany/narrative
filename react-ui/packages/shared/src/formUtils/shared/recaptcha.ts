import * as yup from 'yup';

export interface RecaptchaFormValues {
  recaptchaResponse: string;
}

export const recaptchaInitialValues: RecaptchaFormValues = {
   recaptchaResponse: '',
};

const recaptchaResponse = yup
  .string()
  .required();

export const recaptchaValidationSchema = {
  recaptchaResponse
};
