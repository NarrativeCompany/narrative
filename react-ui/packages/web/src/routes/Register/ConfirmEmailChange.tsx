import { compose, lifecycle } from 'recompose';
import { RouteComponentProps, withRouter } from 'react-router';
import {
  AuthenticationState,
  buildEmptyErrorState,
  ErrorState,
  ErrorType,
  handleFormlessServerOperation,
  logException,
  setErrorStateinCache,
  VerifyPendingEmailAddressInput,
  withClearErrorState,
  WithClearErrorStateProps,
  withUpdateAuthState,
  WithUpdateAuthStateProps,
  withVerifyPendingEmailAddressForUser,
  WithVerifyPendingEmailAddressForUserProps
} from '@narrative/shared';
import { WebRoute } from '../../shared/constants/routes';
import * as React from 'react';
import { storeAuthToken } from '../../shared/utils/authTokenUtils';
import { apolloCache } from '../../apolloClientInit';
import { openNotification } from '../../shared/utils/notificationsUtil';
import { ChangeEmailAddressMessages } from '../../shared/i18n/ChangeEmailAddressMessages';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { Loading } from '../../shared/components/Loading';

export interface PendingEmailAddressRouteProps extends VerifyPendingEmailAddressInput {
  userOid: string;
}

type Props =
  WithVerifyPendingEmailAddressForUserProps &
  WithClearErrorStateProps &
  RouteComponentProps<PendingEmailAddressRouteProps> &
  InjectedIntlProps &
  WithUpdateAuthStateProps;

export default compose(
  withVerifyPendingEmailAddressForUser,
  withClearErrorState,
  withRouter,
  withUpdateAuthState,
  injectIntl,
  lifecycle<Props, {}>({
    // tslint:disable-next-line object-literal-shorthand
    componentDidMount: async function () {
      const {
        match,
        verifyPendingEmailAddressForUser,
        clearErrorState,
        history,
        updateAuthenticationState,
        intl: { formatMessage }
      } = this.props;

      const { userOid, confirmationId, emailAddressOid, verificationStep } = match && match.params;

      try {
        const input: VerifyPendingEmailAddressInput = { confirmationId, emailAddressOid, verificationStep };
        const result = await handleFormlessServerOperation(
          () => verifyPendingEmailAddressForUser( input, userOid )
        );

        // jw: if we did not get a result then likely there was an error that bumbled up to the error boundary and will
        //     present to the user. So, let's focus on the cases where we got a result, we will still redirect to to
        //     Home below, so no worries there.
        if (result) {
          const { emailAddress, emailAddressToVerify, incompleteVerificationSteps } = result;
          const token = result.token &&
            result.token.token;

          // Make sure we clear error state in case there was an outstanding fetch that caused
          // an EMAIL_VERIFICATION_REQUIRED
          try {
            await clearErrorState();
          } catch (exception) {
            // tslint:disable-next-line no-console
            console.error('Error clearing error state while confirming pending email', exception);
          }

          // jw: if we have a token, then we need to update the users authentication.
          if (token) {
            // Store the new token and update the auth state
            storeAuthToken(token, false);
            await updateAuthenticationState({ authenticationState: AuthenticationState.USER_AUTHENTICATED });

            // jw: Let's inform the user that they are done.
            openNotification.updateSuccess({
              message: formatMessage(ChangeEmailAddressMessages.EmailAddressVerificationCompleteTitle),
              description: formatMessage(
                ChangeEmailAddressMessages.EmailAddressVerificationCompleteMessage,
                {emailAddress}
              ),
              duration: null
            });

          // jw: if we do not have any more verification steps remaining then let's show the login modal for email
          //     verification when we take them to home.
          } else if (!incompleteVerificationSteps.length) {
            const errorState: ErrorState = buildEmptyErrorState();
            errorState.type = ErrorType.LOGIN_REQ_EMAIL_VERIFIED;
            setErrorStateinCache(apolloCache, errorState);

          } else {
            // todo:error-handling: We should assert here that we only have one incomplete verification step remaining.

            let description;
            if (emailAddressToVerify) {
              description = formatMessage(
                ChangeEmailAddressMessages.EmailAddressVerifiedMessage,
                { emailAddressToVerify }
                );
            } else {
              // todo:error-handling: We should assert here that the incompleteVerificationStep is primary, since that
              //      is the only time we expect not to have a emailAddressToVerify.
              description = formatMessage(ChangeEmailAddressMessages.PendingEmailAddressVerifiedMessage);
            }

            // jw: Let's inform the user that they still have work to do.
            openNotification.updateSuccess({
              message: formatMessage(ChangeEmailAddressMessages.EmailAddressVerifiedTitle),
              description,
              duration: null
            });
          }
        }

        // jw: Whether their email address still requires verification or not we should send them to the home page.
        history.push(WebRoute.Home);

      } catch (e) {
        logException('Error verifying email', e);
        // Just go home - an error will result from the RequestTerminatedException
        history.push(WebRoute.Home);
      }
    }
  }),
/* so that the user has some indication that something is happening, let's show the Loading spinner until we are done */
) (() => <Loading />) as React.ComponentClass<{}>;
