import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import {
  PublicationFormValues,
  sharedPublicationFieldValidators
} from './shared';

export interface CreatePublicationFormValues extends PublicationFormValues {
  agreedToAup: boolean;
}

export const createPublicationInitialValues: CreatePublicationFormValues = {
  agreedToAup: false,
  description: '',
  name: '',
  logo: {}
};

// TODO: localize yup validation messages #894
const agreedToAup = yup
  .boolean()
  .oneOf([true], 'You must agree to the AUP.')
  .required();

export const createPublicationValidationSchema: ObjectSchema<CreatePublicationFormValues> =
  yup.object({
    ...sharedPublicationFieldValidators,
    agreedToAup
  });

export const createPublicationFormUtil = {
  validationSchema: createPublicationValidationSchema,
  mapPropsToValues: () => (createPublicationInitialValues)
};
