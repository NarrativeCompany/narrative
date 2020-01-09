import { branch, compose, renderComponent, withProps } from 'recompose';
import { ContainedLoading } from '../components/Loading';
import * as React from 'react';
import {
  TwoFactorEnabledOnAccountProps,
  withCurrentUserTwoFactorAuthState,
  WithCurrentUserTwoFactorAuthStateProps
} from '@narrative/shared';

export const withCurrentUserTwoFactorAuthEnabled = compose(
  withCurrentUserTwoFactorAuthState,
  branch<WithCurrentUserTwoFactorAuthStateProps>((props) => props.currentUserTwoFactorAuthStateData.loading,
    renderComponent(() => <ContainedLoading/>)
  ),
  withProps<TwoFactorEnabledOnAccountProps, WithCurrentUserTwoFactorAuthStateProps>(
    (props): TwoFactorEnabledOnAccountProps => {
      const { currentUserTwoFactorAuthStateData: { getCurrentUserTwoFactorAuthState } } = props;
      const twoFactorEnabled =
        getCurrentUserTwoFactorAuthState &&
        getCurrentUserTwoFactorAuthState.enabled;

      return { twoFactorEnabled };
    }
  )
);
