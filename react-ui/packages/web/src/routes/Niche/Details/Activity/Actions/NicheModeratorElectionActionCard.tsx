import * as React from 'react';
import { compose } from 'recompose';
import { withNicheModeratorElectionDetails, WithNicheModeratorElectionDetailsProps } from '@narrative/shared';
import {
  DetailsActionPlaceholderCard
} from '../../../../../shared/components/detailAction/DetailsActionPlaceholderCard';
import { DetailsActionCard } from '../../../../../shared/components/detailAction/DetailsActionCard';
import { generatePath } from 'react-router';
import { WebRoute } from '../../../../../shared/constants/routes';
import { FormattedMessage } from 'react-intl';
import { NicheDetailsMessages } from '../../../../../shared/i18n/NicheDetailsMessages';

interface ParentProps {
  electionOid: string;
}

type Props = ParentProps &
  WithNicheModeratorElectionDetailsProps;

const NicheModeratorElectionActionCardComponent: React.SFC<Props> = (props) => {
  const { loading, electionOid, election } = props;

  // jw: if we are loading, then present the loading placeholder action card.
  if (loading) {
    return <DetailsActionPlaceholderCard/>;
  }

  // jw: if we failed to find an election from the server, let's output nothing.
  if (!election) {
    // todo:error-handling: we need to report this to the server, so that we can track down how this happened. We
    //      never delete Elections, so this should never ever happen!
    return null;
  }

  return (
    <DetailsActionCard
      icon="election"
      title={<FormattedMessage {...NicheDetailsMessages.NicheModeratorElection} />}
      sideColor="secondary-blue"
      countDown={null}
      toDetails={generatePath(WebRoute.ModeratorElectionDetails, { electionOid })}
    />
  );
};

export const NicheModeratorElectionActionCard = compose(
  withNicheModeratorElectionDetails
)(NicheModeratorElectionActionCardComponent) as React.ComponentClass<ParentProps>;
