import * as React from 'react';
import { Modal } from 'antd';
import { DeleteMemberNeoWalletForm } from './DeleteMemberNeoWalletForm';

interface Props {
  visible?: boolean;
  dismiss: () => void;
}

export const DeleteMemberNeoWalletModal: React.SFC<Props> = (props) => {
  const { visible, dismiss } = props;

  return (
    <Modal
      visible={visible}
      onCancel={dismiss}
      footer={null}
      destroyOnClose={true}
    >
      <DeleteMemberNeoWalletForm dismiss={dismiss} />
    </Modal>
  );
};
