import * as React from 'react';
import { Publication, User } from '@narrative/shared';
import { Modal } from 'antd';
import { ChangePublicationOwnerForm } from './ChangePublicationOwnerForm';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { FormattedMessage } from 'react-intl';

export interface ChangePublicationOwnerModalProps {
  visible?: boolean;
  publication: Publication;
  potentialOwners: User[];
  close: () => void;
}

export const ChangePublicationOwnerModal: React.SFC<ChangePublicationOwnerModalProps> = (props) => {
  const { visible, ...formProps } = props;

  // jw: if there are no potentialOwners then let's give a warning that there are no avilable admins
  if (!formProps.potentialOwners.length) {
    return (
      <Modal
        visible={visible}
        title={<FormattedMessage {...PublicationDetailsMessages.ChangePublicationOwnerFormTitle}/>}
        onCancel={props.close}
        footer={null}
      >
        <FormattedMessage {...PublicationDetailsMessages.NoAvailableAdminForOwnershipTransferWarning} />
      </Modal>
    );
  }

  return (
    <Modal
      visible={visible}
      onCancel={props.close}
      footer={null}
      destroyOnClose={true}
    >
      <ChangePublicationOwnerForm {...formProps} />
    </Modal>
  );
};
