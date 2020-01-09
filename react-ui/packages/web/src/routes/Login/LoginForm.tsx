import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import { withRouter } from 'react-router-dom';
import { AuthWrapper } from '../../shared/styled/shared/auth';
import { Login } from './Login';
import { TwoFactorLogin } from './TwoFactorLogin';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { withState, WithStateProps } from '@narrative/shared';
import { RecoverPassword } from './RecoverPassword';

export enum LoginFormMessageType {
  NONE,
  LOGIN_REQUIRED,
  LOGIN_EXPIRED,
  EMAIL_VERIFIED
}

export enum ModalComponent {
  LOGIN,
  TWO_FACTOR,
  RECOVER_PASSWORD
}

interface State {
  modalComponent: ModalComponent;
}
const initialState: State = {
  modalComponent: ModalComponent.LOGIN
};

// tslint:disable no-any
interface WithHandlers {
  handleSetModalComponentVisible: (modalComponent: ModalComponent) => any;
}

interface ParentProps {
  forceTwoFactorVisible?: boolean;
  dismiss?: () => any;
  loginFormMessageType?: LoginFormMessageType;
  showTwoFactorExpiredMessage?: boolean;
}
// tslint:enable no-any

type Props =
  ParentProps &
  WithStateProps<State> &
  InjectedIntlProps &
  WithHandlers;

const LoginForm: React.SFC<Props> = (props) => {
  const { state, dismiss, handleSetModalComponentVisible, loginFormMessageType, showTwoFactorExpiredMessage } = props;

  return (
    <AuthWrapper centerAll={true} style={{marginTop: (dismiss ? 0 : 15)}}>

      {ModalComponent.LOGIN === state.modalComponent &&
      <Login
        dismiss={dismiss}
        setComponentVisible={handleSetModalComponentVisible}
        loginFormMessageType={loginFormMessageType}
      />}

      {ModalComponent.TWO_FACTOR === state.modalComponent &&
      <TwoFactorLogin
        dismiss={dismiss}
        showExpiredMessage={showTwoFactorExpiredMessage}
      />}

      {ModalComponent.RECOVER_PASSWORD === state.modalComponent &&
      <RecoverPassword
        dismiss={dismiss}
      />}

    </AuthWrapper>
  );
};

export default compose<Props, {}>(
  withRouter,
  injectIntl,
  withState<State>(initialState),
  withHandlers({
    handleSetModalComponentVisible: (props: Props) => (modalComponent: ModalComponent) => {
      props.setState(ss => ({...ss, modalComponent}));
    }
  }),
  withProps((props: Props) => {
    const {forceTwoFactorVisible, handleSetModalComponentVisible, state} = props;

    // Allow control of login form mode via parent props
    if (!(ModalComponent.TWO_FACTOR === state.modalComponent) && forceTwoFactorVisible) {
      handleSetModalComponentVisible(ModalComponent.TWO_FACTOR);
    }
  })
)(LoginForm) as React.ComponentClass<ParentProps>;
