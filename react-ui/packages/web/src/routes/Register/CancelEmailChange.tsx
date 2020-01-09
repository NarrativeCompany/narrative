import { compose, lifecycle } from 'recompose';
import { RouteComponentProps, withRouter } from 'react-router';
import {
  handleFormlessServerOperation,
  logException,
  withCancelEmailAddressChange,
  WithCancelEmailAddressChangeProps,
  WithUpdateAuthStateProps,
  VerifyPendingEmailAddressInput
} from '@narrative/shared';
import { WebRoute } from '../../shared/constants/routes';
import * as React from 'react';
import { openNotification } from '../../shared/utils/notificationsUtil';
import { ChangeEmailAddressMessages } from '../../shared/i18n/ChangeEmailAddressMessages';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { PendingEmailAddressRouteProps } from './ConfirmEmailChange';
import { Loading } from '../../shared/components/Loading';

type Props =
  WithCancelEmailAddressChangeProps &
  RouteComponentProps<PendingEmailAddressRouteProps> &
  InjectedIntlProps &
  WithUpdateAuthStateProps;

export default compose(
  withCancelEmailAddressChange,
  withRouter,
  injectIntl,
  lifecycle<Props, {}>({
    // tslint:disable-next-line object-literal-shorthand
    componentDidMount: async function () {
      const {
        match,
        cancelEmailAddressChange,
        history,
        intl: { formatMessage }
      } = this.props;

      const { userOid, confirmationId, emailAddressOid, verificationStep } = match && match.params;

      try {
        const input: VerifyPendingEmailAddressInput = { confirmationId, emailAddressOid, verificationStep };
        const result = await handleFormlessServerOperation(
          () => cancelEmailAddressChange( input, userOid )
        );

        // jw: if we got any kind of object back then the mutation ran successfully and we can present the success
        //     message to the user on the home page.
        if (result) {
          const { emailAddress } = result;

          openNotification.updateSuccess({
            message: formatMessage(ChangeEmailAddressMessages.EmailAddressChangeCanceledTitle),
            description: formatMessage(ChangeEmailAddressMessages.EmailAddressChangeCanceledMessage, {emailAddress}),
            duration: 5
          });
        }

        // jw: Now that the cancellation has been
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
