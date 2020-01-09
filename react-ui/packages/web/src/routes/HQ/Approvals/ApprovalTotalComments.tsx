import * as React from 'react';
import { ApprovalAction } from './ApprovalAction';
import { generatePath } from 'react-router';
import { WebRoute } from '../../../shared/constants/routes';
import { commentsAnchor } from '../../../shared/components/comment/CommentsSection';

interface ParentProps {
  referendumOid: string;
  totalComments: number;
}

export const ApprovalTotalComments: React.SFC<ParentProps> = (props) => {
  const { referendumOid, totalComments } = props;

  const toComments = totalComments > 0
    ? `${generatePath(WebRoute.ApprovalDetails, { referendumOid })}#${commentsAnchor}`
    : undefined;

  return (
    <ApprovalAction href={toComments} iconType="message">
      {totalComments}
    </ApprovalAction>
  );
};
