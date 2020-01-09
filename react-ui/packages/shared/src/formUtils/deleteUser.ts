import { password, getValidationSchemaWithConditional2fa, TwoFactorEnabledOnAccountProps } from './shared';

export interface DeleteUserFormValues {
  currentPassword: string;
  twoFactorAuthCode: string;
}

export const deleteUserFormInitialValues: DeleteUserFormValues = {
  currentPassword: '',
  twoFactorAuthCode: ''
};

const deleteUserFormValidators = {
  currentPassword: password
};

export const deleteUserFormFormikUtil = {
  validationSchema: (props: TwoFactorEnabledOnAccountProps) => {
    return getValidationSchemaWithConditional2fa(props, deleteUserFormValidators, 'twoFactorAuthCode');
  },
  mapPropsToValues: () => (deleteUserFormInitialValues)
};
