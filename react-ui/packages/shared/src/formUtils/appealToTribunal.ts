import * as yup from 'yup';
import { ObjectSchema } from 'yup';

export interface AppealToTribunalFormValues {
  comment: string;
}

const appealToTribunalInitialValues: AppealToTribunalFormValues = {
  comment: '',
};

const comment = yup
  .string()
  .required();

export const appealToTribunalSchema: ObjectSchema<AppealToTribunalFormValues> = yup.object({comment});

export const appealToTribunalFormikUtil = {
  validationSchema: appealToTribunalSchema,
  mapPropsToValues: () => (appealToTribunalInitialValues)
};
