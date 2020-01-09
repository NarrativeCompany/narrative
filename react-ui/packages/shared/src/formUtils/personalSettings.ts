import * as yup from 'yup';
import { BooleanSchema, ObjectSchema } from 'yup';
import { QualityFilter } from '../types';

export interface UserPersonalSettingsFormValues {
  qualityFilter: QualityFilter;
  displayAgeRestrictedContent: boolean;
  hideMyFollowers: boolean;
  hideMyFollows: boolean;
}

const qualityValidator = yup
  .mixed()
  .oneOf([
    QualityFilter.HIDE_LOW_QUALITY,
    QualityFilter.ANY_QUALITY,
    QualityFilter.ONLY_HIGH_QUALITY
  ], 'Quality is a required field')
  .required();

// jw: most of these fields are required, so let's create that validator first.
const requiredBooleanValidator: BooleanSchema = yup
  .boolean()
  .required();

// jw: next, let's setup the validator for these fields!
const updatePersonalSettingsValidationSchema: ObjectSchema<UserPersonalSettingsFormValues> = yup.object({
  qualityFilter: qualityValidator,
  displayAgeRestrictedContent: requiredBooleanValidator,
  hideMyFollowers: requiredBooleanValidator,
  hideMyFollows: requiredBooleanValidator,
});

export const personalSettingsUtil = {
  validationSchema: updatePersonalSettingsValidationSchema,
  mapPropsToValues: (defaultValues: UserPersonalSettingsFormValues) =>
    ({...defaultValues})
};
