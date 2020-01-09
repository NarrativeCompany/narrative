import * as React from 'react';
import { Modal } from 'antd';
import { compose } from 'recompose';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { FormikProps, withFormik } from 'formik';
import { InputField } from '../../../shared/components/InputField';
import { TwoFactorLoginMessages } from '../../../shared/i18n/TwoFactorLoginMessages';
import { ChangeEmailAddressMessages } from '../../../shared/i18n/ChangeEmailAddressMessages';
import {
  applyExceptionToState,
  changeEmailAddressFormFormikUtil,
  ChangeEmailAddressFormValues,
  initialFormState,
  MethodError,
  TwoFactorEnabledOnAccountProps,
  withState,
  WithStateProps,
  withUpdateAuthState,
  WithUpdateAuthStateProps,
  withUpdateEmailAddress,
  WithUpdateEmailAddressProps
} from '@narrative/shared';
import { FormMethodError } from '../../../shared/components/FormMethodError';
import { withCurrentUserTwoFactorAuthEnabled } from '../../../shared/containers/withCurrentUserTwoFactorAuthEnabled';
import {
  AccountVerificationFormWrapper,
  distanceBetweenAccountVerificationFields
} from '../../../shared/components/AccountVerificationFormWrapper';
import { openNotification } from '../../../shared/utils/notificationsUtil';

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
  FormikProps<ChangeEmailAddressFormValues>;

const ChangeEmailAddressModalComponent: React.SFC<Props> = (props) => {
  const { visible, dismiss, twoFactorEnabled, state, isSubmitting, intl: { formatMessage } } = props;

  return (
    <Modal
      visible={visible}
      onCancel={dismiss}
      footer={null}
      destroyOnClose={true}
    >

      <AccountVerificationFormWrapper
        title={ChangeEmailAddressMessages.PageTitle}
        submitText={TwoFactorLoginMessages.SubmitBtnText}
        isSubmitting={isSubmitting}
        twoFactorEnabled={twoFactorEnabled}
      >

        <FormMethodError methodError={state.methodError} />

        <InputField
          size="large"
          type="email"
          placeholder={formatMessage(ChangeEmailAddressMessages.NewEmailAddressLabel)}
          label={formatMessage(ChangeEmailAddressMessages.NewEmailAddressLabel)}
          name="emailAddress"
          style={{marginBottom: distanceBetweenAccountVerificationFields}}
        />

        <InputField
          size="large"
          type="email"
          placeholder={formatMessage(ChangeEmailAddressMessages.ConfirmNewEmailAddressLabel)}
          label={formatMessage(ChangeEmailAddressMessages.ConfirmNewEmailAddressLabel)}
          name="confirmEmailAddress"
          style={{marginBottom: distanceBetweenAccountVerificationFields}}
        />

      </AccountVerificationFormWrapper>

    </Modal>
  );
};

export const ChangeEmailAddressModal = compose(
  injectIntl,
  withUpdateAuthState,
  withCurrentUserTwoFactorAuthEnabled,
  withState<MethodError>(initialFormState),
  withUpdateEmailAddress,
  withFormik<Props & WithUpdateEmailAddressProps & WithUpdateAuthStateProps, ChangeEmailAddressFormValues>({
    ...changeEmailAddressFormFormikUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const input = {...values};
      const { setState, dismiss, isSubmitting, intl: { formatMessage } } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        const result = await props.updateEmailAddress({ input });

        // Return control to the parent
        dismiss();

        const currentEmailAddress = result.emailAddress;
        const newEmailAddress = result.pendingEmailAddress;

        openNotification.updateSuccess({
          message: formatMessage(ChangeEmailAddressMessages.EmailChangeRequested),
          description: formatMessage(
            ChangeEmailAddressMessages.EmailChangeRequestedMessage,
            {currentEmailAddress, newEmailAddress}
          ),
          duration: null
        });

      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);
      }

      setSubmitting(false);
    },
  })
)(ChangeEmailAddressModalComponent) as React.ComponentClass<ParentProps>;
