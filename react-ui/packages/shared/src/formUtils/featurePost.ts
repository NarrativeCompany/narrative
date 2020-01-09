import * as yup from 'yup';
import { FeaturePostDuration } from '../types';
import { ObjectSchema } from 'yup';

export interface FeaturePostFormValues {
  duration: FeaturePostDuration;
}

// TODO: localize yup validation messages #894
const durationValidator = yup
  .mixed()
  .oneOf([
    FeaturePostDuration.ONE_DAY,
    FeaturePostDuration.THREE_DAYS,
    FeaturePostDuration.ONE_WEEK,
  ], 'Duration is a required field')
  .required();

const initialValues: FeaturePostFormValues = {
  duration: FeaturePostDuration.ONE_DAY
};

const validationSchema: ObjectSchema<FeaturePostFormValues> =
  yup.object({
    duration: durationValidator
  });

export const featurePostFormikUtil = {
  validationSchema,
  mapPropsToValues: () => (initialValues)
};
