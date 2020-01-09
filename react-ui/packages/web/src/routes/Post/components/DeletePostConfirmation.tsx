import * as React from 'react';
import { PostMessages } from '../../../shared/i18n/PostMessages';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import { ConfirmationModal } from '../../../shared/components/ConfirmationModal';

export interface DeletePostConfirmationProps {
  visible: boolean;
  useGenericDeleteText?: boolean;
  dismiss: () => void;
  onDeletePost: () => void;
  isLoading?: boolean;
}

export const DeletePostConfirmation: React.SFC<DeletePostConfirmationProps> = (props) => {
  const { visible, dismiss, onDeletePost, useGenericDeleteText, isLoading } = props;

  return (
    <ConfirmationModal
      visible={visible}
      processing={isLoading}
      dismiss={dismiss}
      onConfirmation={onDeletePost}
      title={PostMessages.DeletePostConfirmationTitle}
      btnText={useGenericDeleteText ? PostMessages.DeleteBtnText : PostMessages.DeleteAndExitBtnText}
      btnProps={{ type: 'danger' }}
      linkText={SharedComponentMessages.Cancel}
      linkProps={{ onClick: dismiss }}
    />
  );
};
