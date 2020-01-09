import * as React from 'react';
import { branch, compose, lifecycle, renderComponent, withProps } from 'recompose';
import {
  PublicationDetailsConnect,
  WithPublicationDetailsContextProps
} from '../../components/PublicationDetailsContext';
import {
  PublicationRole,
  withPublicationPowerUserInvitation,
  WithPublicationPowerUserInvitationParentProps,
  WithPublicationPowerUserInvitationProps
} from '@narrative/shared';
import { fullPlaceholder, withLoadingPlaceholder } from '../../../../shared/utils/withLoadingPlaceholder';
import { withRouter, RouteComponentProps, generatePath } from 'react-router';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import { openNotification } from '../../../../shared/utils/notificationsUtil';
import { PublicationDetailsMessages } from '../../../../shared/i18n/PublicationDetailsMessages';
import { getIdForUrl } from '../../../../shared/utils/routeUtils';
import { WebRoute } from '../../../../shared/constants/routes';
import { EnhancedPublicationRole } from '../../../../shared/enhancedEnums/publicationRole';
import { SEO } from '../../../../shared/components/SEO';
import { Paragraph } from '../../../../shared/components/Paragraph';
import { FormattedMessage } from 'react-intl';
import { Heading } from '../../../../shared/components/Heading';
import { PublicationInvitationResponseButtons } from './components/PublicationInvitationResponseButtons';
import { Card } from '../../../../shared/components/Card';
import styled from '../../../../shared/styled';

export const CardContainer = styled.div`
  margin: 20px auto 0;
  max-width: 550px;
`;

type Props = WithPublicationDetailsContextProps &
  WithPublicationPowerUserInvitationProps &
  InjectedIntlProps;

const PublicationInvitationComponent: React.SFC<Props> = (props) => {
  // jw: if we got here we know for a fact that we have invitedRoles.
  const { invitedRoles, publicationDetail: { publication } } = props;

  return (
    <React.Fragment>
      <SEO title={PublicationDetailsMessages.PublicationInvitationSeoTitle} publication={publication} />

      <CardContainer>
        <Card>
          <Heading size={2}>
            <FormattedMessage {...PublicationDetailsMessages.PublicationInvitationTitle} />
          </Heading>

          <Paragraph marginBottom="large">
            <FormattedMessage {...PublicationDetailsMessages.PublicationInvitationDescription} />
          </Paragraph>

          <ul>
            {invitedRoles.map((role: PublicationRole) => {
              const roleType = EnhancedPublicationRole.get(role);

              return (
                <li key={`roleDescription_${role}`}>
                  <FormattedMessage {...roleType.name}/>
                  {': '}
                  <FormattedMessage {...roleType.description}/>
                </li>
              );
            })}
          </ul>

          <Paragraph marginBottom="large">
            <FormattedMessage {...PublicationDetailsMessages.PublicationInvitationAcceptanceQuestion}/>
          </Paragraph>

          <PublicationInvitationResponseButtons
            publication={publication}
            invitedRoles={invitedRoles}
          />
        </Card>
      </CardContainer>
    </React.Fragment>
  );
};

export default compose(
  PublicationDetailsConnect,
  withProps<WithPublicationPowerUserInvitationParentProps, WithPublicationDetailsContextProps>(
    (props: WithPublicationDetailsContextProps) => {
      const publicationOid = props.publicationDetail.oid;

      return { publicationOid };
    }
  ),
  withPublicationPowerUserInvitation,
  withLoadingPlaceholder(fullPlaceholder),
  withRouter,
  injectIntl,
  // jw: if the user has not been invited to any roles then we will want to redirect them to the publication home
  //     page with a message explaining that they have no invitations pending.
  lifecycle<Props & RouteComponentProps, {}>({
    // tslint:disable-next-line object-literal-shorthand
    componentDidMount: async function () {
      const {
        invitedRoles,
        history, intl: { formatMessage },
        publicationDetail: { publication: { oid, prettyUrlString } }
      } = this.props;

      if (!invitedRoles.length) {
        await openNotification.updateSuccess(
          {
            description: '',
            message: formatMessage(PublicationDetailsMessages.NoInvitationsPending),
            duration: 5
          });

        const id = getIdForUrl(prettyUrlString, oid);
        history.push(generatePath(WebRoute.PublicationDetails, {id}));
      }
    }
  }),
  // jw: if the component is going to redirect don't bother rendering the body.
  branch<WithPublicationPowerUserInvitationProps>(props => props.invitedRoles.length === 0,
    renderComponent(() => null)
  )
)(PublicationInvitationComponent) as React.ComponentClass<{}>;
