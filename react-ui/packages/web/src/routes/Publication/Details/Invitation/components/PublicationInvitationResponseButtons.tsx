import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import {
  Publication,
  PublicationRole,
  withAcceptPublicationPowerUserInvite,
  WithAcceptPublicationPowerUserInviteProps,
  withDeclinePublicationPowerUserInvite,
  WithDeclinePublicationPowerUserInviteProps,
  WithPublicationPowerUserInvitationProps,
  withState, WithStateProps
} from '@narrative/shared';
import { injectIntl, InjectedIntlProps, FormattedMessage } from 'react-intl';
import { generatePath, RouteComponentProps, withRouter } from 'react-router';
import { openNotification } from '../../../../../shared/utils/notificationsUtil';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { getIdForUrl } from '../../../../../shared/utils/routeUtils';
import { WebRoute } from '../../../../../shared/constants/routes';
import { EnhancedPublicationRole } from '../../../../../shared/enhancedEnums/publicationRole';
import { createCommaDelimitedWordList } from '../../../../../shared/components/CommaDelimitedWordList';
import { FlexContainer, FlexContainerProps } from '../../../../../shared/styled/shared/containers';
import styled from 'styled-components';
import { Button } from '../../../../../shared/components/Button';

type MessageDescriptor = FormattedMessage.MessageDescriptor;

interface ParentProps extends Pick<WithPublicationPowerUserInvitationProps, 'invitedRoles'> {
  publication: Publication;
}

interface State {
  processingAccept?: boolean;
  processingDecline?: boolean;
}

interface Handlers {
  acceptInvitation: () => void;
  declineInvitation: () => void;
}

type Props = ParentProps &
  WithStateProps<State> &
  Handlers;

const ButtonsContainer = styled<FlexContainerProps>(FlexContainer)`
  margin: 0 auto;
  max-width: 300px;
`;

const PublicationInvitationResponseButtonsComponent: React.SFC<Props> = (props) => {
  const { acceptInvitation, declineInvitation, state: { processingAccept, processingDecline } } = props;

  return (
    <ButtonsContainer justifyContent="space-between">
      <Button type="primary" onClick={acceptInvitation} loading={processingAccept}>
        <FormattedMessage {...PublicationDetailsMessages.AcceptInvitationButtonText} />
      </Button>
      <Button type="danger" onClick={declineInvitation} loading={processingDecline}>
        <FormattedMessage {...PublicationDetailsMessages.DeclineInvitationButtonText} />
      </Button>
    </ButtonsContainer>
  );
};

type HandlerProps = ParentProps &
  WithStateProps<State> &
  RouteComponentProps &
  InjectedIntlProps &
  WithAcceptPublicationPowerUserInviteProps &
  WithDeclinePublicationPowerUserInviteProps;

async function processServerOperation(
  props: HandlerProps,
  successMessage: MessageDescriptor,
  processingState: State,
  processor: (publicationOid: string) => {}
): Promise<void> {
  const {
    setState,
    invitedRoles,
    history,
    intl: { formatMessage },
    publication: { oid, prettyUrlString },
    state: { processingAccept, processingDecline }
  } = props;

  // jw: if we are processing already then short out.
  if (processingAccept || processingDecline) {
    return;
  }

  setState(ss => ({...ss, ...processingState}));
  try {
    const result = await processor(oid);

    // jw: if we got a result then let's notify the user and redirect them to the main page.
    if (result) {
      const roleNamesWithArticles: string[] = invitedRoles.reduce((names, role: PublicationRole) => {
        const roleType = EnhancedPublicationRole.get(role);
        names.push(formatMessage(roleType.nameWithArticle));
        return names;
      }, [] as string[]);
      const invitedRoleNames = createCommaDelimitedWordList(roleNamesWithArticles, formatMessage);

      await openNotification.updateSuccess(
        {
          description: '',
          message: formatMessage(successMessage, {invitedRoleNames}),
          duration: 0
        });

      const id = getIdForUrl(prettyUrlString, oid);
      history.push(generatePath(WebRoute.PublicationDetails, {id}));
    }

  } finally {
    setState(ss => ({...ss, processingAccept: undefined, processingDecline: undefined}));
  }
}

export const PublicationInvitationResponseButtons = compose(
  withState<State>({}),
  withAcceptPublicationPowerUserInvite,
  withDeclinePublicationPowerUserInvite,
  withRouter,
  injectIntl,
  withHandlers<HandlerProps, Handlers>({
    acceptInvitation: (props) => () => {
      const { acceptPublicationPowerUserInvite } = props;

      processServerOperation(
        props,
        PublicationDetailsMessages.InvitationAcceptedMessage,
        {processingAccept: true},
        (publicationOid: string) => {
          return acceptPublicationPowerUserInvite({publicationOid});
        }
      );
    },
    declineInvitation: (props) => () => {
      const { declinePublicationPowerUserInvite } = props;

      processServerOperation(
        props,
        PublicationDetailsMessages.InvitationDeclinedMessage,
        {processingDecline: true},
        (publicationOid: string) => {
          return declinePublicationPowerUserInvite({publicationOid});
        }
      );
    }
  })
)(PublicationInvitationResponseButtonsComponent) as React.ComponentClass<ParentProps>;
