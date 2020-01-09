import { compose, lifecycle } from 'recompose';
import { RouteComponentProps, withRouter } from 'react-router';
import {
  buildEmptyErrorState,
  ErrorState,
  ErrorType,
  handleFormlessServerOperation,
  setErrorStateinCache,
  VerifyEmailAddressInput,
  withClearErrorState,
  WithClearErrorStateProps,
  withVerifyEmailAddressForUser,
  WithVerifyEmailAddressForUserProps
} from '@narrative/shared';
import { WebRoute } from '../../shared/constants/routes';
import * as React from 'react';
import { apolloCache } from '../../apolloClientInit';
import { Loading } from '../../shared/components/Loading';

interface RouteProps extends VerifyEmailAddressInput {
  userOid: string;
}

type Props =
  WithVerifyEmailAddressForUserProps &
  WithClearErrorStateProps &
  RouteComponentProps<RouteProps>;

export default compose(
  withVerifyEmailAddressForUser,
  withClearErrorState,
  withRouter,
  lifecycle<Props, {}>({
    // tslint:disable-next-line object-literal-shorthand
    componentDidMount: async function () {
      const { match, verifyEmailAddressForUser, clearErrorState, history } = this.props;
      const { userOid, confirmationId } = match && match.params;

      const input: VerifyEmailAddressInput = { confirmationId };
      const result = await handleFormlessServerOperation(
        () => verifyEmailAddressForUser( {input, userOid} )
      );

      // jw: if we got an object back from the mutation then it went through on the server
      if (result) {
        // Make sure we clear error state in case there was an outstanding fetch that caused
        // an EMAIL_VERIFICATION_REQUIRED
        try {
          await clearErrorState();
        } catch (exception) {
          // tslint:disable-next-line no-console
          console.error('Error clearing error state while confirming email', exception);
        }

        // Go Home
        history.push(WebRoute.Home);

        // Set the error state in order to prompt the user for sign-in
        const errorState: ErrorState = buildEmptyErrorState();
        errorState.type = ErrorType.LOGIN_REQ_EMAIL_VERIFIED;
        setErrorStateinCache(apolloCache, errorState);

      } else {
        // Go Home
        history.push(WebRoute.Home);
      }
    }
  }),
/* so that the user has some indication that something is happening, let's show the Loading spinner until we are done */
) (() => <Loading />) as React.ComponentClass<{}>;
