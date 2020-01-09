import * as React from 'react';
import { branch, compose, renderNothing, withProps } from 'recompose';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from '../../containers/withExtractedCurrentUser';
import {
  withNicheUserAssociations,
  WithNicheUserAssociationsProps
} from '@narrative/shared';
import { SidebarCard } from '../SidebarCard';
import { FormattedMessage } from 'react-intl';
import { SidebarMessages } from '../../i18n/SidebarMessages';
import { Paragraph } from '../Paragraph';
import { Link } from '../Link';
import { WebRoute } from '../../constants/routes';
import styled from '../../styled';
import { MAX_NICHES } from '../../constants/constants';

const SlotCountContainer = styled.span`
  color: white;
  background-color: ${props => props.theme.primaryGreen};
  line-height: 30px;
  border-radius: 15px;
  width: 30px;
  display: inline-block;
`;

interface Props extends WithExtractedCurrentUserProps {
  loading: boolean;
  usedSlotCount: number;
}

const NicheSlotCountSidebarCardComponent: React.SFC<Props> = (props) => {
  const { loading, currentUser, usedSlotCount } = props;

  if (!currentUser) {
    // todo:error-handling: We should never get here since we have a branch HOC that should short out if we don't
    //      have one. So log with the server.
    return null;
  }

  if (loading) {
    return (
      <SidebarCard
        title={<FormattedMessage {...SidebarMessages.MyNicheSlotCount} />}
        loading={true}
      />
    );
  }

  const usedSlotCountHtml = <SlotCountContainer>{usedSlotCount}</SlotCountContainer>;

  return (
    <SidebarCard title={<FormattedMessage {...SidebarMessages.MyNicheSlotCount} />}>
      <Paragraph size="large" textAlign="center" marginBottom="large">
        <FormattedMessage
          {...SidebarMessages.MyNicheSlotCountDescription}
          values={{usedSlotCountHtml, totalSlotCount: MAX_NICHES}}
        />
      </Paragraph>

      <Paragraph size="small" textAlign="center">
        <Link to={WebRoute.Auctions}>
          <FormattedMessage {...SidebarMessages.BrowseAuctions}/>
        </Link>
      </Paragraph>
    </SidebarCard>
  );
};

export const NicheSlotCountSidebarCard = compose(
  // jw: if the current user is not logged in, then
  withExtractedCurrentUser,
  branch((props: WithExtractedCurrentUserProps) => (!props.currentUser),
    renderNothing
  ),
  withProps((props: WithExtractedCurrentUserProps) => {
    const { currentUser } = props;
    const userOid = currentUser && currentUser.oid;

    return { userOid };
  }),
  withNicheUserAssociations,
  // jw: next, let's pull the associations and loading flag off of the hql query results and expose that.
  withProps((props: WithNicheUserAssociationsProps) => {
    const { associations } = props;

    return { usedSlotCount: associations.length };
  })
)(NicheSlotCountSidebarCardComponent) as React.ComponentClass<{}>;
