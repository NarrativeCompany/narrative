import * as React from 'react';
import { Modal } from 'antd';
import { compose } from 'recompose';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { withFormik, FormikProps } from 'formik';
import { InputField } from '../../../shared/components/InputField';
import { TwoFactorLoginMessages } from '../../../shared/i18n/TwoFactorLoginMessages';
import {
  applyExceptionToState,
  MethodError,
  initialFormState,
  AuthenticationState,
  WithCurrentUserTwoFactorAuthStateProps,
  withState,
  WithStateProps,
  withUpdateAuthState,
  WithUpdateAuthStateProps,
  withUpdatePassword,
  WithUpdatePasswordProps,
  changePasswordFormFormikUtil,
  ChangePasswordFormValues,
  TwoFactorEnabledOnAccountProps
} from '@narrative/shared';
import { storeAuthToken } from '../../../shared/utils/authTokenUtils';
import { ChangePasswordMessages } from '../../../shared/i18n/ChangePasswordMessages';
import { FormMethodError } from '../../../shared/components/FormMethodError';
import { withCurrentUserTwoFactorAuthEnabled } from '../../../shared/containers/withCurrentUserTwoFactorAuthEnabled';
import {
  AccountVerificationFormWrapper,
  distanceBetweenAccountVerificationFields
} from '../../../shared/components/AccountVerificationFormWrapper';

interface ParentProps {
  // tslint:disable-next-line no-any
  dismiss: () => any;
  visible: boolean;
}

type Props =
  TwoFactorEnabledOnAccountProps &
  WithStateProps<MethodError> &
  ParentProps &
  InjectedIntlProps &
  WithCurrentUserTwoFactorAuthStateProps &
  WithUpdateAuthStateProps &
  WithUpdatePasswordProps &
  FormikProps<ChangePasswordFormValues>;

const ChangePasswordModalComponent: React.SFC<Props> = (props) => {
  const { visible, dismiss, twoFactorEnabled, state, isSubmitting, intl: { formatMessage } } = props;

  return (
    <Modal
      visible={visible}
      onCancel={dismiss}
      footer={null}
      destroyOnClose={true}
    >

      <AccountVerificationFormWrapper
        title={ChangePasswordMessages.PageTitle}
        submitText={TwoFactorLoginMessages.SubmitBtnText}
        customPasswordLabel={ChangePasswordMessages.CurrentPasswordLabel}
        isSubmitting={isSubmitting}
        twoFactorEnabled={twoFactorEnabled}
      >

        <FormMethodError methodError={state.methodError} />

        <InputField
          size="large"
          type="password"
          placeholder={formatMessage(ChangePasswordMessages.NewPasswordLabel)}
          label={formatMessage(ChangePasswordMessages.NewPasswordLabel)}
          name="newPassword"
          style={{marginBottom: distanceBetweenAccountVerificationFields}}
        />

        <InputField
          size="large"
          type="password"
          placeholder={formatMessage(ChangePasswordMessages.ConfirmNewPasswordLabel)}
          label={formatMessage(ChangePasswordMessages.ConfirmNewPasswordLabel)}
          name="confirmNewPassword"
          style={{marginBottom: distanceBetweenAccountVerificationFields}}
        />

      </AccountVerificationFormWrapper>

    </Modal>
  );
};

export const ChangePasswordModal = compose(
  injectIntl,
  withUpdateAuthState,
  withCurrentUserTwoFactorAuthEnabled,
  withState<MethodError>(initialFormState),
  withUpdatePassword,
  withFormik<Props, ChangePasswordFormValues>({
    ...changePasswordFormFormikUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const input = {...values};
      const { setState, dismiss, updateAuthenticationState, isSubmitting } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        const { token } = await props.updatePassword({ input });

        // if payload has the token, pass the token to AuthStore
        if (token) {
          // Store the new token and update the auth state
          storeAuthToken(token, false);
          await updateAuthenticationState({ authenticationState: AuthenticationState.USER_AUTHENTICATED });
          // Return control to the parent
          dismiss();
        }
      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);
      }

      setSubmitting(false);
    },
  })
)(ChangePasswordModalComponent) as React.ComponentClass<ParentProps>;
