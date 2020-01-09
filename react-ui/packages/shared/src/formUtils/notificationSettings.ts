import * as yup from 'yup';
import { BooleanSchema, ObjectSchema } from 'yup';

export interface UserNotificationSettingsFormValues {
  notifyWhenFollowed: boolean;
  notifyWhenMentioned: boolean;
  suspendAllEmails: boolean;
}

// jw: most of these fields are required, so let's create that validator first.
const requiredBooleanValidator: BooleanSchema = yup
  .boolean()
  .required();

// jw: next, let's setup the validator for these fields!
const updateNotificationSettingsValidationSchema: ObjectSchema<UserNotificationSettingsFormValues> = yup.object({
  notifyWhenFollowed: requiredBooleanValidator,
  notifyWhenMentioned: requiredBooleanValidator,
  suspendAllEmails: requiredBooleanValidator
});

export const notificationSettingsUtil = {
  validationSchema: updateNotificationSettingsValidationSchema,
  mapPropsToValues: (defaultValues: UserNotificationSettingsFormValues) =>
    ({...defaultValues})
};
