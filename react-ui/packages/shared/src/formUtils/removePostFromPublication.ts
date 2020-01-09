import * as yup from 'yup';
import { ObjectSchema } from 'yup';

export interface RemovePostFromPublicationFormValues {
  message?: string;
}

const message = yup
  .string();

const validationSchema: ObjectSchema<RemovePostFromPublicationFormValues> =
  yup.object({
    message
  });

export const removePostFromPublicationFormikUtil = {
  validationSchema,
  mapPropsToValues: () => ({})
};
