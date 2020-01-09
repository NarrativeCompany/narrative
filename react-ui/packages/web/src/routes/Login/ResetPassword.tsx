import * as React from 'react';
import { branch, compose, lifecycle, renderComponent, withProps } from 'recompose';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import {
  applyExceptionToState,
  initialFormState,
  MethodError,
  resetPasswordFormFormikUtil,
  ResetPasswordFormValues,
  ResetPasswordInput,
  TwoFactorEnabledOnAccountProps,
  ValidateResetPasswordUrlInput,
  withResetPassword,
  WithResetPasswordProps,
  withState,
  WithStateProps,
  withValidateResetPasswordUrl,
  WithValidateResetPasswordUrlProps,
  PWResetURLValidationResult,
  omitProperties
} from '@narrative/shared';
import { FormikProps, withFormik } from 'formik';
import { FormMethodError } from '../../shared/components/FormMethodError';
import { RouteComponentProps, RouterProps, withRouter } from 'react-router';
import { openNotification } from '../../shared/utils/notificationsUtil';
import { WebRoute } from '../../shared/constants/routes';
import { InputField } from '../../shared/components/InputField';
import { TwoFactorLoginMessages } from '../../shared/i18n/TwoFactorLoginMessages';
import { ResetPasswordMessages } from '../../shared/i18n/ResetPasswordMessages';
import { AccountVerificationFormWrapper } from '../../shared/components/AccountVerificationFormWrapper';

interface State extends MethodError {
  isSubmitting?: boolean;
}

type ValidationProps = Pick<PWResetURLValidationResult, 'valid' | 'expired' | 'twoFactorEnabled'>;

type Props =
  ValidationProps &
  TwoFactorEnabledOnAccountProps &
  WithStateProps<MethodError> &
  WithStateProps<State> &
  InjectedIntlProps &
  RouterProps &
  RouteComponentProps<{userOid: string, timestamp: string, key: string}> &
  FormikProps<ResetPasswordFormValues>;

const ResetPasswordComponent: React.SFC<Props> = (props) => {
  const { state: { methodError, isSubmitting }, valid, twoFactorEnabled, intl: { formatMessage } } = props;

  if (!valid) {
    // todo:error-handling: Report to the server, because the lifecycle listener should have caught this
    return null;
  }

  return (
    <AccountVerificationFormWrapper
      title={ResetPasswordMessages.PageTitle}
      submitText={TwoFactorLoginMessages.SubmitBtnText}
      isSubmitting={isSubmitting}
      twoFactorEnabled={twoFactorEnabled}
      excludePassword={true}
      style={{marginTop: 15}}
    >

      <FormMethodError methodError={methodError}/>

      <InputField
        size="large"
        type="password"
        placeholder={formatMessage(ResetPasswordMessages.NewPasswordLabel)}
        label={formatMessage(ResetPasswordMessages.NewPasswordLabel)}
        name="newPassword"
        style={{ marginBottom: 10 }}
      />

      <InputField
        size="large"
        type="password"
        placeholder={formatMessage(ResetPasswordMessages.ConfirmNewPasswordLabel)}
        label={formatMessage(ResetPasswordMessages.ConfirmNewPasswordLabel)}
        name="confirmNewPassword"
        style={{ marginBottom: 10 }}
      />

    </AccountVerificationFormWrapper>
  );
};

export default compose(
  injectIntl,
  withRouter,
  withState<MethodError>(initialFormState),
  withProps((props: Props) => {
    const { match } = props;
    const params =
      match &&
      match.params;

    const input: ValidateResetPasswordUrlInput = {
      timestamp: params.timestamp,
      resetPasswordKey: params.key
    };

    return ({
      input,
      userOid: params.userOid,
    });
  }),
  // Validate the URL args
  withValidateResetPasswordUrl,
  branch<WithValidateResetPasswordUrlProps>((props) => (
    props.validateResetPasswordUrlData && props.validateResetPasswordUrlData.loading),
    renderComponent(() => null)
  ),
  withProps<ValidationProps, WithValidateResetPasswordUrlProps>((props): ValidationProps => {
    const { validateResetPasswordUrlData } = props;

    return omitProperties(validateResetPasswordUrlData.validateResetPasswordUrl, ['__typename']) as ValidationProps;
  }),
  lifecycle<ValidationProps & RouterProps & InjectedIntlProps, {}>({
    // tslint:disable-next-line object-literal-shorthand
    componentDidMount: async function () {
      const { valid, expired, history: { push }, intl: { formatMessage } } = this.props;

      if (!valid) {
        // If there is an error, the reset URL is invalid so go home and display a message
        push(WebRoute.Home);

        const msg = expired ? ResetPasswordMessages.ExpiredURLMessage : ResetPasswordMessages.BadURLMessage;

        openNotification.updateFailed(
          undefined,
          {
            description: '',
            message: formatMessage(msg),
            duration: 0
          });
      }
    }
  }),
  withResetPassword,
  withFormik<Props & WithResetPasswordProps & WithValidateResetPasswordUrlProps, ResetPasswordFormValues>({
    ...resetPasswordFormFormikUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { match, setState, history, isSubmitting, resetPassword } = props;

      if (isSubmitting) {
        return;
      }

      const params =
        match &&
        match.params;

      setState(ss => ({ ...ss, methodError: null, isSubmitting: true }));

      try {
        const input: ResetPasswordInput = {
          password: values.newPassword,
          passwordConfirm: values.confirmNewPassword,
          twoFactorAuthCode: values.twoFactorAuthCode,
          resetPasswordKey: params.key,
          timestamp: params.timestamp,
        };

        await resetPassword({ input, userOid: params.userOid });

        // Success!  Go home and display a success message
        history.push(WebRoute.Home);

        openNotification.updateSuccess(
          {
            description: '',
            message: props.intl.formatMessage(ResetPasswordMessages.SuccessMessage),
            duration: 0
          });
      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);

      } finally {
        setState(ss => ({ ...ss, isSubmitting: undefined }));
        setSubmitting(false);
      }
    }
  })
)(ResetPasswordComponent) as React.ComponentClass<{}>;
