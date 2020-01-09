import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { neoAddressRequiredValidator } from './shared';

export interface NeoAddressValues {
  neoAddress: string;
}

// jw: next, let's setup the validator for these fields!
const putNeoAddressValidationSchema: ObjectSchema<NeoAddressValues> = yup.object({
  neoAddress: neoAddressRequiredValidator
});

export const putNeoAddressUtil = {
  validationSchema: putNeoAddressValidationSchema
};
