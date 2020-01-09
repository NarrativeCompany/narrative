import * as React from 'react';
import { WithNicheDetailsContextProps } from '../../components/NicheDetailsContext';
import { FormattedMessage } from 'react-intl';
import { NicheDetailsMessages } from '../../../../../shared/i18n/NicheDetailsMessages';
import { NicheAppealActionCard } from './NicheAppealActionCard';
import { NicheAuctionActionCard } from './NicheAuctionActionCard';
import { NicheReferendumActionCard } from './NicheReferendumActionCard';
import { NicheModeratorElectionActionCard } from './NicheModeratorElectionActionCard';
import { ActiveInvoiceStatusCard } from '../../../../../shared/components/auction/ActiveInvoiceStatusCard';
import { SectionHeader } from '../../../../../shared/components/SectionHeader';

export const NicheActions: React.SFC<WithNicheDetailsContextProps> = (props) => {
  const {
    activeAuctionOid,
    activeModeratorElectionOid,
    currentBallotBoxReferendumOid,
    currentTribunalAppealOids,
    currentUserActiveInvoiceOid
  } = props.nicheDetail;

  const actions: React.ReactNode[] = [];

  // jw: If the user has a outstanding invoice, let's include the status for that first
  if (currentUserActiveInvoiceOid) {
    actions.push(<ActiveInvoiceStatusCard key={currentUserActiveInvoiceOid} invoiceOid={currentUserActiveInvoiceOid}/>);
  }

  // jw: If there is an auction open right now, let's include that
  if (activeAuctionOid) {
    actions.push(<NicheAuctionActionCard key={activeAuctionOid} auctionOid={activeAuctionOid}/>);
  }

  // jw: If we have a active referendum then let's include that on the page
  if (currentBallotBoxReferendumOid) {
    actions.push((
      <NicheReferendumActionCard
        key={currentBallotBoxReferendumOid}
        referendumOid={currentBallotBoxReferendumOid}
      />
    ));
  }

  // jw: If there is a moderator election happening right now, let's include that.
  if (activeModeratorElectionOid) {
    actions.push((
      <NicheModeratorElectionActionCard
        key={activeModeratorElectionOid}
        electionOid={activeModeratorElectionOid}
      />
    ));
  }

  // jw: Finally, let's include any open appeals
  if (currentTribunalAppealOids) {
    currentTribunalAppealOids.map(
      (tribunalIssueOid: string) => actions.push((
        <NicheAppealActionCard
          key={tribunalIssueOid}
          tribunalIssueOid={tribunalIssueOid}
        />))
    );
  }

  // jw: if we don't have any actions, short out.
  if (!actions.length) {
    return null;
  }

  return (
    <React.Fragment>
      <SectionHeader title={<FormattedMessage {...NicheDetailsMessages.Actions} />}/>

      {actions.map(
        (action: React.ReactNode) => action
      )}

    </React.Fragment>
  );
};
