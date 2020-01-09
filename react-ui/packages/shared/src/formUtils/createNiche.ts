import * as yup from 'yup';
import { ObjectSchema } from 'yup';

export interface CreateNicheFormValues {
  assertChecked: boolean;
  agreeChecked: boolean;
}

export const createNicheInitialValues: CreateNicheFormValues = {
  assertChecked: false,
  agreeChecked: false,
};

// TODO: localize yup validation messages #894
const assertChecked = yup
  .boolean()
  .oneOf([true], 'Verification is required')
  .required();
const agreeChecked = yup
  .boolean()
  .oneOf([true], 'Agreement is required')
  .required();

export const createNicheValidationSchema: ObjectSchema<CreateNicheFormValues> =
  yup.object({assertChecked, agreeChecked});

export const createNicheFormUtil = {
  validationSchema: createNicheValidationSchema,
  mapPropsToValues: () => (createNicheInitialValues)
};
