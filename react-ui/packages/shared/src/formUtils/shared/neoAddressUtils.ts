import * as yup from 'yup';

const neoAddressLength = 34;

export const neoAddressValidator = yup
  .string()
  .min(neoAddressLength)
  .max(neoAddressLength);

export const neoAddressRequiredValidator = yup
  .string()
  .min(neoAddressLength)
  .max(neoAddressLength)
  .required();
