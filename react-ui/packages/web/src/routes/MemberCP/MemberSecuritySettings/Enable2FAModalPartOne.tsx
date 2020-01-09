import * as React from 'react';
import { Col, Form, Icon, Row, Tooltip } from 'antd';
import {
  applyExceptionToState,
  MethodError,
  initialFormState,
  AuthenticationState,
  EnableTwoFactorAuthInput,
  pw2FACodeRememberMeFormFormikUtil,
  withEnableTwoFactorAuth,
  WithEnableTwoFactorAuthProps,
  withState,
  WithStateProps,
  withUpdateAuthState,
  WithUpdateAuthStateProps,
  PW2FACodeRememberMeFormValues
} from '@narrative/shared';
import styled from '../../../shared/styled';
import { Link } from '../../../shared/components/Link';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { Enable2FAMessages } from '../../../shared/i18n/Enable2FAMessages';
import { TwoFactorLoginMessages } from '../../../shared/i18n/TwoFactorLoginMessages';
import { compose } from 'recompose';
import { InputField } from '../../../shared/components/InputField';
import { convertInputFieldAddon } from '../../../shared/utils/convertInputAddon';
import { LoginMessages } from '../../../shared/i18n/LoginMessages';
import { AuthForm } from '../../../shared/styled/shared/auth';
import { withFormik, FormikProps } from 'formik';
import { Paragraph } from '../../../shared/components/Paragraph';
import { storeAuthToken } from '../../../shared/utils/authTokenUtils';
import { Button } from '../../../shared/components/Button';
import { FormMethodError } from '../../../shared/components/FormMethodError';
import { externalUrls } from '../../../shared/constants/externalUrls';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { mediaQuery } from '../../../shared/styled/utils/mediaQuery';
import { TwoFactorAuthenticationCodeInput } from '../../../shared/components/TwoFactorAuthenticationCodeInput';
import { CheckboxField } from '../../../shared/components/CheckboxField';

const FormItem = Form.Item;

export interface Enable2FAModalPartOneProps {
  secret: string;
  qrCodeImage: string;
}

interface ParentProps extends Enable2FAModalPartOneProps {
  success: () => void;
}

type Props =
  ParentProps &
  WithStateProps<MethodError> &
  InjectedIntlProps &
  WithEnableTwoFactorAuthProps &
  WithUpdateAuthStateProps &
  FormikProps<PW2FACodeRememberMeFormValues>;

const Image = styled.img`
  ${mediaQuery.md_up`
    margin-top: 10px;
  `};
`;

const Enable2FAModalPartOneComponent: React.SFC<Props> = (props) => {
  const { state, qrCodeImage, secret, isSubmitting, intl: { formatMessage } } = props;

  const iosLink = (
    <Link.Anchor href={externalUrls.iosGoogleAuthenticator} target="_blank">
      <FormattedMessage {...Enable2FAMessages.StepOneIOSLinkText}/>
    </Link.Anchor>
  );

  const androidLink = (
    <Link.Anchor href={externalUrls.androidGoogleAuthenticator} target="_blank">
      <FormattedMessage {...Enable2FAMessages.StepOneAndroidLink}/>
    </Link.Anchor>
  );

  return (
    <React.Fragment>
        <FormMethodError methodError={state.methodError}/>

        <Row gutter={60} justify="center">
          <Col md={11} offset={1}>
            <Paragraph marginBottom="large">
              <strong>
                <FormattedMessage {...Enable2FAMessages.StepOneTitle}/>
              </strong>

              <FormattedMessage {...Enable2FAMessages.StepOne} values={{iosLink, androidLink}}/>
            </Paragraph>

            <FlexContainer justifyContent="center">
              <Image src={qrCodeImage}/>
            </FlexContainer>

            <FlexContainer justifyContent="center">
              <Tooltip
                title={secret}
                trigger="click"
                placement="bottom"
              >
                <span style={{cursor: 'pointer'}}>
                  <Paragraph color="primary">{formatMessage(Enable2FAMessages.ShowSecretKey)}</Paragraph>
                </span>
              </Tooltip>
            </FlexContainer>
          </Col>

          <Col md={11}>
            <Paragraph marginBottom="large">
              <strong>
                <FormattedMessage {...Enable2FAMessages.StepTwoTitle}/>
              </strong>

              <FormattedMessage {...Enable2FAMessages.StepTwoMessage}/>
            </Paragraph>

            <AuthForm>
              <FormItem>
                <InputField
                  style={{paddingTop: 20, marginBottom: 0}}
                  prefix={convertInputFieldAddon(<Icon type="lock"/>)}
                  size="large"
                  type="password"
                  placeholder={formatMessage(LoginMessages.PasswordInputPlaceholder)}
                  label={formatMessage(LoginMessages.PasswordInputLabel)}
                  name="currentPassword"
                />

                <TwoFactorAuthenticationCodeInput
                  name="twoFactorAuthCode"
                  style={{marginBottom: 0}}
                />

                <CheckboxField style={{padding: '10px 0', marginBottom: 0}} name="rememberMe">
                  <FormattedMessage {...TwoFactorLoginMessages.RememberMe}/>
                </CheckboxField>

                <Button size="large" type="primary" htmlType="submit" block={true} loading={isSubmitting}>
                  <FormattedMessage {...TwoFactorLoginMessages.SubmitBtnText}/>
                </Button>
              </FormItem>
            </AuthForm>
          </Col>
        </Row>
    </React.Fragment>
  );
};

export const Enable2FAModalPartOne = compose(
  injectIntl,
  withUpdateAuthState,
  withEnableTwoFactorAuth,
  withState<MethodError>(initialFormState),
  withFormik<Props, PW2FACodeRememberMeFormValues>({
    ...pw2FACodeRememberMeFormFormikUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { setState, updateAuthenticationState, isSubmitting } = props;
      const { currentPassword, twoFactorAuthCode } = {...values};

      if (isSubmitting) {
        return;
      }

      const rememberMe = !!(values.rememberMe);

      const input: EnableTwoFactorAuthInput = {currentPassword, twoFactorAuthCode , rememberMe, secret: props.secret};

      setState(ss => ({...ss, methodError: null}));

      try {
        // Try to enable
        const { token } = await props.enableTwoFactorAuth({ input });

        if (token) {
          // If we get this far, we have a valid token with a valid 2FA expiry
          storeAuthToken(token, false);
          await updateAuthenticationState({ authenticationState: AuthenticationState.USER_AUTHENTICATED });

          // jw: tell our parent that 2FA has been enabled
          props.success();
        }
      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);
      }

      setSubmitting(false);
    }
  })
)(Enable2FAModalPartOneComponent) as React.ComponentClass<ParentProps>;
