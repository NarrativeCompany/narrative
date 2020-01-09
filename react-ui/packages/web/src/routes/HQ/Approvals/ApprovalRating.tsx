import * as React from 'react';
import { ApprovalAction } from './ApprovalAction';

interface ParentProps {
  approvalRating: number;
}

export const ApprovalRating: React.SFC<ParentProps> = (props) => {
  const {approvalRating} = props;

  return (
    <ApprovalAction iconType="like-o">
      {approvalRating}%
    </ApprovalAction>
  );
};
