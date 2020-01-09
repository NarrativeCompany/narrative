import * as React from 'react';
import { CountDown } from '../../../shared/components/CountDown';
import { ApprovalAction } from './ApprovalAction';

interface ParentProps {
  endTime: string;
}

export const ApprovalTimeRemaining: React.SFC<ParentProps> = (props) => {
  const { endTime } = props;
  return (
    <ApprovalAction iconType="hourglass">
      <CountDown endTime={endTime} />
    </ApprovalAction>
  );
};
