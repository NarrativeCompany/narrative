import * as yup from 'yup';

// TODO: localize yup validation messages #894
export const requiredNrveValidator = yup
  .string()
  // jw: borrowed from: https://stackoverflow.com/a/5917250/5656622
  .matches(/^(\d+|\d{1,3}(,\d{3})*)(\.\d+)?$/, 'must be in the format 1000.01 or 1,000.01')
  .required();
