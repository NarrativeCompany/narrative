import * as React from 'react';
import { Modal } from 'antd';
import { UpdateMemberNeoWalletForm } from './UpdateMemberNeoWalletForm';

interface Props {
  visible?: boolean;
  dismiss: () => void;
}

export const UpdateMemberNeoWalletModal: React.SFC<Props> = (props) => {
  const { visible, dismiss } = props;

  return (
    <Modal
      visible={visible}
      onCancel={dismiss}
      footer={null}
      destroyOnClose={true}
    >
      <UpdateMemberNeoWalletForm dismiss={dismiss} />
    </Modal>
  );
};
