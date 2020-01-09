import * as React from 'react';
import { compose } from 'recompose';
import { RouteComponentProps, withRouter } from 'react-router-dom';
import { FormikProps, withFormik } from 'formik';
import { Col, Divider, Icon, Row } from 'antd';
import { Link } from '../../shared/components/Link';
import { Logo } from '../../shared/components/Logo';
import { Paragraph } from '../../shared/components/Paragraph';
import { InputField } from '../../shared/components/InputField';
import { CheckboxField } from '../../shared/components/CheckboxField';
import { Button } from '../../shared/components/Button';
import { convertInputFieldAddon } from '../../shared/utils/convertInputAddon';
import { WebRoute } from '../../shared/constants/routes';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { LoginMessages } from '../../shared/i18n/LoginMessages';
import { storeAuthToken } from '../../shared/utils/authTokenUtils';
import { AuthForm, AuthLinkWrapper, AuthWrapper, RememberMeWrapper } from '../../shared/styled/shared/auth';
import {
  applyExceptionToState,
  MethodError,
  initialFormState,
  AuthenticationState,
  loginFormikUtil,
  LoginFormValues,
  LoginInput,
  withClearErrorState,
  WithClearErrorStateProps,
  withLoginUser,
  WithLoginUserProps,
  withState,
  WithStateProps,
  withUpdateAuthState,
  WithUpdateAuthStateProps,
  ErrorState,
  HttpStatusCode
} from '@narrative/shared';
import { FlexContainer } from '../../shared/styled/shared/containers';
import { Heading } from '../../shared/components/Heading';
import { FormMethodError } from '../../shared/components/FormMethodError';
import { resolvePreviousLocation } from '../../shared/utils/routeUtils';
import { generatePath } from 'react-router';
import { DescriptionParagraph } from '../MemberCP/settingsStyles';
import { LoginFormMessageType, ModalComponent } from './LoginForm';
import { reloadForLoginStateChange } from '../../apolloClientInit';
import { Recaptcha } from '../../shared/components/Recaptcha';
import { RecaptchaHandlers, resetGrecaptcha, withRecaptchaHandlers } from '../../shared/utils/recaptchaUtils';

interface ParentProps {
  dismiss?: () => void;
  setComponentVisible: (component: ModalComponent) => void;
  loginFormMessageType?: LoginFormMessageType;
}

type Props =
  ParentProps &
  FormikProps<LoginFormValues> &
  WithLoginUserProps &
  RouteComponentProps<{}> &
  WithStateProps<MethodError> &
  InjectedIntlProps &
  WithUpdateAuthStateProps &
  RecaptchaHandlers;

const LoginComponent: React.SFC<Props> = (props) => {
  const {
    state,
    intl,
    dismiss,
    loginFormMessageType,
    setComponentVisible,
    isSubmitting,
    handleRecaptchaVerifyCallback,
    handleRecaptchaExpiredCallback,
    errors,
    touched
  } = props;

  const recaptchaError = touched.recaptchaResponse && errors.recaptchaResponse ?
    errors.recaptchaResponse as string :
    undefined;

  const showModalTitle =
    !!dismiss ||
    LoginFormMessageType.EMAIL_VERIFIED === loginFormMessageType;

  return (

    <AuthWrapper centerAll={true}>
      <AuthForm>

        <Row type="flex" align="middle" justify="center" style={{paddingBottom: 25}}>
           <Col>
             <Logo/>
           </Col>
         </Row>

        <FlexContainer centerAll={true} column={true} style={{paddingBottom: 25}}>
          <Heading size={3}>
              {showModalTitle &&
              <FormattedMessage {...LoginMessages.FormTitleModal}/>}

              {!showModalTitle &&
              <FormattedMessage {...LoginMessages.FormTitleFullPage}/>}
          </Heading>

          {LoginFormMessageType.LOGIN_EXPIRED === loginFormMessageType &&
          <DescriptionParagraph  textAlign="center">
            <FormattedMessage {...LoginMessages.LoginExpired}/>
          </DescriptionParagraph>}

          {LoginFormMessageType.LOGIN_REQUIRED === loginFormMessageType &&
          <DescriptionParagraph  textAlign="center">
            <FormattedMessage {...LoginMessages.LoginRequired}/>
          </DescriptionParagraph>}

          {LoginFormMessageType.EMAIL_VERIFIED === loginFormMessageType &&
          <DescriptionParagraph  textAlign="center">
            <FormattedMessage {...LoginMessages.EmailVerifiedLoginRequired}/>
          </DescriptionParagraph>}
        </FlexContainer>

        <FormMethodError methodError={state.methodError} />

        <InputField
          prefix={convertInputFieldAddon(<Icon type="mail"/>)}
          size="large"
          type="email"
          placeholder={intl.formatMessage(LoginMessages.EmailInputPlaceholder)}
          name="emailAddress"
        />

        <InputField
          prefix={convertInputFieldAddon(<Icon type="lock"/>)}
          size="large"
          type="password"
          placeholder={intl.formatMessage(LoginMessages.PasswordInputPlaceholder)}
          name="password"
        />

        <RememberMeWrapper alignItems="center" justifyContent="space-between">
          <CheckboxField name="rememberMe" style={{margin: 0}}>
            <FormattedMessage {...LoginMessages.RememberMe}/>
          </CheckboxField>

          <Link.Anchor onClick={() => setComponentVisible(ModalComponent.RECOVER_PASSWORD)}>
            <FormattedMessage {...LoginMessages.ForgotPassword}/>
          </Link.Anchor>
        </RememberMeWrapper>

        <Recaptcha
          verifyCallback={handleRecaptchaVerifyCallback}
          expiredCallback={handleRecaptchaExpiredCallback}
          error={recaptchaError}
          style={{marginBottom: 24}}
        />

        <Button
          size="large"
          type="primary"
          htmlType="submit"
          block={true}
          loading={isSubmitting}
          style={{marginBottom: 10}}>
          <FormattedMessage {...LoginMessages.SubmitBtnText}/>
        </Button>

        <AuthLinkWrapper centerAll={true}>
          <Paragraph>
            <FormattedMessage {...LoginMessages.RegisterLinkPrefix}/>
          </Paragraph>

          <Link to={generatePath(WebRoute.Register)}>
            <FormattedMessage {...LoginMessages.RegisterLinkText}/>
          </Link>
        </AuthLinkWrapper>

        {/*If in full page mode, add a link to log out and return to home*/}
        {!props.dismiss &&
        <Row type="flex" align="middle" justify="center">
          <Divider/>

          <Link to={WebRoute.Home}>
            <FormattedMessage {...LoginMessages.ReturnHomeLinkText}/>
          </Link>
        </Row>}
      </AuthForm>
    </AuthWrapper>
  );
};

