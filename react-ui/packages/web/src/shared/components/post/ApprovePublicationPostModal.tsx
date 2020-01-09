import * as React from 'react';
import { ConfirmationModal } from '../ConfirmationModal';
import { PostDetailMessages } from '../../i18n/PostDetailMessages';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { Paragraph } from '../Paragraph';
import { FormattedMessage } from 'react-intl';
import { Modal } from 'antd';

export interface ApprovePublicationPostModalProps {
  approvePost?: () => void;
  close: () => void;
  processing?: boolean;
}

export const ApprovePublicationPostModal: React.SFC<ApprovePublicationPostModalProps> = (props) => {
  const { approvePost, processing, close } = props;

  // jw: because approvePost is optional let's just output a stub modal until we have a handler. We just need something
  //     for the transition effects to operate on.
  if (!approvePost) {
    return <Modal />;
  }

  return (
    <ConfirmationModal
      // bl: only visible if we have a removePostFromPublicationHandler.
      visible={!!approvePost}
      processing={processing}
      dismiss={close}
      onConfirmation={approvePost}
      title={PostDetailMessages.ApprovePublicationPostModalTitle}
      btnText={PostDetailMessages.ApprovePost}
      btnProps={{ type: 'primary' }}
      linkText={SharedComponentMessages.Cancel}
      linkProps={{ onClick: close }}
    >
      <Paragraph marginBottom="large">
        <FormattedMessage {...PostDetailMessages.ApprovePublicationPostModalConfirmation} />
      </Paragraph>
    </ConfirmationModal>
  );
};
