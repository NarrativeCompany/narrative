import {
  getValidationSchemaWithConditional2fa,
  password,
  TwoFactorEnabledOnAccountProps
} from './shared';
import { pw2FACodeFormInitialValues, PW2FACodeFormValues } from './pw2faCode';
import * as yup from 'yup';

export interface ChangePublicationOwnerFormValues extends PW2FACodeFormValues {
  userOid?: string;
}

const initialValues: ChangePublicationOwnerFormValues = {
  ...pw2FACodeFormInitialValues,
  userOid: undefined
};

// TODO: localize yup validation messages #894
const userOid = yup
  .string()
  .required('You must select a new Owner.');

const baseValidators = {
  currentPassword: password,
  userOid
};

export const changePublicationOwnerFormFormikUtil = {
  validationSchema: (props: TwoFactorEnabledOnAccountProps) => {
    return getValidationSchemaWithConditional2fa(props, baseValidators, 'twoFactorAuthCode');
  },
  mapPropsToValues: () => (initialValues)
};
