import * as React from 'react';
import { SharedFeaturePostModalsProps } from './FeaturePostModals';
import { Modal } from 'antd';
import { PublicationDetailsMessages } from '../../i18n/PublicationDetailsMessages';
import { ConfirmationModal } from '../ConfirmationModal';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { Paragraph } from '../Paragraph';
import { FormattedMessage } from 'react-intl';

export interface UnfeaturePostModalHandler {
  unfeaturePostHandler?: () => void;
}

type Props =
  UnfeaturePostModalHandler &
  SharedFeaturePostModalsProps;

export const UnfeaturePostModal: React.SFC<Props> = (props) => {
  const { post, unfeaturePostHandler, closeModalHandler, processing } = props;

  // jw: use a stub modal if we do not have a handler since ConfirmationModal requires a onConfirmation handler and
  //     we need the modal on the DOM for transition effects to work.
  if (!post || !unfeaturePostHandler) {
    return (
      <Modal />
    );
  }

  return (
    <ConfirmationModal
      visible={true}
      processing={processing}
      dismiss={closeModalHandler}
      // jw: I kinda hate this, but since the buttons are outside of the form we have to manually trigger the submitForm
      onConfirmation={unfeaturePostHandler}
      title={PublicationDetailsMessages.StopFeaturingPostTitle}
      btnText={PublicationDetailsMessages.StopFeaturingPostButtonText}
      btnProps={{ type: 'danger' }}
      linkText={SharedComponentMessages.Cancel}
      linkProps={{ onClick: closeModalHandler }}
    >
      <Paragraph marginBottom="large">
        <FormattedMessage {...PublicationDetailsMessages.StopFeaturingPostQuestion} />
      </Paragraph>
    </ConfirmationModal>
  );
};
