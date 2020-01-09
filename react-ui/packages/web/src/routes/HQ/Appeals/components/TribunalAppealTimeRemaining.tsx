import * as React from 'react';
import { TribunalAppealAction } from './TribunalAppealAction';
import { CountDown } from '../../../../shared/components/CountDown';

interface ParentProps {
  endTime: string;
}

export const TribunalAppealTimeRemaining: React.SFC<ParentProps> = (props) => {
  const {endTime} = props;
  return (
    <TribunalAppealAction iconType="hourglass">
      <CountDown endTime={endTime} />
    </TribunalAppealAction>
  );
};
