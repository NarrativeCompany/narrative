import * as yup from 'yup';
import { ObjectSchema } from 'yup';

export const maxNicheNameLength = 60;

// TODO: localize yup validation messages #894
const name = yup
  .string()
  .min(3)
  .max(maxNicheNameLength)
  .required();

const description = yup
  .string()
  .min(10)
  .max(256)
  .required();

export interface NicheDetailsFormValues {
  name: string;
  description: string;
}

export const nicheDetailsInitialValues: NicheDetailsFormValues = {
  name: '',
  description: '',
};

export const nicheDetailsValidationSchema: ObjectSchema<NicheDetailsFormValues> =
  yup.object({name, description});

export const nicheDetailsFormUtil = {
  validationSchema: nicheDetailsValidationSchema,
};
