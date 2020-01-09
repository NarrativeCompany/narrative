import * as React from 'react';
import { ReferendumProps } from './ApprovalDetails';
import { ApprovalDetailsActionCard } from '../../../../shared/components/approval/ApprovalDetailsActionCard';
import { FormattedMessage } from 'react-intl';
import { ApprovalDetailsMessages } from '../../../../shared/i18n/ApprovalDetailsMessages';
import { ReferendumVoteDescription } from '../../../../shared/components/referendum/ReferendumVoteDescription';
import { Heading } from '../../../../shared/components/Heading';

export const ApprovalActionCard: React.SFC<ReferendumProps> = (props) => {
  const { referendum, referendum: { currentUserVote } } = props;

  // jw: if the referendum is closed, and the current user has not voted, then there is no reason to show this
  //     action box, since there will be nothing meaningful about it.
  if (!referendum.open && !currentUserVote) {
    return null;
  }

  return (
    <ApprovalDetailsActionCard
      referendum={referendum}
      title={<FormattedMessage {...ApprovalDetailsMessages.VoteToApproveOrRejectThisNiche} />}
      placeButtonsBeforeBody={true}
    >

      {currentUserVote &&
        <Heading textAlign="center" size={4} style={{marginTop: '15px'}}>
          <ReferendumVoteDescription referendum={referendum}/>
        </Heading>}

    </ApprovalDetailsActionCard>
  );
};
