import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { displayName, username } from './shared/user';

export interface EditProfileFormValues {
  displayName: string;
  username: string;
}

export const editProfileInitialValues: EditProfileFormValues = {
  displayName: '',
  username: ''
};

export const editProfileValidationSchema: ObjectSchema<EditProfileFormValues> =
  yup.object({displayName, username});

export const editProfileFormikUtil = {
  validationSchema: editProfileValidationSchema,
  mapPropsToValues: (defaultValues: EditProfileFormValues) => ({...editProfileInitialValues, ...defaultValues})
};
