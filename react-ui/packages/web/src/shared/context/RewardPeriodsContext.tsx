import * as React from 'react';
import { RewardPeriod } from '@narrative/shared';

// bl: modeled everything here off of MemberProfileContext
export interface WithRewardPeriodsProps {
  rewardPeriods: RewardPeriod[];
  onCanceledRedemptionRequest?: () => void;
}

// tslint:disable-next-line no-any
export const RewardPeriodsContext: React.Context<any> = React.createContext(undefined as any);

// jw: since we have the user in context, let's provide a Connector to make consuming that user much easier
export const RewardPeriodsConnect = <P extends WithRewardPeriodsProps>(
  WrappedComponent: React.ComponentType<P> | React.SFC<P>
) => {
  return class extends React.PureComponent {
    public render () {
      return (
        <RewardPeriodsContext.Consumer>
          {(contextProps: WithRewardPeriodsProps) => {
            return (
              <WrappedComponent
                {...contextProps}
                {...this.props}
              />
            );
          }}
        </RewardPeriodsContext.Consumer>
      );
    }
  };
};
