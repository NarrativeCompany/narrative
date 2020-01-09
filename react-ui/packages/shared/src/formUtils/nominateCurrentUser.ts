import * as yup from 'yup';
import { ObjectSchema } from 'yup';

export interface NominateCurrentUserFormValues {
  personalStatement?: string;
}

const nominateCurrentUserInitialValues: NominateCurrentUserFormValues = {
  personalStatement: undefined
};

const personalStatement = yup
  .string()
  .min(1)
  .max(140);

const nominateCurrentUserSchema: ObjectSchema<NominateCurrentUserFormValues> =
  yup.object({ personalStatement });

export const nominateCurrentUserFormikUtil = {
  validationSchema: nominateCurrentUserSchema,
  mapPropsToValues: (initialValues: NominateCurrentUserFormValues) =>
    ({ ...nominateCurrentUserInitialValues, ...initialValues })
};
