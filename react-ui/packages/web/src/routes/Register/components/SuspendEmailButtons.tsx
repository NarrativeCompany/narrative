import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import {
  SuspendEmailInput,
  withSuspendEmailAddressForUser,
  WithSuspendEmailAddressForUserProps,
  withState,
  WithStateProps
} from '@narrative/shared';
import { Button } from '../../../shared/components/Button';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { SuspendEmailMessages } from '../../../shared/i18n/SuspendEmailMessages';
import { WebRoute } from '../../../shared/constants/routes';
import styled from 'styled-components';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { openNotification } from '../../../shared/utils/notificationsUtil';
import { RouteComponentProps, withRouter } from 'react-router';

interface ParentProps {
  userOid: string;
  input: SuspendEmailInput;
}

const ButtonsContainer = styled<FlexContainerProps>(FlexContainer)`
  margin: 0 auto;
  max-width: 300px;
`;

interface State {
  processing?: boolean;
}

interface Handlers {
  suspendEmailAddress: () => void;
}

type HandlerProps = ParentProps &
  WithStateProps<State> &
  RouteComponentProps &
  InjectedIntlProps &
  WithSuspendEmailAddressForUserProps;

type Props = WithStateProps<State> &
  Handlers;

const SuspendEmailButtonsComponent: React.SFC<Props> = (props) => {
  const { suspendEmailAddress, state: { processing } } = props;

  return (
    <ButtonsContainer justifyContent="space-between">
      <Button type="primary" onClick={suspendEmailAddress} loading={processing}>
        <FormattedMessage {...SuspendEmailMessages.SuspendEmailsConfirmButtonText} />
      </Button>
      <Button type="danger" href={WebRoute.Home}>
        <FormattedMessage {...SuspendEmailMessages.SuspendEmailsCancelButtonText} />
      </Button>
    </ButtonsContainer>
  );
};

export const SuspendEmailButtons = compose(
  withState<State>({}),
  withSuspendEmailAddressForUser,
  withRouter,
  injectIntl,
  withHandlers<HandlerProps, Handlers>({
    suspendEmailAddress: (props) => async () => {
      const {
        suspendEmailAddressForUser,
        userOid,
        input,
        history,
        setState,
        state: { processing },
        intl: { formatMessage }
      } = props;

      // jw: if we are already processing then short out.
      if (processing) {
        return;
      }

      setState(ss => ({...ss, processing: true}));
      try {
        const result = await suspendEmailAddressForUser({userOid, input});

        // jw: if we got a result then let's notify the user and redirect them to the main page.
        if (result) {
          const { emailAddress } = input;

          await openNotification.updateSuccess(
            {
              description: '',
              message: formatMessage(SuspendEmailMessages.EmailsSuspended, { emailAddress }),
              duration: 0
            });

          history.push(WebRoute.Home);
        }

      } finally {
        setState(ss => ({...ss, processing: undefined}));
      }
    }
  })
)(SuspendEmailButtonsComponent) as React.ComponentClass<ParentProps>;
