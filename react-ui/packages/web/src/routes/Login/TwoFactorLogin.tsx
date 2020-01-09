import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { RouteComponentProps, withRouter } from 'react-router-dom';
import { Link } from '../../shared/components/Link';
import { withFormik, FormikProps } from 'formik';
import { Col, Divider, Row } from 'antd';
import { AuthForm, AuthWrapper } from '../../shared/styled/shared/auth';
import { Logo, } from '../../shared/components/Logo';
import { WebRoute } from '../../shared/constants/routes';
import { FormattedMessage } from 'react-intl';
import { logout, storeAuthToken } from '../../shared/utils/authTokenUtils';
import { TwoFactorLoginMessages } from '../../shared/i18n/TwoFactorLoginMessages';
import { LoginMessages } from '../../shared/i18n/LoginMessages';
import { Button } from '../../shared/components/Button';
import { withExtractedAuthState, WithExtractedAuthStateProps } from '../../shared/containers/withExtractedAuthState';
import {
  applyExceptionToState,
  MethodError,
  initialFormState,
  twoFactorLoginFormikUtil,
  TwoFactorLoginFormValues,
  withState,
  WithStateProps,
  WithTwoFactorLoginProps,
  withTwoFactorLoginUser,
  withUpdateAuthState,
  WithUpdateAuthStateProps
} from '@narrative/shared';
import { FlexContainer } from '../../shared/styled/shared/containers';
import { Heading } from '../../shared/components/Heading';
import { FormMethodError } from '../../shared/components/FormMethodError';
import { DescriptionParagraph } from '../MemberCP/settingsStyles';
import { reloadForLoginStateChange } from '../../apolloClientInit';
import { TwoFactorAuthenticationCodeInput } from '../../shared/components/TwoFactorAuthenticationCodeInput';
import { getPreviousLocationFor2FAVerify, LoginVerifyHistoryState } from './LoginVerify';
import { CheckboxField } from '../../shared/components/CheckboxField';

// tslint:disable no-any
interface WithHandlers {
  handleCancelTwoFactorLogin: () => any;
}

interface ParentProps {
  dismiss?: () => any;
  showExpiredMessage?: boolean;
}
// tslint:enable no-any

type Props =
  RouteComponentProps<{}, {}, LoginVerifyHistoryState> &
  ParentProps &
  WithStateProps<MethodError> &
  WithHandlers &
  WithTwoFactorLoginProps &
  WithUpdateAuthStateProps &
  WithExtractedAuthStateProps &
  FormikProps<TwoFactorLoginFormValues>;

const TwoFactorLoginComponent: React.SFC<Props> = (props) => {
  const { state, handleCancelTwoFactorLogin, showExpiredMessage, isSubmitting, dismiss } = props;

  let dismissLink: React.ReactNode | undefined;
  // jw: if this is for the standalone login page, then we need to include the standard link
  if (!dismiss) {
    dismissLink = (
      <Link onClick={handleCancelTwoFactorLogin} to={WebRoute.Home}>
        <FormattedMessage {...LoginMessages.ReturnHomeLinkText}/>
      </Link>
    );

  // jw: if this is for expiration then let's include a footer link to logout the user
  } else if (showExpiredMessage) {
    dismissLink = (
      <Link.Anchor onClick={dismiss}>
        <FormattedMessage {...LoginMessages.SignOutLinkText}/>
      </Link.Anchor>
    );
  }

  return (
    <AuthWrapper centerAll={true} style={{paddingTop: 15}}>
      <AuthForm>

        <Row type="flex" align="middle" justify="center" style={{paddingBottom: 25}}>
          <Col>
            <Logo/>
          </Col>
        </Row>

        <FlexContainer centerAll={true} column={true} style={{paddingBottom: 25}}>
          <Heading size={3}>
            <FormattedMessage {...TwoFactorLoginMessages.FormTitle}/>
          </Heading>

          {showExpiredMessage &&
            <DescriptionParagraph textAlign="center">
              <FormattedMessage {...TwoFactorLoginMessages.TwoFactorExpired}/>
            </DescriptionParagraph>
          }

        </FlexContainer>

        <FormMethodError methodError={state.methodError}/>

        <TwoFactorAuthenticationCodeInput name="verificationCode" />

        <CheckboxField name="rememberMe">
          <FormattedMessage {...TwoFactorLoginMessages.RememberMe}/>
        </CheckboxField>

        <Button
          size="large"
          type="primary"
          htmlType="submit"
          loading={isSubmitting}
          block={true}>
          <FormattedMessage {...TwoFactorLoginMessages.SubmitBtnText}/>
        </Button>

        {dismissLink &&
        <Row type="flex" align="middle" justify="center">
          <Divider/>

          {dismissLink}
        </Row>}
      </AuthForm>

    </AuthWrapper>
  );
};

export const TwoFactorLogin = compose(
  withRouter,
  withExtractedAuthState,
  withState<MethodError>(initialFormState),
  withTwoFactorLoginUser,
  withUpdateAuthState,
  withHandlers({
    handleCancelTwoFactorLogin: (props: Props) => async () => {
      const { userAuthenticated } = props;

      // If when exiting we are not authenticated, log out
      if (!userAuthenticated) {
        await logout();
      }
    }
  }),
  withFormik<Props, TwoFactorLoginFormValues>({
    ...twoFactorLoginFormikUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const input = {...values};
      const { setState, location, isSubmitting, showExpiredMessage } = props;

      if (isSubmitting) {
        return;
      }

      try {
        setState(ss => ({...ss, methodError: null}));

        const { token } = await props.twoFactorLogin({input});

        // if payload has the token, pass the token to AuthStore
        if (token) {
          // If we get this far, we have a valid token with a valid 2FA expiry
          storeAuthToken(token, false);

          // zb: normally we would reset the store here, but until
          // it is fixed we will be reloading the page until the
          // apollo resetStore() is fixed
          // TODO: #1036 Fix me when underlying Apollo issues are resolved

          // Make sure reset the store so we don't mix data from the unauthenticated user with data for the current user
          // await resetStore();

          // Return to sender and component and component state is destroyed
          // bl: since we are just going to load a new page below, there's no need to update authentication
          // state here. this will avoid unnecessary new calls to refresh current user which were happening
          // and subsequently being canceled once the page reloaded
          // await updateAuthenticationState({ authenticationState: AuthenticationState.USER_AUTHENTICATED });
          if (props.dismiss && !showExpiredMessage) {
            // bl: while we are doing reloads here, there's no need to dismiss the modal. just keep it open
            // until the page reloads to avoid the jarring effect of the modal closing then page reloading
            // props.dismiss();
            reloadForLoginStateChange();
          } else {
            // jw: due to the versatility of this component we need to handle not just history previous lookups, but
            //     also allow the returnTo to be specified through the state.
            const prevLocation = getPreviousLocationFor2FAVerify(location);

            window.location.href = prevLocation || WebRoute.Home;
          }
        }
      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);
      }

      setSubmitting(false);
    }
  })
)(TwoFactorLoginComponent) as React.ComponentClass<ParentProps>;
