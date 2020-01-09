import * as React from 'react';
import { Modal } from 'antd';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { InvitePowerUserButtonProps } from './InvitePowerUserButton';
import { InvitePowerUserModalBody } from './InvitePowerUserModalBody';

interface Props extends InvitePowerUserButtonProps {
  visible?: boolean;
  close: () => void;
}

export const InvitePowerUserModal: React.SFC<Props> = (props) => {
  const { visible, close, ...bodyProps } = props;
  const { canInviteRoles, currentUserRoles } = bodyProps;

  // jw: there are two pieces of the body which change based on whether the current user can send any invitations.
  let description: FormattedMessage.MessageDescriptor;
  let body: React.ReactNode | undefined;

  // jw: we can get here with no invitation roles available for admins and editors if there are no free slots for the
  //     roles that they can invite. So, let's handle that up front.
  if (!canInviteRoles.length) {
    description = currentUserRoles.admin
      ? PublicationDetailsMessages.InvitePowerUserModalAdminWarning
      : PublicationDetailsMessages.InvitePowerUserModalEditorWarning;

  } else {
    description = PublicationDetailsMessages.InvitePowerUserModalDescription;
    /*
        jw: note, since we are going to be relying on state pretty heavily for this we need to keep that part of the
            component isolated outside of the modal itself. Otherwise the state would remain between loads of the modal
            and this is just easier to manage since it better separates concerns.
    */
    body = <InvitePowerUserModalBody close={close} {...bodyProps} />;
  }

  return (
    <Modal
      title={<FormattedMessage {...PublicationDetailsMessages.InvitePowerUserModalTitle}/>}
      visible={visible}
      destroyOnClose={true}
      onCancel={close}
      footer={null}
    >
      <Paragraph marginBottom="large">
        <FormattedMessage {...description}/>
      </Paragraph>

      {body}
    </Modal>
  );
};
