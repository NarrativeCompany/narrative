import * as React from 'react';
import { compose, withProps } from 'recompose';
import { ReferendumOidProps, ReferendumType, withReferendum, WithReferendumProps, } from '@narrative/shared';
import {
  DetailsActionPlaceholderCard
} from '../../../../../shared/components/detailAction/DetailsActionPlaceholderCard';
import { NicheDetailsMessages } from '../../../../../shared/i18n/NicheDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { ReferendumProgressBar } from '../../../../../shared/components/referendum/ReferendumProgressBar';
import { ApprovalDetailsActionCard } from '../../../../../shared/components/approval/ApprovalDetailsActionCard';
import { ReferendumProps } from '../../../../HQ/Approvals/Details/ApprovalDetails';

type Props = ReferendumOidProps &
  ReferendumProps &
  {
    loading: boolean;
  };

const NicheReferendumActionCardComponent: React.SFC<Props> = (props) => {
  const { loading, referendum } = props;

  // jw: if we are loading, then present the loading placeholder action card.
  if (loading) {
    return <DetailsActionPlaceholderCard />;
  }

  // jw: if we failed to find a referendum from the server, let's output nothing.
  if (!referendum) {
    // todo:error-handling: we need to report this to the server, so that we can track down how this happened. We
    //      never delete referendums, so this should never ever happen!
    return null;
  }

  const { type, votePointsFor, votePointsAgainst } = referendum;

  let descriptionMessage;
  switch (type) {
    case ReferendumType.RATIFY_NICHE:
    case ReferendumType.APPROVE_SUGGESTED_NICHE:
      descriptionMessage = NicheDetailsMessages.ApproveSuggestedNicheDescription;
      break;
    default:
      // todo:error-handling: Report to the server if the type is not 'APPROVE_REJECTED_NICHE'
      descriptionMessage = NicheDetailsMessages.ApproveRejectedNicheDescription;
      break;
  }

  return (
    <ApprovalDetailsActionCard
      referendum={referendum}
      title={<FormattedMessage {...NicheDetailsMessages.Review} />}
      footerText={<FormattedMessage {...NicheDetailsMessages.ReviewDetails} />}
    >
      <Paragraph size="small">
        <FormattedMessage {...descriptionMessage} />
      </Paragraph>

      <ReferendumProgressBar votePointsFor={votePointsFor} votePointsAgainst={votePointsAgainst}/>
    </ApprovalDetailsActionCard>
  );
};

export const NicheReferendumActionCard = compose(
  withReferendum,
  withProps((props: WithReferendumProps) => {
    const { loading, getReferendum } = props.referendumData;

    return { loading, referendum: getReferendum };
  })
)(NicheReferendumActionCardComponent) as React.ComponentClass<ReferendumOidProps>;
