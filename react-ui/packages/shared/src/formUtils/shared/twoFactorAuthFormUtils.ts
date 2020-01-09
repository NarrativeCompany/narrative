import { verificationCodeValidator } from './user';
import * as yup from 'yup';

// jw: first, let's create a utility function to insert the 2FA field validator if needed.
function addConditional2faValidatedField(otherValidators: {}, fieldName: string, twoFactorEnabled?: boolean) {
  if (twoFactorEnabled) {
    return {
      ...otherValidators
      , [fieldName]: verificationCodeValidator
    };
  }

  return otherValidators;
}

// jw: now, let's define a interface for Props to use to provide what we need.
export interface TwoFactorEnabledOnAccountProps {
  twoFactorEnabled?: boolean;
}

// jw: finally, lets create a utility to generate the YUP object and bring all of this together.
export function getValidationSchemaWithConditional2fa(
  props: TwoFactorEnabledOnAccountProps,
  otherValidators: {},
  fieldName: string
) {
  return yup.object(addConditional2faValidatedField(otherValidators, fieldName, props.twoFactorEnabled));
}
