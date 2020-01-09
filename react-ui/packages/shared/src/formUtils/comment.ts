import * as yup from 'yup';
import { ObjectSchema } from 'yup';

export interface PostCommentFormValues {
  body: string;
}

// TODO: localize yup validation messages #894
const body = yup
  .string()
  .required();

export const commentValidationSchema: ObjectSchema<PostCommentFormValues> =
  yup.object({ body });

export const commentFormUtil = {
  validationSchema: commentValidationSchema
};
