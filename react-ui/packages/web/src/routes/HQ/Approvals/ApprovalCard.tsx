import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { FlipCard } from '../../../shared/components/FlipCard';
import { ApprovalCardFront } from './ApprovalCardFront';
import { ApprovalCardBack } from './ApprovalCardBack';
import { withState, WithStateProps } from '@narrative/shared';
import { Referendum } from '@narrative/shared';
import { Card } from '../../../shared/components/Card';

interface State {
  isFlipped: boolean;
}
const initialState: State = {
  isFlipped: false,
};

// tslint:disable no-any
interface WithHandlers {
  handleToggleFlipCard: () => any;
}
// tslint:enable no-any

interface ParentProps {
  referendum: Referendum;
}

type Props =
  ParentProps &
  WithStateProps<State> &
  WithHandlers;

const ApprovalCardComponent: React.SFC<Props> = (props) => {
  const { state, referendum, handleToggleFlipCard } = props;

  return (
    <FlipCard
      isFlipped={state.isFlipped}
      front={<ApprovalCardFront referendum={referendum} toggleCard={handleToggleFlipCard}/>}
      back={<ApprovalCardBack referendum={referendum} toggleCard={handleToggleFlipCard}/>}
      placeholder={<Card height={400} loading={true}/>}
    />
  );
};

export const ApprovalCard = compose(
  withState<State>(initialState),
  withHandlers({
    handleToggleFlipCard: (props: Props) => () => props.setState(ss => ({...ss, isFlipped: !ss.isFlipped}))
  })
)(ApprovalCardComponent) as React.ComponentClass<ParentProps>;
