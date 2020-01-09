import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  TribunalIssueDetail,
  TribunalIssueOidProps,
  TribunalIssueType,
  withTribunalAppealSummary,
  WithTribunalAppealSummaryProps
} from '@narrative/shared';
import {
  DetailsActionPlaceholderCard
} from '../../../../../shared/components/detailAction/DetailsActionPlaceholderCard';
import { DetailsActionCard } from '../../../../../shared/components/detailAction/DetailsActionCard';
import { generatePath } from 'react-router';
import { WebRoute } from '../../../../../shared/constants/routes';
import { CountDown } from '../../../../../shared/components/CountDown';
import { FormattedMessage } from 'react-intl';
import { NicheDetailsMessages } from '../../../../../shared/i18n/NicheDetailsMessages';

interface Props extends TribunalIssueOidProps {
  issueDetail: TribunalIssueDetail;
  loading: boolean;
}

const NicheAppealActionCardComponent: React.SFC<Props> = (props) => {
  const { loading, issueDetail, tribunalIssueOid } = props;

  // jw: if we are loading, then present the loading placeholder action card.
  if (loading) {
    return <DetailsActionPlaceholderCard />;
  }

  // jw: if we failed to find a issueDetail from the server, let's output nothing.
  if (!issueDetail) {
    // todo:error-handling: we need to report this to the server, so that we can track down how this happened. We
    //      never delete Tribunal Appeals, so this should never ever happen!
    return null;
  }

  const { tribunalIssue, tribunalIssue: { referendum } } = issueDetail;
  const { endDatetime } = referendum;

  const isNicheEdit = tribunalIssue.type === TribunalIssueType.APPROVE_NICHE_DETAIL_CHANGE;

  const titleMessage = isNicheEdit ? NicheDetailsMessages.NicheEditRequest : NicheDetailsMessages.AppealRequest;

  return (
    <DetailsActionCard
      icon={isNicheEdit ? 'edit' : 'appeals'}
      title={<FormattedMessage {...titleMessage} />}
      sideColor={isNicheEdit ? 'green' : 'orange'}
      countDown={<CountDown endTime={endDatetime}/>}
      toDetails={generatePath(WebRoute.AppealDetails, {tribunalIssueOid})}
    />
  );
};

export const NicheAppealActionCard = compose(
  withTribunalAppealSummary,
  withProps((props: WithTribunalAppealSummaryProps) => {
    const { loading, getTribunalAppealSummary } = props.tribunalAppealSummaryData;

    return { loading, issueDetail: getTribunalAppealSummary };
  })
)(NicheAppealActionCardComponent) as React.ComponentClass<TribunalIssueOidProps>;
