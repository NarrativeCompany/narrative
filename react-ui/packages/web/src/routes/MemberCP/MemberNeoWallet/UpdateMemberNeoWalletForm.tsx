import * as React from 'react';
import { compose } from 'recompose';
import { withCurrentUserTwoFactorAuthEnabled } from '../../../shared/containers/withCurrentUserTwoFactorAuthEnabled';
import {
  applyExceptionToState,
  initialFormState,
  withPutUserNeoWallet,
  WithPutUserNeoWalletProps,
  withState,
  PutUserNeoAddressFormValues,
  putUserNeoAddressFormFormikUtil,
  TwoFactorEnabledOnAccountProps,
  WithStateProps,
  SimpleFormState
} from '@narrative/shared';
import { FormikProps, withFormik } from 'formik';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import { FormMethodError } from '../../../shared/components/FormMethodError';
import { MemberNeoWalletMessages } from '../../../shared/i18n/MemberNeoWalletMessages';
import { openNotification } from '../../../shared/utils/notificationsUtil';
import {
  AccountVerificationFormWrapper,
  distanceBetweenAccountVerificationFields
} from '../../../shared/components/AccountVerificationFormWrapper';
import { FormField } from '../../../shared/components/FormField';
import { FormattedMessage } from 'react-intl';

interface ParentProps {
  dismiss: () => void;
}

type Props = ParentProps &
  TwoFactorEnabledOnAccountProps &
  FormikProps<PutUserNeoAddressFormValues> &
  WithStateProps<SimpleFormState> &
  InjectedIntlProps;

const UpdateMemberNeoWalletFormComponent: React.SFC<Props> = (props) => {
  const { twoFactorEnabled, state, isSubmitting } = props;

  return (
    <AccountVerificationFormWrapper
      title={MemberNeoWalletMessages.UpdateNeoWalletTitle}
      description={<FormattedMessage {...MemberNeoWalletMessages.UpdateNeoWalletDescription}/>}
      submitText={MemberNeoWalletMessages.SubmitNeoAddressBtnText}
      isSubmitting={isSubmitting}
      twoFactorEnabled={twoFactorEnabled}
    >

      <FormMethodError methodError={state.methodError} />

      <FormField.Input
        size="large"
        type="text"
        name="neoAddress"
        label={<FormattedMessage {...MemberNeoWalletMessages.NeoAddressLabel}/>}
        style={{marginBottom: distanceBetweenAccountVerificationFields}}
      />

    </AccountVerificationFormWrapper>
  );
};

export const UpdateMemberNeoWalletForm = compose(
  injectIntl,
  withCurrentUserTwoFactorAuthEnabled,
  withState<SimpleFormState>(initialFormState),
  withPutUserNeoWallet,
  withFormik<Props & WithPutUserNeoWalletProps, PutUserNeoAddressFormValues>({
    ...putUserNeoAddressFormFormikUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const input = {...values};
      const { setState, dismiss, isSubmitting, intl: { formatMessage } } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null, isSubmitting: true}));

      try {
        await props.putUserNeoWallet(input);

        dismiss();

        // Notify the user of success
        await openNotification.updateSuccess(
          {
            message: formatMessage(MemberNeoWalletMessages.NeoAddressUpdated),
            description: formatMessage(MemberNeoWalletMessages.NeoAddressWaitingPeriod),
            duration: null
          });

      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);

      } finally {
        setSubmitting(false);
        setState(ss => ({...ss, methodError: null, isSubmitting: undefined}));
      }
    }
  })
)(UpdateMemberNeoWalletFormComponent) as React.ComponentClass<ParentProps>;
