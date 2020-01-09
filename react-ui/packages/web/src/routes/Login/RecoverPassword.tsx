import * as React from 'react';
import { Col, Icon, Row } from 'antd';
import { compose } from 'recompose';
import { withExtractedAuthState } from '../../shared/containers/withExtractedAuthState';
import { Logo } from '../../shared/components/Logo';
import { Heading } from '../../shared/components/Heading';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { DescriptionParagraph } from '../MemberCP/settingsStyles';
import {
  applyExceptionToState,
  RecoverPasswordFormValues,
  RecoverPasswordInput,
  recoverPasswordFormFormikUtil,
  withRecoverPassword,
  WithRecoverPasswordProps,
  withState,
  WithStateProps,
  MethodError,
  initialFormState
} from '@narrative/shared';
import { RecoverPasswordMessages } from '../../shared/i18n/RecoverPasswordMessage';
import { convertInputFieldAddon } from '../../shared/utils/convertInputAddon';
import { RegisterMessages } from '../../shared/i18n/RegisterMessages';
import { withFormik, FormikProps } from 'formik';
import { FormMethodError } from '../../shared/components/FormMethodError';
import { FormControl } from '../../shared/components/FormControl';
import { RouterProps, withRouter } from 'react-router';
import { openNotification } from '../../shared/utils/notificationsUtil';
import { WebRoute } from '../../shared/constants/routes';
import { AuthForm, AuthWrapper } from '../../shared/styled/shared/auth';
import { InputField } from '../../shared/components/InputField';
import { Button } from '../../shared/components/Button';
import { FlexContainer } from '../../shared/styled/shared/containers';
import { RecaptchaHandlers, resetGrecaptcha, withRecaptchaHandlers } from '../../shared/utils/recaptchaUtils';
import { Recaptcha } from '../../shared/components/Recaptcha';

interface ParentProps {
  // tslint:disable-next-line no-any
  dismiss?: () => any;
}

interface WithHandlers {
  // tslint:disable-next-line no-any
  handleRecoverPassword: () => any;
}

type Props =
  ParentProps &
  WithStateProps<MethodError> &
  WithHandlers &
  InjectedIntlProps &
  RouterProps &
  FormikProps<RecoverPasswordFormValues> &
  RecaptchaHandlers;

const RecoverPasswordComponent: React.SFC<Props> = (props) => {
  const {
    state,
    intl,
    isSubmitting,
    handleRecaptchaVerifyCallback,
    handleRecaptchaExpiredCallback,
    errors,
    touched
  } = props;

  const recaptchaError = touched.recaptchaResponse && errors.recaptchaResponse ?
    errors.recaptchaResponse as string :
    undefined;

  return (
    <AuthWrapper centerAll={true} column={true}>

      <AuthForm>

        <Row type="flex" align="middle" justify="center" style={{paddingBottom: 25}}>
          <Col>
            <Logo/>
          </Col>
        </Row>

        <FlexContainer centerAll={true} column={true} style={{paddingBottom: 25}}>

          <Heading size={3}>
            <FormattedMessage {...RecoverPasswordMessages.FormTitle}/>
          </Heading>

          <DescriptionParagraph textAlign="center">
            <FormattedMessage {...RecoverPasswordMessages.Description}/>
          </DescriptionParagraph>

          <FormMethodError methodError={state.methodError} />

          <InputField
            name="emailAddress"
            prefix={convertInputFieldAddon(<Icon type="mail"/>)}
            size="large"
            type="email"
            placeholder={intl.formatMessage(RegisterMessages.EmailFieldLabel)}
            style={{marginTop: 24, width: 325}}
          />

          <Recaptcha
            verifyCallback={handleRecaptchaVerifyCallback}
            expiredCallback={handleRecaptchaExpiredCallback}
            error={recaptchaError}
            style={{marginBottom: 24, width: 325}}
          />

          <FormControl>
            <Button
              type="primary"
              htmlType="submit"
              size="large"
              loading={isSubmitting}
              style={{width: 325}}
              >
              <FormattedMessage {...RecoverPasswordMessages.SubmitLabel}/>
            </Button>
          </FormControl>

        </FlexContainer>

      </AuthForm>

    </AuthWrapper>
  );
};

export const RecoverPassword = compose(
  injectIntl,
  withRouter,
  withState<MethodError>(initialFormState),
  withExtractedAuthState,
  withRecoverPassword,
  withFormik<Props & WithRecoverPasswordProps, RecoverPasswordFormValues>({
    ...recoverPasswordFormFormikUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { setState, dismiss, history, isSubmitting } = props;

      if (isSubmitting) {
        return;
      }

      const input: RecoverPasswordInput = { ...values };

      try {
        setState(ss => ({ ...ss, methodError: null }));

        await props.recoverPassword({ input });

        // Success!  Dismiss the dialog, go home and display a success message
        if (dismiss) {
          dismiss();
        }

        history.push(WebRoute.Home);

        openNotification.updateSuccess(
          {
            description: '',
            message: props.intl.formatMessage(RecoverPasswordMessages.SuccessMessage),
            duration: 0
          });
      } catch (exception) {
        resetGrecaptcha();
        applyExceptionToState(exception, setErrors, setState);
      }

      setSubmitting(false);
    }
  }),
  withRecaptchaHandlers
)(RecoverPasswordComponent) as React.ComponentClass<ParentProps>;
