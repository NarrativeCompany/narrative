import * as React from 'react';
import { SummaryGrid } from '../SummaryGrid';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { SummaryGridRow } from '../SummaryGridRow';
import { LocalizedTime } from '../LocalizedTime';
import { CountDown } from '../CountDown';
import { getApprovalPercentage } from '../../utils/referendumUtils';
import { LocalizedNumber } from '../LocalizedNumber';
import { EnhancedReferendumType } from '../../enhancedEnums/referendumType';
import { ReferendumProps } from '../../../routes/HQ/Approvals/Details/ApprovalDetails';
import { ReferendumMessages } from '../../i18n/ReferendumMessages';
import { compose } from 'recompose';
import { Paragraph } from '../Paragraph';
import { MemberLink } from '../user/MemberLink';

type Props =
  ReferendumProps &
  InjectedIntlProps;

const ReferendumSummarySectionComponent: React.SFC<Props> = (props) => {
  const { intl, referendum, referendum: { startDatetime, endDatetime } } = props;

  const type = EnhancedReferendumType.get(referendum.type);
  const isTribunalType = type.isTribunalType();
  const startMessage = isTribunalType
    ? ReferendumMessages.AppealPeriodStart
    : ReferendumMessages.ApprovalPeriodStart;
  const endMessage = isTribunalType
    ? ReferendumMessages.AppealPeriodEnd
    : ReferendumMessages.ApprovalPeriodEnd;
  const statusMessage = referendum.open
    ? ReferendumMessages.UnderReview
    : isTribunalType
      ? ReferendumMessages.AppealEnded
      : ReferendumMessages.ApprovalEnded;

  const totalPoints = parseFloat(referendum.votePointsFor) + parseFloat(referendum.votePointsAgainst);

  return (
    <SummaryGrid title={<FormattedMessage {...ReferendumMessages.Summary}/>}>
      <SummaryGridRow title={<FormattedMessage {...ReferendumMessages.SummaryType}/>}>
        <FormattedMessage {...type.getTypeMessage()}/>
      </SummaryGridRow>

      {type.isNicheApproval() && referendum.niche && referendum.niche.suggester &&
        <SummaryGridRow title={<FormattedMessage {...ReferendumMessages.Suggester}/>}>
          <MemberLink user={referendum.niche.suggester}/>
        </SummaryGridRow>
      }

      <SummaryGridRow title={<FormattedMessage {...startMessage}/>}>
        <LocalizedTime time={startDatetime}/>
      </SummaryGridRow>

      <SummaryGridRow title={<FormattedMessage {...endMessage}/>}>
        {referendum.open
          ? <CountDown endTime={endDatetime}/>
          : <LocalizedTime time={endDatetime}/>
        }
      </SummaryGridRow>

      <SummaryGridRow title={<FormattedMessage {...ReferendumMessages.CurrentStatus}/>}>
        <FormattedMessage {...statusMessage}/>
      </SummaryGridRow>

      {isTribunalType && <SummaryGridRow title={<FormattedMessage {...ReferendumMessages.TotalVotes}/>}>
        <LocalizedNumber value={totalPoints}/>
      </SummaryGridRow>}

      {!isTribunalType && <SummaryGridRow title={<FormattedMessage {...ReferendumMessages.TotalVotePoints}/>}>
        <LocalizedNumber value={totalPoints} minFractionLength={2}/>
      </SummaryGridRow>}

      <SummaryGridRow title={<FormattedMessage {...ReferendumMessages.UpVotePercentage}/>}>
        {getApprovalPercentage(referendum.votePointsFor, referendum.votePointsAgainst)}%
      </SummaryGridRow>

      {!referendum.open &&
        <SummaryGridRow title={<FormattedMessage {...ReferendumMessages.FinalResult}/>}>
          <Paragraph color="dark">
            <FormattedMessage {...type.getResultTitleMessage(referendum)} />
          </Paragraph>
          <Paragraph color="light">
            {type.getResultDescription(intl, referendum)}
          </Paragraph>
        </SummaryGridRow>
      }
    </SummaryGrid>
  );
};

export const ReferendumSummarySection = compose(
  injectIntl
)(ReferendumSummarySectionComponent) as React.ComponentClass<ReferendumProps>;
