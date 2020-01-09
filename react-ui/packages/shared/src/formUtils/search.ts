import * as yup from 'yup';
import { ObjectSchema } from 'yup';

export const keyword = yup
  .string()
  .min(2)
  .max(255)
  .required();

export const filter = yup.string();
export const size = yup.number();
export const page = yup.number();

export interface SearchFormValues {
  keyword: string;
  filter: string;
  size: number;
  page: number;
}

export const searchInitialValues: SearchFormValues = {
  keyword: '',
  filter: '',
  size: 0,
  page: 0
};

export const searchValidationSchema: ObjectSchema<SearchFormValues> =
  yup.object({keyword, filter, size, page});

export const searchFormikUtil = {
  validationSchema: searchValidationSchema,
  mapPropsToValues: () => (searchInitialValues)
};
