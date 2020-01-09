import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { Modal } from 'antd';
import LoginForm from './LoginForm';
import { logout } from '../../shared/utils/authTokenUtils';
import { withExtractedAuthState, WithExtractedAuthStateProps } from '../../shared/containers/withExtractedAuthState';

// tslint:disable no-any
interface ParentProps {
  dismiss: () => any;
  visible: boolean;
}

interface WithHandlers {
  cancel: () => any;
}
// tslint:enable no-any

type Props =
  ParentProps &
  WithExtractedAuthStateProps &
  WithHandlers;

const LoginModalComponent: React.SFC<Props> = (props) => {
  const { visible, dismiss, cancel } = props;

  return (
      <Modal
        visible={visible}
        onCancel={cancel}
        footer={null}
        destroyOnClose={true}
      >
        <LoginForm dismiss={dismiss}/>
      </Modal>
  );
};

export const LoginModal = compose(
  withExtractedAuthState,
  withHandlers({
    cancel: (props: Props) => async () => {
      const { dismiss, userRequires2FA } = props;

      // if when dismissing the modal and user is cancelling 2fa
      if (userRequires2FA) {
        await logout();
      }

      // dismiss modal
      dismiss();
    }
  })
)(LoginModalComponent) as React.ComponentClass<ParentProps>;
