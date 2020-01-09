import * as React from 'react';
import { ConfirmationModal } from '../../../shared/components/ConfirmationModal';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import { PostMessages } from '../../../shared/i18n/PostMessages';

interface ParentProps {
  visible: boolean;
  dismiss: () => void;
  isDraft: boolean;
  onExitClick: () => void;
}

export const ExitPostConfirmation: React.SFC<ParentProps> = (props) => {
  const { visible, dismiss, isDraft, onExitClick } = props;

  const title = isDraft ?
    PostMessages.SaveAndExitConfirmationTitle :
    PostMessages.DiscardAndExitConfirmationTitle;
  const btnText = isDraft ?
    PostMessages.SaveAndExitBtnText :
    PostMessages.DiscardAndExitBtnText;

  return (
    <ConfirmationModal
      visible={visible}
      onConfirmation={onExitClick}
      dismiss={dismiss}
      title={title}
      btnText={btnText}
      btnProps={{ type: 'primary' }}
      linkText={SharedComponentMessages.Cancel}
      linkProps={{ onClick: dismiss }}
    />
  );
};
