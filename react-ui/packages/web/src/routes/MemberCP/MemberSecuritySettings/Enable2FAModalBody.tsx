import * as React from 'react';
import {
  withGenerateTwoFactorSecret,
  WithGenerateTwoFactorSecretProps,
  withState,
  WithStateProps,
} from '@narrative/shared';
import { branch, compose, Omit, renderComponent, withHandlers, withProps } from 'recompose';
import { Enable2FAModalPartOne, Enable2FAModalPartOneProps } from './Enable2FAModalPartOne';
import { Enable2FAModalPartTwo, Enable2FAModalPartTwoProps } from './Enable2FAModalPartTwo';
import { Enable2FAModalProps } from './Enable2FAModal';
import { ContainedLoading } from '../../../shared/components/Loading';

interface State {
  hasEnabled2FA?: boolean;
}

type ParentProps = Omit<Enable2FAModalProps, 'visible'>;

interface Handlers {
  on2faEnabled: () => void;
}

type Props =
  ParentProps &
  Enable2FAModalPartOneProps &
  Enable2FAModalPartTwoProps &
  WithStateProps<State> &
  Handlers;

const Enable2FAModalBodyComponent: React.SFC<Props> = (props) => {
  const {
    qrCodeImage,
    secret,
    backupCodes,
    dismiss,
    on2faEnabled,
    state: { hasEnabled2FA }
  } = props;

  return (
    <React.Fragment>
      {!hasEnabled2FA &&
        <Enable2FAModalPartOne
          secret={secret}
          qrCodeImage={qrCodeImage}
          success={on2faEnabled}
        />
      }

      {hasEnabled2FA &&
        <Enable2FAModalPartTwo
          backupCodes={backupCodes}
          dismiss={dismiss}
        />
      }
    </React.Fragment>
  );
};

export const Enable2FAModalBody =  compose(
  withGenerateTwoFactorSecret,
  branch<WithGenerateTwoFactorSecretProps>((props) => props.twoFactorSecretData.loading,
    renderComponent(() => <ContainedLoading />)
  ),
  // jw: let's use the withGenerateTwoFactorSecret data to fulfill the properties needed for both parts of this process
  withProps<Enable2FAModalPartOneProps & Enable2FAModalPartTwoProps, WithGenerateTwoFactorSecretProps>((props) => {
    const { twoFactorSecretData: { getGeneratedTwoFactorSecret, loading } } = props;
    const secret =
      getGeneratedTwoFactorSecret &&
      getGeneratedTwoFactorSecret.secret;
    const qrCodeImage =
      getGeneratedTwoFactorSecret &&
      getGeneratedTwoFactorSecret.qrCodeImage;
    const backupCodes =
      getGeneratedTwoFactorSecret &&
      getGeneratedTwoFactorSecret.backupCodes;

    return { secret, qrCodeImage, backupCodes, loading };
  }),
  withState<State>({}),
  withHandlers<ParentProps & WithStateProps<State>, Handlers>({
    on2faEnabled: (props) => () => {
      const { success, setState } = props;

      // jw: first: let's let the parent know that 2FA has been enabled.
      success();

      // jw: next, let's update to show the backup codes to the user.
      setState(ss => ({...ss, hasEnabled2FA: true}));
    }
  })
)(Enable2FAModalBodyComponent) as React.ComponentClass<ParentProps>;
