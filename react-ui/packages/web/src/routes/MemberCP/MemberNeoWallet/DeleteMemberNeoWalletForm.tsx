import * as React from 'react';
import { compose } from 'recompose';
import { withCurrentUserTwoFactorAuthEnabled } from '../../../shared/containers/withCurrentUserTwoFactorAuthEnabled';
import {
  applyExceptionToState,
  initialFormState,
  MethodError,
  withDeleteUserNeoWallet,
  WithDeleteUserNeoWalletProps,
  withState,
  DeleteUserNeoAddressFormValues,
  deleteUserNeoAddressFormFormikUtil,
  TwoFactorEnabledOnAccountProps,
  WithStateProps
} from '@narrative/shared';
import { FormikProps, withFormik } from 'formik';
import { injectIntl, InjectedIntlProps, FormattedMessage } from 'react-intl';
import { FormMethodError } from '../../../shared/components/FormMethodError';
import { MemberNeoWalletMessages } from '../../../shared/i18n/MemberNeoWalletMessages';
import { openNotification } from '../../../shared/utils/notificationsUtil';
import { AccountVerificationFormWrapper } from '../../../shared/components/AccountVerificationFormWrapper';

interface State extends MethodError {
  isSubmitting?: boolean;
}

interface ParentProps {
  dismiss: () => void;
}

type Props = ParentProps &
  TwoFactorEnabledOnAccountProps &
  FormikProps<DeleteUserNeoAddressFormValues> &
  WithStateProps<State> &
  InjectedIntlProps;

const DeleteMemberNeoWalletFormComponent: React.SFC<Props> = (props) => {
  const { twoFactorEnabled, state, isSubmitting } = props;

  return (
    <AccountVerificationFormWrapper
      title={MemberNeoWalletMessages.DeleteNeoWalletTitle}
      description={<FormattedMessage {...MemberNeoWalletMessages.UpdateNeoWalletDescription}/>}
      submitText={MemberNeoWalletMessages.DeleteNeoAddressBtnText}
      isSubmitting={isSubmitting}
      twoFactorEnabled={twoFactorEnabled}
    >

      <FormMethodError methodError={state.methodError} />

    </AccountVerificationFormWrapper>
  );
};

export const DeleteMemberNeoWalletForm = compose(
  injectIntl,
  withCurrentUserTwoFactorAuthEnabled,
  withState<State>(initialFormState),
  withDeleteUserNeoWallet,
  withFormik<Props & WithDeleteUserNeoWalletProps, DeleteUserNeoAddressFormValues>({
    ...deleteUserNeoAddressFormFormikUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const input = {...values};
      const { setState, dismiss, isSubmitting, intl: { formatMessage } } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null, isSubmitting: true}));

      try {
        await props.deleteUserNeoWallet(input);

        dismiss();

        // Notify the user of success
        await openNotification.updateSuccess(
          {
            message: formatMessage(MemberNeoWalletMessages.NeoAddressDeleted),
            description: null
          });

      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);

      } finally {
        setSubmitting(false);
        setState(ss => ({...ss, methodError: null, isSubmitting: undefined}));
      }

    },
  }),
)(DeleteMemberNeoWalletFormComponent) as React.ComponentClass<ParentProps>;
