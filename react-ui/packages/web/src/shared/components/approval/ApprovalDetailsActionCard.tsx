import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { withState, WithStateProps } from '@narrative/shared';
import { generatePath } from 'react-router';
import { WebRoute } from '../../constants/routes';
import { DetailsActionCard } from '../detailAction/DetailsActionCard';
import { ApprovalCardVoteButtons } from '../../../routes/HQ/Approvals/ApprovalCardVoteButtons';
import { CountDown } from '../CountDown';
import { RejectNicheReferendumModal } from '../niche/RejectNicheReferendumModal';
import { ReferendumProps } from '../../../routes/HQ/Approvals/Details/ApprovalDetails';
import { EnhancedReferendumType } from '../../enhancedEnums/referendumType';

interface State {
  rejectReasonModalOpen: boolean;
}

interface WithHandlers {
  // tslint:disable-next-line no-any
  toggleRejectReasonModal: (show: boolean) => any;
}

interface ParentProps extends ReferendumProps {
  title: React.ReactNode;
  footerText?: React.ReactNode;
  placeButtonsBeforeBody?: boolean;
}

type Props = ParentProps &
  WithHandlers &
  WithStateProps<State>;

const ApprovalDetailsActionCardComponent: React.SFC<Props> = (props) => {
  const { referendum, title, footerText, placeButtonsBeforeBody, toggleRejectReasonModal } = props;

  // jw: if we failed to find a referendum from the server, let's output nothing.
  if (!referendum) {
    // todo:error-handling: we need to report this to the server, so that we can track down how this happened. We
    //      never delete referendums, so this should never ever happen!
    return null;
  }

  const { endDatetime } = referendum;
  const referendumOid = referendum.oid;

  const includeRejectReasonForm = referendum.open &&
    EnhancedReferendumType.get(referendum.type).isRequiresRejectionReason();

  const voteButtons = (!referendum.open ? null : (
    <ApprovalCardVoteButtons
      showRejectReasonForm={() => toggleRejectReasonModal(true)}
      referendum={referendum}
    />)
  );

  return (
    <React.Fragment>
      <DetailsActionCard
        icon={footerText ? 'review' : undefined}
        title={title}
        sideColor="blue"
        countDown={<CountDown endTime={endDatetime}/>}
        toDetails={footerText ? generatePath(WebRoute.ApprovalDetails, {referendumOid}) : undefined}
        footerText={footerText}
      >
        {placeButtonsBeforeBody && voteButtons}

        {props.children}

        {!placeButtonsBeforeBody && voteButtons}

      </DetailsActionCard>

      {includeRejectReasonForm && <RejectNicheReferendumModal
        referendum={referendum}
        dismiss={() => toggleRejectReasonModal(false)}
        visible={props.state.rejectReasonModalOpen}
      />}
    </React.Fragment>
  );
};

export const ApprovalDetailsActionCard = compose(
  withState<State>({rejectReasonModalOpen: false}),
  withHandlers({
    toggleRejectReasonModal: (props: WithStateProps<State>) => (show: boolean) => {
      props.setState(ss => ({...ss, rejectReasonModalOpen: show}));
    }
  })
)(ApprovalDetailsActionCardComponent) as React.ComponentClass<ParentProps>;
