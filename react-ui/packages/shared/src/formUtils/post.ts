import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { booleanValidator } from './shared/user';
import { PostInput } from '../types';

export const postInitialValues = {
  title: '',
  subTitle: null,
  body: '',
  canonicalUrl: '',
  publishToPrimaryChannel: undefined,
  ageRestricted: null,
  disableComments: false,
  draft: true,
  publishToNiches: []
};

export const MAX_CANONICAL_URL_LENGTH = 255;

const title = yup
  .string();
const subTitle = yup
  .string()
  .nullable(true);
const body = yup
  .string();
const canonicalUrl = yup
  .string()
  .nullable()
  .max(MAX_CANONICAL_URL_LENGTH)
  .url();
const publishToPrimaryChannel = yup
  .string();

const publishToNiches = yup
  .array()
  .of(yup.string());
const ageRestricted = booleanValidator.nullable(true);
const disableComments = booleanValidator;
const draft = booleanValidator;

export const postValidationSchema: ObjectSchema<PostInput> =
  yup.object({
    title,
    subTitle,
    body,
    canonicalUrl,
    publishToPrimaryChannel,
    publishToNiches,
    ageRestricted,
    disableComments,
    draft
  });

export const postFormUtil = {
  validationSchema: postValidationSchema,
  mapPropsToValues: (defaultValues?: PostInput) => Object.assign(postInitialValues, defaultValues)
};
