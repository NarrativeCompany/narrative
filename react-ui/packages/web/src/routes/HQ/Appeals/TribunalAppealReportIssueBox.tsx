import * as React from 'react';
import { NicheEditDetail, TribunalIssueType } from '@narrative/shared';
import { TribunalAppealReportCommentBox } from './components/TribunalAppealReportCommentBox';
import { NicheEditDetails } from './components/NicheEditDetails';

interface ParentProps {
  type: TribunalIssueType;
  nicheEditDetail: NicheEditDetail|null;
  comments: string|null;
}

export const TribunalAppealReportIssueBox: React.SFC<ParentProps> = (props) => {
  const { type, comments, nicheEditDetail } = props;

  if (TribunalIssueType.APPROVE_NICHE_DETAIL_CHANGE === type && nicheEditDetail) {
    return <NicheEditDetails editDetails={nicheEditDetail} />;
  }

  return <TribunalAppealReportCommentBox comments={comments}/>;
};