export const Login = compose(
  withRouter,
  injectIntl,
  withLoginUser,
  withState<MethodError>(initialFormState),
  withUpdateAuthState,
  withClearErrorState,
  withFormik<Props & WithClearErrorStateProps, LoginFormValues>({
    ...loginFormikUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const input: LoginInput = {...values};
      const {
        setState,
        clearErrorState,
        location,
        updateAuthenticationState,
        loginFormMessageType,
        isSubmitting,
        intl: { formatMessage }
      } = props;

      if (isSubmitting) {
        return;
      }

      try {
        setState(ss => ({...ss, methodError: null}));

        const { token, twoFactorAuthExpired } = await props.loginUser({ input });

        // if payload has the token, pass the token to AuthStore
        if (token) {
          // Make sure we set the state indicating whether the token we received requires 2FA - indicated by whether
          // its 2FA expiry is expired
          storeAuthToken(token, twoFactorAuthExpired === true);

          // Now that we properly stored the token let's redirect the user
          // If user was redirected from auth route, direct them back to where they were going
          // else redirect them to HQ
          let prevLocation;
          if (LoginFormMessageType.EMAIL_VERIFIED === loginFormMessageType) {
            // Special case for email verified login - stay where you are
            prevLocation = location;
          } else {
            prevLocation = resolvePreviousLocation(location);
          }

          if (!twoFactorAuthExpired) {
            // Make sure reset the store so we don't mix data from the unauthenticated user with data for the current
            // user.  Only reset for non-2FA since the client will try to re-fetch on a reset and would have an
            // invalid token!

            // zb: normally we would reset the store here, but until
            // it is fixed we will be reloading the page until the
            // apollo resetStore() is fixed
            // TODO: #1036 Fix me when underlying Apollo issues are resolved
            // await resetStore();

            // bl: since we are just going to load a new page below, there's no need to update authentication
            // state here. this will avoid unnecessary new calls to refresh current user which were happening
            // and subsequently being canceled once the page reloaded
            // await updateAuthenticationState({ authenticationState: AuthenticationState.USER_AUTHENTICATED });
            if (props.dismiss) {
              // bl: while we are doing reloads here, there's no need to dismiss the modal. just keep it open
              // until the page reloads to avoid the jarring effect of the modal closing then page reloading
              // props.dismiss();
              reloadForLoginStateChange();
            } else {
              window.location.href = prevLocation || WebRoute.Home;
            }
          } else {
            // Prompt for a 2FA code
            await updateAuthenticationState({ authenticationState: AuthenticationState.USER_REQUIRES_2FA });
            props.setComponentVisible(ModalComponent.TWO_FACTOR);
          }
        }
      } catch (exception) {
        resetGrecaptcha();
        const errorState: ErrorState = exception.errorState;
        // bl: special handling for Too Many Requests errors from Cloudflare's rate limiting
        if (errorState && errorState.httpStatusCode === HttpStatusCode.TOO_MANY_REQUESTS) {
          setState(ss => ({ ...ss, methodError: [formatMessage(LoginMessages.TooManyRequests)] }));
          // bl: clear the error state so that we don't show the generic NetworkError modal
          await clearErrorState();
        } else {
          applyExceptionToState(exception, setErrors, setState);
        }
      }

      setSubmitting(false);
    }
  }),
  withRecaptchaHandlers
)(LoginComponent) as React.ComponentClass<ParentProps>;
