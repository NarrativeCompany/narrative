import * as React from 'react';
import { Modal } from 'antd';
import { ConfirmPW2FACode } from './ConfirmPW2FACode';
import { MethodError } from '@narrative/shared';

interface ParentProps extends MethodError {
  // tslint:disable-next-line no-any
  dismiss: () => any;
  handleSubmit: (password: string, verificationCode?: string) => void;
  show2FAInput: boolean;
  // tslint:disable-next-line no-any
  fieldErrors: any | null;
  visible: boolean;
}

export const ConfirmPW2FACodeModal: React.SFC<ParentProps> = (props) => {
  const { visible, dismiss, handleSubmit, methodError, fieldErrors, show2FAInput } = props;

  return (
    <Modal
      visible={visible}
      onCancel={dismiss}
      footer={null}
      destroyOnClose={true}
    >
      <ConfirmPW2FACode
        onSubmit={handleSubmit}
        methodError={methodError}
        fieldErrors={fieldErrors}
        show2FAInput={show2FAInput}
      />
    </Modal>
  );
};
