import * as React from 'react';
import { GlobalErrorModal } from './GlobalErrorModal';
import { TOSAgreementModal } from './TOSAgreementModal';
import { withExtractedErrorState, WithExtractedErrorStateProps } from '../../shared/containers/withExtractedErrorState';
import { compose, withHandlers, withProps } from 'recompose';
import { ErrorType, withClearErrorState, WithClearErrorStateProps } from '@narrative/shared';
import { Modal } from 'antd';
import LoginForm, { LoginFormMessageType } from '../../routes/Login/LoginForm';
import { EmailConfirmRequiredNotificationModal } from './EmailConfirmRequiredNotificationModal';
import { ActivityRateLimitExceededModal } from './ActivityRateLimitExceededModal';
import { RouterProps, withRouter } from 'react-router';
import { WebRoute } from '../../shared/constants/routes';
import { ExpiredPublicationErrorModal } from '../../routes/Publication/components/ExpiredPublicationErrorModal';

enum HandlerType {
  NONE,
  LOGIN_REQUIRED,
  TWOFACTOR_REQUIRED,
  TOS_ACCEPT,
  EMAIL_VERIFICATION_REQUIRED,
  GLOBAL_ERROR,
  ACTIVITY_RATE_LIMIT_EXCEEDED,
  EXPIRED_PUBLICATION,
}

interface WithHandlers {
  // tslint:disable-next-line no-any
  handleDismiss: () => any;
}

interface WithProps {
  handlerType: HandlerType;
  modalVisible: boolean;
  loginMessage: LoginFormMessageType;
}

type Props =
  WithProps &
  WithHandlers &
  WithExtractedErrorStateProps &
  RouterProps;

const GlobalErrorControllerComponent: React.SFC<Props> = (props) => {
  const { errorState, handlerType, handleDismiss, modalVisible, loginMessage, isError } = props;

  if (!isError) {
    return null;
  }

  switch (handlerType) {
    case HandlerType.TOS_ACCEPT:
      // Prompt for TOS acceptance
      return (
        <TOSAgreementModal
          handleDismiss={handleDismiss}
          modalVisible={modalVisible}
        />
      );

    case HandlerType.EMAIL_VERIFICATION_REQUIRED:
      // Prompt user that they need to confirm the welcome email
      return (
        <EmailConfirmRequiredNotificationModal
          handleDismiss={handleDismiss}
          modalVisible={modalVisible}
        />
      );
    case HandlerType.LOGIN_REQUIRED:
      // Handle expired login - prompt for login
      return (
        <Modal
          visible={modalVisible}
          onCancel={handleDismiss}
          footer={null}
          destroyOnClose={true}
        >
          <LoginForm
            dismiss={handleDismiss}
            loginFormMessageType={loginMessage}
          />
        </Modal>
      );

    case HandlerType.TWOFACTOR_REQUIRED:
      const { history: { location, push } } = props;

      // jw: if the signin verification is not the route we are on then redirect to it.
      if (location.pathname !== WebRoute.SigninVerify) {
        // jw: be sure to include the search in the redirect, just in case there are any parameters we should maintain
        push(WebRoute.SigninVerify, {returnTo: location.pathname + location.search});
      }

      // jw: now, there is nothing else to do.
      return null;

    case HandlerType.ACTIVITY_RATE_LIMIT_EXCEEDED:
      // Show a global error modal
      return (
        <ActivityRateLimitExceededModal
          visible={modalVisible}
          handleDismiss={handleDismiss}
          errorMessage={errorState.message}
        />
      );

    case HandlerType.EXPIRED_PUBLICATION:
      // Show a global error modal
      return (
        <ExpiredPublicationErrorModal
          visible={modalVisible}
          close={handleDismiss}
          errorDetails={errorState.detail}
        />
      );

    case HandlerType.GLOBAL_ERROR:
      // Show a global error modal
      return (
        <GlobalErrorModal
          errorState={errorState}
          handleDismiss={handleDismiss}
          modalVisible={modalVisible}
        />
      );

    default:
      // todo:error-handling: We should report to the server that there was a ErrorType we could not handle
      return null;
  }
};

export const GlobalErrorController = compose(
  withClearErrorState,
  withHandlers({
    handleDismiss: (props: Props & WithClearErrorStateProps) => async () => {
      const { clearErrorState } = props;

      await clearErrorState();
    }
  }),
  withExtractedErrorState,
  withProps((props: Props) => {
    const { isError, errorState } = props;

    let handlerType: HandlerType;
    let loginMessage: LoginFormMessageType = LoginFormMessageType.NONE;

    if (isError) {
      switch (ErrorType[errorState.type || ErrorType.UNDEFINED]) {
        case ErrorType.LOGIN_REQUIRED:
          handlerType = HandlerType.LOGIN_REQUIRED;
          loginMessage = LoginFormMessageType.LOGIN_REQUIRED;
          break;
        case ErrorType.JWT_INVALID:
          handlerType = HandlerType.LOGIN_REQUIRED;
          loginMessage = LoginFormMessageType.LOGIN_EXPIRED;
          break;
        case ErrorType.JWT_2FA_EXPIRED:
          handlerType = HandlerType.TWOFACTOR_REQUIRED;
          break;
        case ErrorType.LOGIN_REQ_EMAIL_VERIFIED:
          handlerType = HandlerType.LOGIN_REQUIRED;
          loginMessage = LoginFormMessageType.EMAIL_VERIFIED;
          break;
        case ErrorType.TOS_AGREEMENT_REQUIRED:
          handlerType = HandlerType.TOS_ACCEPT;
          break;
        case ErrorType.EMAIL_VERIFICATION_REQUIRED:
          handlerType = HandlerType.EMAIL_VERIFICATION_REQUIRED;
          break;
        case ErrorType.ACTIVITY_RATE_LIMIT_EXCEEDED:
          handlerType = HandlerType.ACTIVITY_RATE_LIMIT_EXCEEDED;
          break;
        case ErrorType.EXPIRED_PUBLICATION:
          handlerType = HandlerType.EXPIRED_PUBLICATION;
          break;
        default:
          handlerType = HandlerType.GLOBAL_ERROR;
          break;
      }
    } else {
      handlerType = HandlerType.NONE;
    }

    return {
      handlerType,
      loginMessage
    };
  }),
  withProps((props: Props) => {
    return { modalVisible: !(props.handlerType === HandlerType.NONE) };
  }),
  withRouter
)(GlobalErrorControllerComponent) as React.ComponentClass<{}>;
